package com.example.smarthr_app.presentation.screen.chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.Chat
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.utils.toReadableTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    onNavigateToUserListScreen: () -> Unit,
    onNavigateChatScreen: (String,String,String) -> Unit
) {
    val user by authViewModel.user.collectAsState(initial = null)
    LaunchedEffect(Unit) { //only once called
        user?.let {
            chatViewModel.getMyChatList(companyCode = it.companyCode!!)
        }
    }
    LaunchedEffect(user) {
        user?.let {
            chatViewModel.initSocket(it.userId)
        }
    }


    val chatList = chatViewModel.chatList.collectAsState().value

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column() {
            TopAppBarContent()

            LazyColumn {
                items(chatList ?: emptyList()) { chatItem ->
                    ChatListRow(chatItem = chatItem,onNavigateChatScreen,chatViewModel)
                    HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)
                }
            }
        }



        // Floating Button
        FloatingActionButton(
            onClick = {
                onNavigateToUserListScreen()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF795FFC),
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Chat")
        }
    }
}


@Composable
fun ChatListRow(chatItem: Chat,onNavigateChatScreen:(String,String,String)->Unit,chatViewModel: ChatViewModel) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onNavigateChatScreen(chatItem.user2.id, Uri.encode(chatItem.user2.imageUrl?:"https://cdn.pixabay.com/photo/2023/02/18/11/00/icon-7797704_1280.png"),chatItem.user2.name)
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = chatItem.user2.imageUrl ?: "https://cdn.pixabay.com/photo/2023/02/18/11/00/icon-7797704_1280.png",
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(chatItem.user2.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chatItem.lastMessageSender == chatItem.user1.id) {
                    // Show tick icons only if the sender is user1 (current user)
                    val tickIcon = when (chatItem.lastMessageStatus) {
                        "SEEN" -> Icons.Default.DoneAll // double tick
                        "DELIVERED" -> Icons.Default.Done // single tick
                        else -> null
                    }

                    tickIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = chatItem.lastMessageStatus,
                            tint = if (chatItem.lastMessageStatus == "SEEN") Color.Blue else Color.Gray,
                            modifier = Modifier.size(18.dp).padding(end = 4.dp)
                        )
                    }
                }

                Text(
                    text = chatItem.lastMessage,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }


        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = chatItem.lastUpdated.toReadableTime(),
                color = Color.Gray,
                fontSize = 12.sp
            )
            if (chatItem.lastMessageSender!=chatItem.user1.id && chatItem.lastMessageStatus != "SEEN") {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
            }
        }
    }
}


@Composable
fun TopAppBarContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        Text("Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(24.dp)) // to balance icon
    }
}