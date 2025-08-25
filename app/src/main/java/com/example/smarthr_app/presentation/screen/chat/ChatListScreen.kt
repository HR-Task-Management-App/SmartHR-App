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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.Chat
import com.example.smarthr_app.presentation.components.CompanyLockScreen
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.toReadableTime

@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    onNavigateToUserListScreen: () -> Unit,
    onNavigateChatScreen: (String, String, String) -> Unit,
    goToBack: () -> Unit,
    onNavigateToCompanyManagement: () -> Unit
) {
    val chatListResource by chatViewModel.chatList.collectAsState()
    val user by authViewModel.user.collectAsState(initial = null)

    // Check if user has joined a company
    val hasJoinedCompany = !user?.companyCode.isNullOrBlank()
    val isWaitlisted = !user?.waitingCompanyCode.isNullOrBlank()

    LaunchedEffect(Unit) {
        if (hasJoinedCompany) {
            user?.let { currentUser ->
                chatViewModel.initSocket(currentUser.userId)
                chatViewModel.getMyChatList(currentUser.companyCode!!)
            }
        }
    }

    // Show lock screen if user hasn't joined a company
    if (!hasJoinedCompany && !isWaitlisted) {
        CompanyLockScreen(
            title = "Chat Feature Locked",
            onJoinCompanyClick = onNavigateToCompanyManagement
        )
        return
    }

    // Show waiting message if user is waitlisted
    if (isWaitlisted && !hasJoinedCompany) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Waiting for approval",
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Waiting for HR Approval",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your request to join ${user?.waitingCompanyCode ?: ""} is pending. HR will review your request soon.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }
        }
        return
    }

    // Normal chat interface (only show if user has joined company)
    Box(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        Column {
            // Top Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = goToBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Chat content
            when (chatListResource) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    val chatList = (chatListResource as Resource.Success).data
                    if (chatList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No chats found", color = Color.Gray)
                        }
                    } else {
                        LazyColumn {
                            items(chatList) { chatItem ->
                                ChatListRow(chatItem, onNavigateChatScreen, chatViewModel)
                                HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 0.3.dp)
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    val message = (chatListResource as Resource.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: $message", color = Color.Red)
                    }
                }

                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No chats found", color = Color.Gray)
                    }
                }
            }
        }

        // Floating Button (only show if user has joined company)
        FloatingActionButton(
            onClick = { onNavigateToUserListScreen() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = PrimaryPurple,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Chat")
        }
    }


        // Show transparent overlay based on company status
        if (!hasJoinedCompany && !isWaitlisted) {
            CompanyLockScreen(
                title = "Chat Feature Locked",
                onJoinCompanyClick = onNavigateToCompanyManagement
            )
        } else if (isWaitlisted && !hasJoinedCompany) {
            WaitingApprovalOverlay(
                companyCode = user?.waitingCompanyCode ?: ""
            )
        }
    }

@Composable
fun WaitingApprovalOverlay(
    companyCode: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)), // Black background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Waiting for approval",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFFF9800) // Keep orange color for waiting icon
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Waiting for HR Approval",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White, // White text
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your request to join $companyCode is pending. HR will review your request soon.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f), // White text with slight transparency
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
fun ChatListRow(chatItem: Chat, onNavigateChatScreen: (String, String, String) -> Unit, chatViewModel: ChatViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onNavigateChatScreen(
                        chatItem.user2.id,
                        Uri.encode(chatItem.user2.imageUrl ?: "https://cdn.pixabay.com/photo/2023/02/18/11/00/icon-7797704_1280.png"),
                        chatItem.user2.name
                    )
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
                    val tickIcon = when (chatItem.lastMessageStatus) {
                        "SEEN" -> Icons.Default.DoneAll
                        "DELIVERED" -> Icons.Default.Done
                        else -> null
                    }

                    tickIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = chatItem.lastMessageStatus,
                            tint = if (chatItem.lastMessageStatus == "SEEN") Color.Blue else Color.Gray,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
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
            if (chatItem.lastMessageSender != chatItem.user1.id && chatItem.lastMessageStatus != "SEEN") {
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
fun TopAppBarContent(
    goToBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable(
            onClick = {
                goToBack()
            }
        ))
        Text("Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(24.dp))
    }
}