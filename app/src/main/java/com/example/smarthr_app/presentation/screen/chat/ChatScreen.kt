package com.example.smarthr_app.presentation.screen.chat

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.toReadableTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    receiverId: String,
    imageUrl: String,
    name: String,
    goToBack: () -> Unit
) {
    val chatListResource by chatViewModel.chatList.collectAsState()
    val allMessages by chatViewModel.messages.collectAsState()
    val user = authViewModel.user.collectAsState(initial = null).value ?: return
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }

    val userId = user.userId
    val companyCode = user.companyCode

    val chatId = (chatListResource as? Resource.Success)?.data?.find {
        it.user1.id == receiverId || it.user2.id == receiverId
    }?.id

    val chatMessages = chatId?.let { allMessages[it] }

    val isFirstTimeLoading = chatId != null && chatMessages == null

    // Set active user
    LaunchedEffect(receiverId) {
        chatViewModel.setActiveChatUser(receiverId)
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.setActiveChatUser(null)
        }
    }

    // Scroll to bottom when new messages
    LaunchedEffect(chatMessages?.size) {
        if (!chatMessages.isNullOrEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Fetch messages
    LaunchedEffect(chatId) {
        chatId?.let {
            chatViewModel.getChatBetweenUsers(
                chatId = it,
                companyCode = companyCode!!,
                otherUserId = receiverId
            )
            chatViewModel.markMessagesAsSeen(it, userId)
            chatViewModel.sendSeenMessageInfo(it, userId)
        }
    }

    LaunchedEffect(chatMessages?.size) {
        chatId?.let {
            chatViewModel.sendSeenMessageInfo(it, userId)
        }
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        ChatTopBar(imageUrl = imageUrl, name = name, onBack = goToBack)
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

        when {
            isFirstTimeLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            chatListResource is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading chat", color = Color.Red)
                }
            }

            chatId == null -> {
                // No existing chat
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Start a conversation with $name",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    MessageInputBox(
                        messageText = messageText,
                        onMessageChange = { messageText = it },
                        onSend = {
                            if (messageText.isNotBlank()) {
                                chatViewModel.sendMessage(
                                    senderId = userId,
                                    receiverId = receiverId,
                                    content = messageText,
                                    companyCode = companyCode!!
                                )
                                messageText = ""
                            }
                        }
                    )
                }
            }

            else -> {
                // Chat exists
                val nonNullMessages = chatMessages ?: emptyList()
                Column(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        reverseLayout = true
                    ) {
                        items(nonNullMessages.reversed(), key = { it.id }) { message ->
                            ChatBubble(message = message, isFromMe = message.sender.id == userId)
                        }
                    }

                    MessageInputBox(
                        messageText = messageText,
                        onMessageChange = { messageText = it },
                        onSend = {
                            if (messageText.isNotBlank()) {
                                chatViewModel.sendMessage(
                                    senderId = userId,
                                    receiverId = receiverId,
                                    content = messageText,
                                    companyCode = companyCode!!
                                )
                                messageText = ""
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBox(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color.White, RoundedCornerShape(24.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            placeholder = { Text("Type a message...") },
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(color = Color.Black),
            singleLine = true
        )
        IconButton(onClick = onSend) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color(0xFF7E57C2)
            )
        }
    }
}


@Composable
fun ChatTopBar(imageUrl: String, name: String, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Image(
            painter = rememberAsyncImagePainter(model = Uri.decode(imageUrl)),
            contentDescription = "User Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isFromMe: Boolean) {
    val bubbleColor = if (isFromMe) Color(0xFF7E57C2) else Color.White
    val textColor = if (isFromMe) Color.White else Color.Black
    val alignment = if (isFromMe) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isFromMe) Color(0xFF7E57C2) else Color(0xFFEDE7F6) // Light Blue (for receiver)
                    ,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(top = 2.dp, bottom = 0.dp, start = 10.dp, end = 10.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isFromMe) Color.White else Color.Black
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp.toReadableTime(),
                        fontSize = 11.sp,
                        color = if(isFromMe) Color(0xFFB0BEC5) else Color.Gray,
                        modifier = Modifier.offset(y = (-5).dp)

                    )
                    if (isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = when (message.messageStatus) {
                                "SEEN" -> Icons.Default.DoneAll
                                else -> Icons.Default.Done
                            },
                            contentDescription = message.messageStatus,
                            tint = if (message.messageStatus == "SEEN") Color.Blue else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}