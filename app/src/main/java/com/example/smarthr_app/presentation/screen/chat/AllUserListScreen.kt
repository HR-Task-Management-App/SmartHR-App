package com.example.smarthr_app.presentation.screen.chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.UserInfo
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel

@Composable
fun AllUserListScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    goToBack:()-> Unit,
    onNavigateToChatScreen: (String,String,String) -> Unit
) {
    val user by authViewModel.user.collectAsState(initial = null)
    val allUsers by chatViewModel.userList.collectAsState()

    LaunchedEffect(Unit) {
        chatViewModel.getAllUser()
    }

    val filteredUsers = remember(user, allUsers) {
        allUsers?.filter { it.id != user?.userId } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = goToBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Select Contact",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // User List
        LazyColumn {
            items(filteredUsers) { user ->
                UserListItem(user = user, onClick = {
                    onNavigateToChatScreen(user.id, Uri.encode(user.imageUrl?:"https://cdn.pixabay.com/photo/2023/02/18/11/00/icon-7797704_1280.png"),user.name)
                })
                HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)
            }
        }
    }
}


@Composable
fun UserListItem(user: UserInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.imageUrl ?: "https://cdn.pixabay.com/photo/2023/02/18/11/00/icon-7797704_1280.png",
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(user.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

