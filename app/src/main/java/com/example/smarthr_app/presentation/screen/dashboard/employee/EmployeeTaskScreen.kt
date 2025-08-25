package com.example.smarthr_app.presentation.screen.dashboard.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.presentation.components.CompanyLockScreen
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.presentation.viewmodel.TaskViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeTaskScreen(
    taskViewModel: TaskViewModel,
    authViewModel: AuthViewModel, // Add AuthViewModel
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCompanyManagement: () -> Unit // Add navigation to company management
) {
    val context = LocalContext.current
    val tasksState by taskViewModel.tasksState.collectAsState(initial = null)
    val user by authViewModel.user.collectAsState(initial = null)

    var selectedFilter by remember { mutableStateOf("All") }

    // Check if user has joined a company
    val hasJoinedCompany = !user?.companyCode.isNullOrBlank()
    val isWaitlisted = !user?.waitingCompanyCode.isNullOrBlank()

    LaunchedEffect(Unit) {
        if (hasJoinedCompany) {
            taskViewModel.loadUserTasks()
        }
    }

    // Show lock screen if user hasn't joined a company
    if (!hasJoinedCompany && !isWaitlisted) {
        CompanyLockScreen(
            title = "Tasks Feature Locked",
            onJoinCompanyClick = onNavigateToCompanyManagement
        )
        return
    }

    // Show waiting message if user is waitlisted
    if (isWaitlisted && !hasJoinedCompany) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your request to join ${user?.waitingCompanyCode} is pending. HR will review your request soon.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp)
    ) {
        // Header
        Text(
            text = "My Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Tabs
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Pending", "Completed")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(filter)
                        if (filter != "All") {
                            val count = when (filter) {
                                "Pending" -> (tasksState as? Resource.Success)?.data?.count {
                                    it.status == TaskStatus.NOT_STARTED || it.status == TaskStatus.IN_PROGRESS
                                } ?: 0
                                "Completed" -> (tasksState as? Resource.Success)?.data?.count {
                                    it.status == TaskStatus.FINISHED
                                } ?: 0
                                else -> 0
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (selectedFilter == filter) Color.White else PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selectedFilter == filter) PrimaryPurple else Color.White
                                    )
                                }
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks Content
        when (val currentTasksState = tasksState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            is Resource.Success -> {
                val filteredTasks = when (selectedFilter) {
                    "Pending" -> currentTasksState.data.filter {
                        it.status == TaskStatus.NOT_STARTED || it.status == TaskStatus.IN_PROGRESS
                    }
                    "Completed" -> currentTasksState.data.filter { it.status == TaskStatus.FINISHED }
                    else -> currentTasksState.data
                }

                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (selectedFilter == "All") "No tasks assigned yet" else "No $selectedFilter tasks",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTasks) { task ->
                            EmployeeTaskCard(
                                task = task,
                                onClick = { onNavigateToTaskDetail(task.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error loading tasks",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { taskViewModel.loadUserTasks() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            null -> {
                // Initial state
            }
        }
    }
}

// Keep the existing EmployeeTaskCard and other functions unchanged...
@Composable
fun EmployeeTaskCard(
    task: TaskResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with priority and status (keep all 3 statuses for employee view)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (task.status) {
                            TaskStatus.NOT_STARTED -> Color(0xFFE0E0E0)
                            TaskStatus.IN_PROGRESS -> PrimaryPurple
                            TaskStatus.FINISHED -> Color(0xFF4CAF50)
                        }
                    ) {
                        Text(
                            text = when (task.status) {
                                TaskStatus.NOT_STARTED -> "Not Started"
                                TaskStatus.IN_PROGRESS -> "In Progress"
                                TaskStatus.FINISHED -> "Completed"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.status == TaskStatus.NOT_STARTED) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (task.priority) {
                            TaskPriority.LOW -> Color(0xFF4CAF50)
                            TaskPriority.MEDIUM -> Color(0xFFFF9800)
                            TaskPriority.HIGH -> Color(0xFFFF5722)
                            TaskPriority.URGENT -> Color(0xFFE91E63)
                        }
                    ) {
                        Text(
                            text = "⚡ ${task.priority.name}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Task Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show assigned employees with their status
            task.employees?.let { employees ->
                if (employees.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Team Members (${employees.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            employees.take(3).forEach { employee ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mini avatar
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryPurple.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!employee.imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = employee.imageUrl,
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Profile",
                                                modifier = Modifier.size(12.dp),
                                                tint = PrimaryPurple
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Employee name
                                    Text(
                                        text = employee.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )

                                    // Status indicator
                                    Text(
                                        text = when (employee.taskStatus) {
                                            TaskStatus.NOT_STARTED -> "⏳"
                                            TaskStatus.IN_PROGRESS -> "🔄"
                                            TaskStatus.FINISHED -> "✅"
                                            null -> "⏳"
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            if (employees.size > 3) {
                                Text(
                                    text = "+${employees.size - 3} more members",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom section with date and assignee info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(task.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Assigned by ${task.assignee.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress bar for in-progress tasks
            if (task.status == TaskStatus.IN_PROGRESS) {
                Spacer(modifier = Modifier.height(8.dp))
                task.employees?.let { employees ->
                    val progress = if (employees.isNotEmpty()) {
                        employees.count { it.taskStatus == TaskStatus.FINISHED }.toFloat() / employees.size.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryPurple,
                        trackColor = PrimaryPurple.copy(alpha = 0.2f)
                    )

                    Text(
                        text = "${(progress * 100).toInt()}% team progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        "Date"
    }
}