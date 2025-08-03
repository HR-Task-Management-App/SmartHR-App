package com.example.smarthr_app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.Chat
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.data.model.SeenMessage
import com.example.smarthr_app.data.model.SuccessApiResponseMessage
import com.example.smarthr_app.data.model.UserInfo
import com.example.smarthr_app.data.repository.ChatRepository
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.chat.ChatWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private var webSocketClient: ChatWebSocketClient? = null
    private var currentUserId: String? = null

    private val _chatList = MutableStateFlow<Resource<List<Chat>>?>(null)
    val chatList: StateFlow<Resource<List<Chat>>?> = _chatList

    private val _userList = MutableStateFlow<Resource<List<UserInfo>>?>(null)
    val userList: StateFlow<Resource<List<UserInfo>>?> = _userList

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages

    private val _loadingMessages = MutableStateFlow<Set<String>>(emptySet())
    val loadingMessages: StateFlow<Set<String>> = _loadingMessages

    private val _notificationEvent = MutableStateFlow<Pair<String, String>?>(null)
    val notificationEvent: StateFlow<Pair<String, String>?> = _notificationEvent

    private val _activeChatUserId = MutableStateFlow<String?>(null)
    val activeChatUserId: StateFlow<String?> = _activeChatUserId

    private val _seenStatus = MutableStateFlow<Resource<SuccessApiResponseMessage>?>(null)
    val seenStatus: StateFlow<Resource<SuccessApiResponseMessage>?> = _seenStatus

    fun setActiveChatUser(userId: String?) {
        _activeChatUserId.value = userId
    }

    private fun triggerNotification(title: String, message: String) {
        _notificationEvent.value = title to message
    }

    fun initSocket(userId: String) {
        currentUserId = userId
        webSocketClient = ChatWebSocketClient(
            userId = userId,
            onMessageReceived = { handleIncomingMessage(it) },
            handleSeenMessage = { handleSeenMessage(it) }
        )
        webSocketClient?.connect()
    }

    fun sendMessage(senderId: String, receiverId: String, content: String, companyCode: String, type: String = "TEXT") {
        webSocketClient?.sendMessage(senderId, receiverId, content, companyCode, type)
    }

    fun sendSeenMessageInfo(chatId: String, userId: String) {
        webSocketClient?.sendSeenMessageInfo(chatId, userId)
    }

    private fun handleSeenMessage(seen: SeenMessage) {
        val chatId = seen.chatId
        _messages.update { map ->
            map.mapValues { (_, list) ->
                list.map {
                    if (it.chatId == chatId && it.sender.id == currentUserId)
                        it.copy(messageStatus = "SEEN")
                    else it
                }
            }
        }
        _chatList.update { state ->
            val current = (state as? Resource.Success)?.data ?: return@update state
            Resource.Success(
                current.map {
                    if (it.id == chatId && it.lastMessageSender == currentUserId)
                        it.copy(lastMessageStatus = "SEEN")
                    else it
                }
            )
        }
    }

    fun getMyChatList(companyCode: String) {
        viewModelScope.launch {
            _chatList.value = Resource.Loading()
            _chatList.value = chatRepository.getMyChatList(companyCode)
        }
    }

    fun getAllUser() {
        viewModelScope.launch {
            _userList.value = Resource.Loading()
            _userList.value = chatRepository.getAllUsers()
        }
    }

    fun getChatBetweenUsers(chatId: String, companyCode: String, otherUserId: String) {
        viewModelScope.launch {
            _loadingMessages.update { it + chatId }

            val result = chatRepository.getChatBetweenUser(companyCode, otherUserId)

            if (result is Resource.Success) {
                _messages.update { current ->
                    current.toMutableMap().apply {
                        put(chatId, result.data)
                    }
                }
            } else if (result is Resource.Error) {
                Log.e("ChatError", result.message)
            }

            _loadingMessages.update { it - chatId }
        }
    }

    private fun handleIncomingMessage(message: ChatMessage) {
        val senderId = message.sender.id
        val myId = currentUserId ?: return

        if (message.sender.id == myId || message.receiver.id == myId) {
            addMessageToChat(message)
        }

        val currentActiveChat = _activeChatUserId.value
        if (senderId != myId && senderId != currentActiveChat) {
            triggerNotification("Message from ${message.sender.name}", message.content)
        }
    }

    private fun addMessageToChat(message: ChatMessage) {
        val chatId = message.chatId

        _messages.update { current ->
            val existingMessages = current[chatId] ?: emptyList()
            val updatedList = if (existingMessages.any { it.id == message.id }) {
                existingMessages
            } else {
                existingMessages + message
            }

            current.toMutableMap().apply {
                put(chatId, updatedList)
            }
        }

        _chatList.update { state ->
            val current = (state as? Resource.Success)?.data ?: return@update state

            val isExisting = current.any { it.id == chatId }

            if (isExisting) {
                Resource.Success(
                    current.map {
                        if (it.id == chatId) {
                            it.copy(
                                lastMessage = message.content,
                                lastUpdated = message.timestamp,
                                lastMessageStatus = if (message.sender.id == message.receiver.id) it.lastMessageStatus else "DELIVERED"
                            )
                        } else it
                    }
                )
            } else {
                val newChat = Chat(
                    id = message.chatId,
                    user1 = message.sender,
                    user2 = message.receiver,
                    lastMessage = message.content,
                    lastUpdated = message.timestamp,
                    lastMessageStatus = "DELIVERED",
                    companyCode = message.companyCode,
                    lastMessageType = message.messageType,
                    lastMessageSender = message.sender.id
                )
                Resource.Success((current + newChat))
            }
        }
    }

    fun markMessagesAsSeen(chatId: String, userId: String) {
        viewModelScope.launch {
            _seenStatus.value = Resource.Loading()
            val result = chatRepository.markChatAsSeen(chatId, userId)
            _seenStatus.value = result
        }
    }

    fun clearNotificationEvent() {
        _notificationEvent.value = null
    }

    fun clearChatCache() {
        _messages.value = emptyMap()
    }

    override fun onCleared() {
        webSocketClient?.disconnect()
        super.onCleared()
    }
}
