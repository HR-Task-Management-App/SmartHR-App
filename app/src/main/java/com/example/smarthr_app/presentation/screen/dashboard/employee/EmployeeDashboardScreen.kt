package com.example.smarthr_app.presentation.screen.dashboard.employee

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.smarthr_app.R
import com.example.smarthr_app.data.model.User
import com.example.smarthr_app.presentation.components.CompanyLockScreen
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AttendanceViewModel
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.ChatViewModel
import com.example.smarthr_app.presentation.viewmodel.LeaveViewModel
import com.example.smarthr_app.presentation.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    leaveViewModel: LeaveViewModel,
    attendanceViewModel: AttendanceViewModel,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToChatList: () -> Unit,
    onNavigateToMeetings: () -> Unit,
    onNavigateToCompanyManagement: () -> Unit
) {
    val user by authViewModel.user.collectAsState(initial = null)
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        authViewModel.user.collect { currentUser ->
            if (currentUser == null) {
                onLogout()
            }
        }
    }

    LaunchedEffect(user) {
        user?.let {
            if (!it.companyCode.isNullOrBlank()) {
                chatViewModel.initSocket(it.userId)
            }
        }
    }

    // Check if user has joined a company
    val hasJoinedCompany = !user?.companyCode.isNullOrBlank()
    val isWaitlisted = !user?.waitingCompanyCode.isNullOrBlank()

    val tabs = listOf("Home", "Attendance", "Tasks", "Leave")

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Home, contentDescription = tab)
                                1 -> Icon(Icons.Default.Schedule, contentDescription = tab)
                                2 -> Icon(Icons.Default.Assignment, contentDescription = tab)
                                3 -> Icon(Icons.Default.BeachAccess, contentDescription = tab)
                            }
                        },
                        label = { Text(tab) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            indicatorColor = PrimaryPurple.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTabIndex) {
                0 -> HomeTab(
                    user = user,
                    authViewModel = authViewModel,
                    chatViewModel = chatViewModel,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToMeetings = onNavigateToMeetings,
                    onNavigateToChatList = onNavigateToChatList,
                    onNavigateToCompanyManagement = onNavigateToCompanyManagement
                )
                1 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Always show the actual attendance screen content
                        EmployeeAttendanceScreen(
                            attendanceViewModel = attendanceViewModel,
                            authViewModel = authViewModel,
                            onNavigateToCompanyManagement = onNavigateToCompanyManagement
                        )

                        // Show transparent lock screen overlay if not joined
                        if (!hasJoinedCompany && !isWaitlisted) {
                            CompanyLockScreen(
                                title = "Attendance Feature Locked",
                                onJoinCompanyClick = onNavigateToCompanyManagement
                            )
                        } else if (isWaitlisted && !hasJoinedCompany) {
                            // Show waiting overlay
                            WaitingApprovalOverlay(
                                companyCode = user?.waitingCompanyCode ?: ""
                            )
                        }
                    }
                }
                2 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Always show the actual task screen content
                        EmployeeTaskScreen(
                            taskViewModel = taskViewModel,
                            authViewModel = authViewModel,
                            onNavigateToTaskDetail = onNavigateToTaskDetail,
                            onNavigateToCompanyManagement = onNavigateToCompanyManagement
                        )

                        // Show transparent lock screen overlay if not joined
                        if (!hasJoinedCompany && !isWaitlisted) {
                            CompanyLockScreen(
                                title = "Tasks Feature Locked",
                                onJoinCompanyClick = onNavigateToCompanyManagement
                            )
                        } else if (isWaitlisted && !hasJoinedCompany) {
                            // Show waiting overlay
                            WaitingApprovalOverlay(
                                companyCode = user?.waitingCompanyCode ?: ""
                            )
                        }
                    }
                }
                3 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Always show the actual leave screen content
                        EmployeeLeaveScreen(
                            leaveViewModel = leaveViewModel,
                            authViewModel = authViewModel,
                            onNavigateToCompanyManagement = onNavigateToCompanyManagement
                        )

                        // Show transparent lock screen overlay if not joined
                        if (!hasJoinedCompany && !isWaitlisted) {
                            CompanyLockScreen(
                                title = "Leave Feature Locked",
                                onJoinCompanyClick = onNavigateToCompanyManagement
                            )
                        } else if (isWaitlisted && !hasJoinedCompany) {
                            // Show waiting overlay
                            WaitingApprovalOverlay(
                                companyCode = user?.waitingCompanyCode ?: ""
                            )
                        }
                    }
                }
            }
        }
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
fun HomeTab(
    user: User?,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToMeetings: () -> Unit,
    onNavigateToChatList: () -> Unit,
    onNavigateToCompanyManagement: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Section with Profile and Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(top = 8.dp)
            ) {
                // Profile Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            val imageUrl = user?.imageUrl
                            if (!imageUrl.isNullOrBlank()) {
                                SubcomposeAsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Profile image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    loading = {
                                        val composition by rememberLottieComposition(
                                            LottieCompositionSpec.RawRes(R.raw.image_loading)
                                        )
                                        val progress by animateLottieCompositionAsState(composition)
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            LottieAnimation(
                                                composition = composition,
                                                progress = { progress },
                                                modifier = Modifier.size(36.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    },
                                    error = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Default profile icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default profile icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = user?.name ?: "Employee",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (!user?.companyCode.isNullOrBlank())
                                    "Employee - ${user?.companyCode}"
                                else if (!user?.waitingCompanyCode.isNullOrBlank())
                                    "Waiting for approval"
                                else
                                    "No company assigned",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Action buttons - ALWAYS SHOW CHAT AND LOGOUT
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chat button - ALWAYS SHOW (will have lock screen inside if needed)
                        IconButton(
                            onClick = {
                                onNavigateToChatList()
                                if (!user?.companyCode.isNullOrBlank()) {
                                    chatViewModel.getMyChatList(companyCode = user.companyCode)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                authViewModel.logout()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Company Management Card - show if user needs to join company
        if (user?.companyCode.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToCompanyManagement() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Company Management",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFFF9800)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!user?.waitingCompanyCode.isNullOrBlank())
                                "Company Request Pending"
                            else
                                "Join a Company",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = if (!user?.waitingCompanyCode.isNullOrBlank())
                                "Your request to join ${user?.waitingCompanyCode} is pending"
                            else
                                "Join a company to access all features",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to Company Management",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Meetings Card - ALWAYS SHOW (will have lock screen inside if needed)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToMeetings() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = "Meetings",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFE91E63)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scheduled Meetings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                    Text(
                        text = "View your upcoming and past meetings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go to Meetings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}