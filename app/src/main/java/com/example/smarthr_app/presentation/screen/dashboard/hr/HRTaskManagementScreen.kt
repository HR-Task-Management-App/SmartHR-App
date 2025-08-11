package com.example.smarthr_app.presentation.screen.dashboard.hr

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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.TaskViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRTaskManagementScreen(
    taskViewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToEditTask: (String) -> Unit
) {
    val context = LocalContext.current
    val tasksState by taskViewModel.tasksState.collectAsState(initial = null)
    val deleteTaskState by taskViewModel.deleteTaskState.collectAsState(initial = null)
    val updateTaskState by taskViewModel.updateTaskState.collectAsState(initial = null)

    var selectedFilter by remember { mutableStateOf("All") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<TaskResponse?>(null) }
    var showEmployeeStatusDialog by remember { mutableStateOf(false) }
    var selectedTaskForStatus by remember { mutableStateOf<TaskResponse?>(null) }

    LaunchedEffect(Unit) {
        taskViewModel.loadCompanyTasks()
    }

    // Handle delete response
    LaunchedEffect(deleteTaskState) {
        when (val state = deleteTaskState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Task deleted successfully!")
                taskViewModel.clearDeleteTaskState()
                taskViewModel.loadCompanyTasks()
                showDeleteDialog = false
                taskToDelete = null
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                taskViewModel.clearDeleteTaskState()
            }
            else -> {}
        }
    }

    // Handle task status update
    LaunchedEffect(updateTaskState) {
        when (val state = updateTaskState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Task status updated successfully!")
                taskViewModel.clearUpdateTaskState()
                taskViewModel.loadCompanyTasks()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                taskViewModel.clearUpdateTaskState()
            }
            else -> {}
        }
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Task Management",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter Tabs
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "In Progress", "Finished")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(filter)
                        if (filter != "All") {
                            val count = when (filter) {
                                "In Progress" -> (tasksState as? Resource.Success)?.data?.count {
                                    it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.NOT_STARTED
                                } ?: 0
                                "Finished" -> (tasksState as? Resource.Success)?.data?.count {
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

        // Tasks Content
        Box(modifier = Modifier.weight(1f)) {
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
                        "In Progress" -> currentTasksState.data.filter {
                            it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.NOT_STARTED
                        }
                        "Finished" -> currentTasksState.data.filter { it.status == TaskStatus.FINISHED }
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
                                    text = if (selectedFilter == "All") "No tasks created yet" else "No $selectedFilter tasks",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredTasks) { task ->
                                HRTaskCard(
                                    task = task,
                                    onClick = { onNavigateToTaskDetail(task.id) },
                                    onEdit = { onNavigateToEditTask(task.id) },
                                    onDelete = {
                                        taskToDelete = task
                                        showDeleteDialog = true
                                    },
                                    onShowEmployeeStatus = {
                                        selectedTaskForStatus = task
                                        showEmployeeStatusDialog = true
                                    },
                                    onMarkAsFinished = {
                                        taskViewModel.updateTaskStatus(task.id, TaskStatus.FINISHED.name)
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
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
                                onClick = { taskViewModel.loadCompanyTasks() },
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

            // Floating Action Button
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateTask,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Task")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Task")
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                taskToDelete = null
            },
            title = {
                Text(
                    text = "Delete Task",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${taskToDelete?.title}'? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.let { task ->
                            taskViewModel.deleteTask(task.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = deleteTaskState !is Resource.Loading
                ) {
                    if (deleteTaskState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        taskToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Employee Status Dialog
    if (showEmployeeStatusDialog && selectedTaskForStatus != null) {
        EmployeeStatusDialog(
            task = selectedTaskForStatus!!,
            onDismiss = {
                showEmployeeStatusDialog = false
                selectedTaskForStatus = null
            }
        )
    }
}

@Composable
fun HRTaskCard(
    task: TaskResponse,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowEmployeeStatus: () -> Unit,
    onMarkAsFinished: () -> Unit
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
            // Header with priority and menu (removed status badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority only (removed status badge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show only if task is finished
                    if (task.status == TaskStatus.FINISHED) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4CAF50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Finished",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                            text = task.priority.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                // Menu Button
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        if (task.status != TaskStatus.FINISHED) {
                            DropdownMenuItem(
                                text = { Text("Mark as Finished") },
                                onClick = {
                                    showMenu = false
                                    onMarkAsFinished()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
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

            // Show assigned employees with updated status tracking
            task.employees?.let { employees ->
                if (employees.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        onClick = { onShowEmployeeStatus() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = PrimaryPurple
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${employees.size} employees assigned",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val finishedCount = employees.count { it.taskStatus == TaskStatus.FINISHED }
                                val inProgressCount = employees.count { it.taskStatus == TaskStatus.IN_PROGRESS }
                                val notStartedCount = employees.count { it.taskStatus == TaskStatus.NOT_STARTED || it.taskStatus == null }

                                // Show status breakdown
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (finishedCount > 0) {
                                        Text(
                                            text = "✅$finishedCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                    if (inProgressCount > 0) {
                                        Text(
                                            text = "🔄$inProgressCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryPurple
                                        )
                                    }
                                    if (notStartedCount > 0) {
                                        Text(
                                            text = "⏳$notStartedCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "View Details",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom section with date and assignee
            Column {
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

                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
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
                        text = "Created by ${task.assignee.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress bar - only show for in-progress tasks with actual progress
            if (task.status == TaskStatus.IN_PROGRESS) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = task.employees?.let { employees ->
                    if (employees.isNotEmpty()) {
                        employees.count { it.taskStatus == TaskStatus.FINISHED }.toFloat() / employees.size.toFloat()
                    } else 0f
                } ?: 0f

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryPurple,
                    trackColor = PrimaryPurple.copy(alpha = 0.2f)
                )

                Text(
                    text = "${(progress * 100).toInt()}% completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun EmployeeStatusDialog(
    task: TaskResponse,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Employee Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                task.employees?.forEach { employee ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Employee Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!employee.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = employee.imageUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(24.dp),
                                    tint = PrimaryPurple
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Employee Info
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = employee.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = employee.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (employee.taskStatus) {
                                TaskStatus.NOT_STARTED -> Color(0xFFE0E0E0)
                                TaskStatus.IN_PROGRESS -> PrimaryPurple
                                TaskStatus.FINISHED -> Color(0xFF4CAF50)
                                null -> Color(0xFFE0E0E0)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (employee.taskStatus) {
                                        TaskStatus.NOT_STARTED -> Icons.Default.RadioButtonUnchecked
                                        TaskStatus.IN_PROGRESS -> Icons.Default.Schedule
                                        TaskStatus.FINISHED -> Icons.Default.CheckCircle
                                        null -> Icons.Default.RadioButtonUnchecked
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (employee.taskStatus == TaskStatus.NOT_STARTED || employee.taskStatus == null) Color.Black else Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (employee.taskStatus) {
                                        TaskStatus.NOT_STARTED -> "Not Started"
                                        TaskStatus.IN_PROGRESS -> "In Progress"
                                        TaskStatus.FINISHED -> "Finished"
                                        null -> "Not Started"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (employee.taskStatus == TaskStatus.NOT_STARTED || employee.taskStatus == null) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary
                task.employees?.let { employees ->
                    val finishedCount = employees.count { it.taskStatus == TaskStatus.FINISHED }
                    val inProgressCount = employees.count { it.taskStatus == TaskStatus.IN_PROGRESS }
                    val notStartedCount = employees.count { it.taskStatus == TaskStatus.NOT_STARTED || it.taskStatus == null }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Progress Summary",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✅ Completed: $finishedCount",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "🔄 In Progress: $inProgressCount",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "⏳ Not Started: $notStartedCount",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
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