package com.example.smarthr_app.presentation.viewmodel

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.Chat
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.data.model.SeenMessage
import com.example.smarthr_app.data.model.UserInfo
import com.example.smarthr_app.data.repository.ChatRepository
import com.example.smarthr_app.presentation.screen.dashboard.hr.LoadingCard
import com.example.smarthr_app.utils.ChatWebSocketClient
import com.example.smarthr_app.utils.showNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private var webSocketClient: ChatWebSocketClient? = null

    private val _chatList = MutableStateFlow<List<Chat>?>(null)
    val chatList: StateFlow<List<Chat>?> = _chatList

    private val _userList = MutableStateFlow<List<UserInfo>?>(null)
    val userList: StateFlow<List<UserInfo>?> = _userList

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages

    private val _notificationEvent = MutableStateFlow<Pair<String, String>?>(null)
    val notificationEvent: StateFlow<Pair<String, String>?> = _notificationEvent

    private val _activeChatUserId = MutableStateFlow<String?>(null)
    val activeChatUserId: StateFlow<String?> = _activeChatUserId

    fun setActiveChatUser(userId: String?) {
        _activeChatUserId.value = userId
    }

    private fun triggerNotification(title: String, message: String) {
        _notificationEvent.value = title to message
    }



    private var currentUserId: String? = null

    fun initSocket(userId: String) {
        currentUserId = userId
        webSocketClient = ChatWebSocketClient(
            userId = userId,
            onMessageReceived = { incomingMessage ->
                Log.d("IncomingMessage", incomingMessage.toString())
                handleIncomingMessage(incomingMessage)
            },
            handleSeenMessage = { seenMessage ->
                Log.d("SeenMessage", seenMessage.toString())
                handleSeenMessage(seenMessage)  // Implement this in your ViewModel or wherever appropriate
            }
        )

        webSocketClient?.connect()
    }

    fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        companyCode: String,
        type: String = "TEXT"
    ) {
        webSocketClient?.sendMessage(senderId, receiverId, content, companyCode, type)
    }

    fun sendSeenMessageInfo(
        chatId:String,
        userId: String
    ){
        webSocketClient?.sendSeenMessageInfo(chatId,userId);
    }


    private fun handleSeenMessage(seen: SeenMessage) {
        val chatId = seen.chatId
        _messages.update { map ->
            map.mapValues { (_, list) ->
                list.map {
                    if (it.chatId == chatId && it.sender.id == currentUserId /* me! */)
                        it.copy(messageStatus = "SEEN")
                    else it
                }
            }
        }
        _chatList.update { list ->
            list?.map {
                if (it.id == chatId && it.lastMessageSender == currentUserId)
                    it.copy(lastMessageStatus = "SEEN")
                else it
            }
        }
    }




    override fun onCleared() {
        webSocketClient?.disconnect()
        super.onCleared()
    }

    fun getMyChatList(companyCode: String) {
        viewModelScope.launch {
            _chatList.value = chatRepository.getMyChatList(companyCode)
        }
    }

    fun getAllUser() {
        viewModelScope.launch {
            _userList.value = chatRepository.getAllUsers()
        }
    }

    fun getChatBetweenUsers(chatId: String, companyCode: String, otherUserId: String) {
        viewModelScope.launch {
            val messages = chatRepository.getChatBetweenUser(companyCode, otherUserId) ?: emptyList()
            _messages.update { current ->
                current.toMutableMap().apply {
                    put(chatId, messages)
                }
            }
        }
    }

    private fun handleIncomingMessage(message: ChatMessage) {
        val currentActiveChat = _activeChatUserId.value
        val senderId = message.sender.id
        val myId = currentUserId ?: return
        if (message.sender.id == myId || message.receiver.id == myId) {
            addMessageToChat(message)
        }
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

            Log.d("ChatViewModel", "Adding message to chatId: $chatId ${message.content} | Total messages: ${updatedList.size}")

            current.toMutableMap().apply {
                put(chatId, updatedList)
            }
        }

        _chatList.update { currentList ->
            val isExisting = currentList?.any { it.id == chatId } ?: false

            if (isExisting) {
                currentList.map {
                    if (it.id == chatId) {
                        it.copy(
                            lastMessage = message.content,
                            lastUpdated = message.timestamp,
                            lastMessageStatus = if (message.sender.id == message.receiver.id) it.lastMessageStatus else "DELIVERED"
                        )
                    } else it
                }
            } else {
                // If new chat (first-time), add a new Chat object
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
                (currentList ?: emptyList()) + newChat
            }
        }

    }


    fun markMessagesAsSeen(chatId: String, userId: String) {

        viewModelScope.launch {
            try {
                // Notify backend to mark up to this message as SEEN
                chatRepository.markChatAsSeen(chatId, userId)
            } catch (e: Exception) {
                Log.e("ChatSeen", "Failed to mark messages as seen", e)
            }
        }
    }


    fun clearChatCache() {
        _messages.value = emptyMap()
    }

    fun clearNotificationEvent() {
        _notificationEvent.value = null
    }

}
