package com.example.smarthr_app.presentation.screen.dashboard.hr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel

data class DashboardCard(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRDashboardScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToLeaves: () -> Unit,
    onNavigateToOfficeLocation: () -> Unit,
    onNavigateToCompanyAttendance: () -> Unit
) {
    val user by authViewModel.user.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        authViewModel.user.collect { currentUser ->
            if (currentUser == null) {
                onLogout()
            }
        }
    }

    val dashboardCards = listOf(
        DashboardCard(
            title = "Manage Employees",
            icon = Icons.Default.Group,
            color = Color(0xFF4CAF50),
            onClick = onNavigateToEmployees
        ),
        DashboardCard(
            title = "Task Management", // Add this card
            icon = Icons.Default.Assignment,
            color = Color(0xFF9C27B0),
            onClick = onNavigateToTasks
        ),
        DashboardCard(
            title = "Leave Management",
            icon = Icons.Default.BeachAccess,
            color = Color(0xFFFF9800),
            onClick = onNavigateToLeaves
        ),
        DashboardCard(
            title = "Attendance Setup",
            icon = Icons.Default.LocationOn,
            color = Color(0xFF2196F3),
            onClick = onNavigateToOfficeLocation
        ),
        DashboardCard(
            title = "Attendance Reports",
            icon = Icons.Default.Schedule,
            color = Color(0xFF4CAF50),
            onClick = onNavigateToCompanyAttendance
        ),
        DashboardCard(
            title = "Meetings",
            icon = Icons.Default.VideoCall,
            color = Color(0xFFE91E63),
            onClick = { /* TODO: Navigate to meetings */ }
        ),
        DashboardCard(
            title = "Reports",
            icon = Icons.Default.Assessment,
            color = Color(0xFF795548),
            onClick = { /* TODO: Navigate to reports */ }
        )
    )

    val cardPairs = dashboardCards.chunked(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
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
                            .clickable { onNavigateToProfile() }, // Made clickable
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = user?.name ?: "HR",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "HR Manager",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
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

        // Scrollable Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Welcome back, ${user?.name?.split(" ")?.first() ?: "HR"}!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Manage your employees and company operations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                // Section Title
                Text(
                    text = "Dashboard Options",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            itemsIndexed(cardPairs) { _, cardPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    cardPair.forEach { card ->
                        DashboardOptionCard(
                            card = card,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (cardPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DashboardOptionCard(
    card: DashboardCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { card.onClick() },
        colors = CardDefaults.cardColors(
            containerColor = card.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = card.title,
                tint = card.color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                color = card.color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}