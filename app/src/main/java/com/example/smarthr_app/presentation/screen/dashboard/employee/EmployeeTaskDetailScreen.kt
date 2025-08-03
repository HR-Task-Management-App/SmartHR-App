package com.example.smarthr_app.presentation.screen.dashboard.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.CommentResponse
import com.example.smarthr_app.data.model.TaskPriority
import com.example.smarthr_app.data.model.TaskResponse
import com.example.smarthr_app.data.model.TaskStatus
import com.example.smarthr_app.presentation.screen.dashboard.hr.TaskDescriptionCard
import com.example.smarthr_app.presentation.screen.dashboard.hr.TaskImageCard
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.TaskViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeTaskDetailScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val taskDetailState by taskViewModel.taskDetailState.collectAsState(initial = null)
    val commentsState by taskViewModel.commentsState.collectAsState(initial = null)
    val addCommentState by taskViewModel.addCommentState.collectAsState(initial = null)
    val updateTaskState by taskViewModel.updateTaskState.collectAsState(initial = null)

    var commentText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<TaskStatus?>(null) }
    var expandedStatus by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        taskViewModel.loadTaskById(taskId)
        taskViewModel.loadTaskComments(taskId)
    }

    // Initialize status from task data
    LaunchedEffect(taskDetailState) {
        if (taskDetailState is Resource.Success) {
            selectedStatus = (taskDetailState as Resource.Success).data.status
        }
    }

    // Handle add comment response
    LaunchedEffect(addCommentState) {
        when (val state = addCommentState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Comment added successfully!")
                taskViewModel.clearAddCommentState()
                taskViewModel.loadTaskComments(taskId)
                commentText = ""
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                taskViewModel.clearAddCommentState()
            }
            else -> {}
        }
    }

    // Handle status update response
    LaunchedEffect(updateTaskState) {
        when (val state = updateTaskState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Status updated successfully!")
                taskViewModel.clearUpdateTaskState()
                taskViewModel.loadTaskById(taskId) // Refresh task data
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                taskViewModel.clearUpdateTaskState()
                // Reset status to original value
                if (taskDetailState is Resource.Success) {
                    selectedStatus = (taskDetailState as Resource.Success).data.status
                }
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
                    text = "Task Details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        when (val currentTaskState = taskDetailState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            is Resource.Success -> {
                val task = currentTaskState.data

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Task Header Card
                        EmployeeTaskHeaderCard(task = task)
                    }

                    item {
                        // Status Update Card
                        StatusUpdateCard(
                            currentStatus = selectedStatus,
                            onStatusChange = { newStatus ->
                                selectedStatus = newStatus
                                taskViewModel.updateTaskStatus(taskId, newStatus.name)
                            },
                            isUpdating = updateTaskState is Resource.Loading,
                            expandedStatus = expandedStatus,
                            onExpandedChange = { expandedStatus = it }
                        )
                    }

                    item {
                        // Task Image (if available)
                        if (!task.imageUrl.isNullOrBlank()) {
                            TaskImageCard(imageUrl = task.imageUrl)
                        }
                    }

                    item {
                        // Task Description Card
                        TaskDescriptionCard(task = task)
                    }

                    item {
                        // Assigned Employees Card (mock data since endpoint doesn't return this)
                        AssignedEmployeesCard()
                    }

                    item {
                        // Comments Section
                        EmployeeCommentsSection(
                            taskId = taskId,
                            commentsState = commentsState,
                            commentText = commentText,
                            onCommentTextChange = { commentText = it },
                            onAddComment = {
                                if (commentText.isNotBlank()) {
                                    taskViewModel.addComment(taskId, commentText.trim())
                                }
                            },
                            isAddingComment = addCommentState is Resource.Loading
                        )
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
                            text = "Error loading task",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { taskViewModel.loadTaskById(taskId) },
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

@Composable
fun EmployeeTaskHeaderCard(task: TaskResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Priority Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (task.priority) {
                        TaskPriority.LOW -> Color(0xFF4CAF50)
                        TaskPriority.MEDIUM -> Color(0xFFFF9800)
                        TaskPriority.HIGH -> Color(0xFFFF5722)
                        TaskPriority.URGENT -> Color(0xFFE91E63)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (task.priority) {
                                TaskPriority.LOW -> Icons.Default.KeyboardArrowDown
                                TaskPriority.MEDIUM -> Icons.Default.Remove
                                TaskPriority.HIGH -> Icons.Default.KeyboardArrowUp
                                TaskPriority.URGENT -> Icons.Default.PriorityHigh
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.priority.name} Priority",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Task Meta Information
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Assigned by HR",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                    text = "Created: ${formatFullDate(task.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (task.updatedAt != task.createdAt) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last updated: ${formatFullDate(task.updatedAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateCard(
    currentStatus: TaskStatus?,
    onStatusChange: (TaskStatus) -> Unit,
    isUpdating: Boolean,
    expandedStatus: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Update Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Change the status of this task to keep your team updated on your progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = onExpandedChange
            ) {
                OutlinedTextField(
                    value = when (currentStatus) {
                        TaskStatus.NOT_STARTED -> "Not Started"
                        TaskStatus.IN_PROGRESS -> "In Progress"
                        TaskStatus.FINISHED -> "Completed"
                        null -> ""
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Current Status") },
                    trailingIcon = {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryPurple
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandedStatus
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (currentStatus) {
                                TaskStatus.NOT_STARTED -> Icons.Default.RadioButtonUnchecked
                                TaskStatus.IN_PROGRESS -> Icons.Default.Schedule
                                TaskStatus.FINISHED -> Icons.Default.CheckCircle
                                null -> Icons.Default.Help
                            },
                            contentDescription = null,
                            tint = when (currentStatus) {
                                TaskStatus.NOT_STARTED -> Color(0xFF757575)
                                TaskStatus.IN_PROGRESS -> PrimaryPurple
                                TaskStatus.FINISHED -> Color(0xFF4CAF50)
                                null -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    ),
                    enabled = !isUpdating
                )

                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    TaskStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (status) {
                                            TaskStatus.NOT_STARTED -> Icons.Default.RadioButtonUnchecked
                                            TaskStatus.IN_PROGRESS -> Icons.Default.Schedule
                                            TaskStatus.FINISHED -> Icons.Default.CheckCircle
                                        },
                                        contentDescription = null,
                                        tint = when (status) {
                                            TaskStatus.NOT_STARTED -> Color(0xFF757575)
                                            TaskStatus.IN_PROGRESS -> PrimaryPurple
                                            TaskStatus.FINISHED -> Color(0xFF4CAF50)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = when (status) {
                                                TaskStatus.NOT_STARTED -> "Not Started"
                                                TaskStatus.IN_PROGRESS -> "In Progress"
                                                TaskStatus.FINISHED -> "Completed"
                                            },
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = when (status) {
                                                TaskStatus.NOT_STARTED -> "Haven't started working on this task"
                                                TaskStatus.IN_PROGRESS -> "Currently working on this task"
                                                TaskStatus.FINISHED -> "Task has been completed"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onStatusChange(status)
                                onExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssignedEmployeesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Team Members",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Other employees assigned to this task:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mock employee avatars
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Team Member",
                                modifier = Modifier.size(24.dp),
                                tint = PrimaryPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Employee ${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeCommentsSection(
    taskId: String,
    commentsState: Resource<List<CommentResponse>>?,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onAddComment: () -> Unit,
    isAddingComment: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "Discussion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )

                when (commentsState) {
                    is Resource.Success -> {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryPurple.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${commentsState.data.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Comment Section
            Column {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    placeholder = { Text("Share your thoughts or ask questions...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAddComment,
                        enabled = commentText.isNotBlank() && !isAddingComment,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        if (isAddingComment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Post Comment")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comments List
            when (commentsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                }

                is Resource.Success -> {
                    if (commentsState.data.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No comments yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Be the first to start the discussion!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        commentsState.data.forEach { comment ->
                            CommentItem(comment = comment)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                is Resource.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Error loading comments",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                null -> {
                    // Initial state
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryPurple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (!comment.author.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = comment.author.imageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(20.dp),
                    tint = PrimaryPurple
                )
            }
        }

        // Comment Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.author.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatTimeAgo(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

private fun formatFullDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun formatTimeAgo(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val now = Date()
        val diffInMillis = now.time - (date?.time ?: 0)

        when {
            diffInMillis < 60 * 1000 -> "Just now"
            diffInMillis < 60 * 60 * 1000 -> "${diffInMillis / (60 * 1000)}m ago"
            diffInMillis < 24 * 60 * 60 * 1000 -> "${diffInMillis / (60 * 60 * 1000)}h ago"
            else -> "${diffInMillis / (24 * 60 * 60 * 1000)}d ago"
        }
    } catch (e: Exception) {
        "Recently"
    }
}