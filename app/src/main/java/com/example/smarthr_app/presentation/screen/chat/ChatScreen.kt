package com.example.smarthr_app.presentation.screen.chat

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
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
    val allMessages by chatViewModel.messages.collectAsState()
    val chatList by chatViewModel.chatList.collectAsState()
    val user = authViewModel.user.collectAsState(initial = null).value ?: return
    val coroutineScope = rememberCoroutineScope()


    val userId = user.userId
    val companyCode = user.companyCode

    val chatId = chatList?.find {
        it.user1.id == receiverId || it.user2.id == receiverId
    }?.id

    LaunchedEffect(receiverId) {
        chatViewModel.setActiveChatUser(receiverId)
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.setActiveChatUser(null)
        }
    }

    val listState = rememberLazyListState()
    val chatMessages = allMessages[chatId] ?: emptyList()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(chatId) {
        chatId?.let {
            chatViewModel.sendSeenMessageInfo(chatId = chatId, userId = user.userId)
        }
    }

    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(chatId, Unit) {
        chatId?.let {
            chatViewModel.getChatBetweenUsers(
                chatId = chatId,
                companyCode = companyCode!!,
                otherUserId = receiverId
            )
            chatViewModel.markMessagesAsSeen(chatId, userId)
        }
    }

    LaunchedEffect(chatMessages.size) {
        chatId?.let {
            chatViewModel.sendSeenMessageInfo(it, userId)
        }
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        // Top bar with Online status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = goToBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Image(
                painter = rememberAsyncImagePainter(model = Uri.decode(imageUrl)),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
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

        Divider(color = Color.LightGray, thickness = 1.dp)

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true
        ) {
            items(chatMessages.reversed(), key = { it.id }) { message ->
                Log.i("Messagexyz", message.content)
                ChatBubble(message, isFromMe = message.sender.id == userId)
            }
        }

        // Input box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(Color.White, RoundedCornerShape(24.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            IconButton(
                onClick = {
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
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF7E57C2))
            }
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
                    color = if (isFromMe) Color(0xFF7E57C2) else Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column {
                Text(
                    text = message.content,
                    fontSize = 16.sp,
                    color = if (isFromMe) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp.toReadableTime(),
                        fontSize = 11.sp,
                        color = Color.LightGray
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