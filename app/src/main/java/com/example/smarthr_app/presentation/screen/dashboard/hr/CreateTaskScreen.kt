package com.example.smarthr_app.presentation.screen.dashboard.hr

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.CompanyViewModel
import com.example.smarthr_app.presentation.viewmodel.TaskViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    taskViewModel: TaskViewModel,
    companyViewModel: CompanyViewModel,
    taskId: String? = null, // For editing
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val createTaskState by taskViewModel.createTaskState.collectAsState(initial = null)
    val updateTaskState by taskViewModel.updateTaskState.collectAsState(initial = null)
    val taskDetailState by taskViewModel.taskDetailState.collectAsState(initial = null)
    val approvedEmployees by companyViewModel.approvedEmployees.collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf<TaskPriority?>(null) }
    var selectedEmployees by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var expandedPriority by remember { mutableStateOf(false) }
    var showEmployeeSelector by remember { mutableStateOf(false) }

    val isEditing = taskId != null

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Load data on screen load
    LaunchedEffect(Unit) {
        companyViewModel.loadApprovedEmployees()
        if (isEditing && taskId != null) {
            taskViewModel.loadTaskById(taskId)
        } else {
        }
    }

    // Load existing task data for editing
    LaunchedEffect(taskDetailState) {
        if (isEditing) {
            when (val state = taskDetailState) {
                is Resource.Success -> {
                    val task = state.data
                    title = task.title
                    description = task.description
                    selectedPriority = task.priority
                }
                else -> {}
            }
        }
    }

    // Handle create/update response
    LaunchedEffect(createTaskState) {
        when (val state = createTaskState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Task created successfully!")
                taskViewModel.clearCreateTaskState()
                onNavigateBack()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                taskViewModel.clearCreateTaskState()
            }
            else -> {}
        }
    }

    LaunchedEffect(updateTaskState) {
        when (val state = updateTaskState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Task updated successfully!")
                taskViewModel.clearUpdateTaskState()
                onNavigateBack()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                taskViewModel.clearUpdateTaskState()
            }
            else -> {}
        }
    }

    fun handleSaveTask() {
        when {
            title.isBlank() -> {
                ToastHelper.showErrorToast(context, "Please enter a title")
                return
            }
            description.isBlank() -> {
                ToastHelper.showErrorToast(context, "Please enter a description")
                return
            }
            selectedPriority == null -> {
                ToastHelper.showErrorToast(context, "Please select a priority")
                return
            }
            selectedEmployees.isEmpty() -> {
                ToastHelper.showErrorToast(context, "Please assign at least one employee")
                return
            }
        }

        if (isEditing && taskId != null) {
            taskViewModel.updateTask(
                taskId = taskId,
                title = title.trim(),
                description = description.trim(),
                priority = selectedPriority!!.name,
                status = TaskStatus.NOT_STARTED.name,
                employees = selectedEmployees.toList()
            )
        } else {
            taskViewModel.createTask(
                context = context,
                title = title.trim(),
                description = description.trim(),
                priority = selectedPriority!!.name,
                status = TaskStatus.NOT_STARTED.name,
                employees = selectedEmployees.toList(),
                imageUri = selectedImageUri
            )
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
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
                        text = if (isEditing) "Edit Task" else "Create Task",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = { handleSaveTask() },
                    enabled = (createTaskState !is Resource.Loading && updateTaskState !is Resource.Loading)
                ) {
                    if (createTaskState is Resource.Loading || updateTaskState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (isEditing) "Update" else "Create",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Task Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Priority Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedPriority,
                        onExpandedChange = { expandedPriority = !expandedPriority }
                    ) {
                        OutlinedTextField(
                            value = selectedPriority?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedPriority
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedPriority,
                            onDismissRequest = { expandedPriority = false }
                        ) {
                            TaskPriority.values().forEach { priority ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = when (priority) {
                                                    TaskPriority.LOW -> Color(0xFF4CAF50)
                                                    TaskPriority.MEDIUM -> Color(0xFFFF9800)
                                                    TaskPriority.HIGH -> Color(0xFFFF5722)
                                                    TaskPriority.URGENT -> Color(0xFFE91E63)
                                                },
                                                modifier = Modifier.size(12.dp)
                                            ) {}
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(priority.name)
                                        }
                                    },
                                    onClick = {
                                        selectedPriority = priority
                                        expandedPriority = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                }
            }

            // Image Upload Card
            if (!isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Task Image (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.Black.copy(alpha = 0.6f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Image",
                                            tint = Color.White,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select Image")
                            }
                        }
                    }
                }
            }

            // Employee Assignment Card
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
                            text = "Assign Employees",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )

                        TextButton(
                            onClick = { showEmployeeSelector = !showEmployeeSelector }
                        ) {
                            Text(
                                text = if (showEmployeeSelector) "Done" else "Select",
                                color = PrimaryPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${selectedEmployees.size} employees selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (showEmployeeSelector) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search employees") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (val employeesState = approvedEmployees) {
                            is Resource.Success -> {
                                val filteredEmployees = employeesState.data.filter {
                                    it.name?.contains(searchQuery, ignoreCase = true) == true
                                }

                                // Select All Button
                                if (filteredEmployees.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                selectedEmployees = filteredEmployees.map { it.userId }.toSet()
                                            }
                                        ) {
                                            Text("Select All")
                                        }

                                        TextButton(
                                            onClick = { selectedEmployees = emptySet() }
                                        ) {
                                            Text("Clear All")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Employee List
                                filteredEmployees.forEach { employee ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedEmployees.contains(employee.userId),
                                            onCheckedChange = { isChecked ->
                                                selectedEmployees = if (isChecked) {
                                                    selectedEmployees + employee.userId
                                                } else {
                                                    selectedEmployees - employee.userId
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = PrimaryPurple
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Employee Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryPurple.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!employee.imageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = employee.imageUrl,
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

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = employee.name ?: "Unknown",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (!employee.email.isNullOrBlank()) {
                                                Text(
                                                    text = employee.email,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (!employee.position.isNullOrBlank()) {
                                                Text(
                                                    text = employee.position,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

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

                            is Resource.Error -> {
                                Text(
                                    text = "Error loading employees",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            null -> {
                                // Initial state
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}