package com.example.smarthr_app.presentation.screen.dashboard.hr

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.CompanyViewModel
import com.example.smarthr_app.presentation.viewmodel.MeetingViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetingScreen(
    meetingViewModel: MeetingViewModel,
    companyViewModel: CompanyViewModel,
    meetingId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = meetingId != null

    // States
    val createMeetingState by meetingViewModel.createMeetingState.collectAsState(initial = null)
    val updateMeetingState by meetingViewModel.updateMeetingState.collectAsState(initial = null)
    val meetingDetailState by meetingViewModel.meetingDetailState.collectAsState(initial = null)
    val approvedEmployeesState by companyViewModel.approvedEmployees.collectAsState(initial = null)

    // Form states
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var meetingLink by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var selectedEmployees by remember { mutableStateOf<List<UserDto>>(emptyList()) }

    // Dialog states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showEmployeeSelectionDialog by remember { mutableStateOf(false) }

    // Remove the automatic time picker LaunchedEffects - they prevent re-editing
    // Users can now click on the time field to edit it

    LaunchedEffect(Unit) {
        companyViewModel.loadApprovedEmployees()
        if (isEditing) {
            meetingViewModel.loadMeetingDetail(meetingId)
        }
    }

    // Load existing meeting data for editing
    LaunchedEffect(meetingDetailState) {
        if (isEditing) {
            when (val state = meetingDetailState) {
                is Resource.Success -> {
                    val meeting = state.data
                    title = meeting.title
                    description = meeting.description
                    meetingLink = meeting.meetingLink ?: ""

                    val startDateTime = LocalDateTime.parse(meeting.startTime)
                    startDate = startDateTime.toLocalDate().toString()
                    startTime = startDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

                    val endDateTime = LocalDateTime.parse(meeting.endTime)
                    endDate = endDateTime.toLocalDate().toString()
                    endTime = endDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

                    when (val empState = approvedEmployeesState) {
                        is Resource.Success -> {
                            selectedEmployees = empState.data.filter { employee ->
                                meeting.participants.any { participant -> participant.id == employee.userId }
                            }
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    // Handle create/update response with conflict detection
    LaunchedEffect(createMeetingState, updateMeetingState) {
        val state = if (isEditing) updateMeetingState else createMeetingState
        when (state) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(
                    context,
                    if (isEditing) "Meeting updated successfully!" else "Meeting created successfully!"
                )
                if (isEditing) {
                    meetingViewModel.clearUpdateMeetingState()
                } else {
                    meetingViewModel.clearCreateMeetingState()
                }
                onNavigateBack()
            }
            is Resource.Error -> {
                // Check if it's a time conflict error
                val errorMessage = state.message
                if (errorMessage.contains("conflict", ignoreCase = true) ||
                    errorMessage.contains("overlap", ignoreCase = true) ||
                    errorMessage.contains("time", ignoreCase = true)) {
                    ToastHelper.showErrorToast(context, "⚠️ Time Conflict: $errorMessage")
                } else {
                    ToastHelper.showErrorToast(context, errorMessage)
                }

                if (isEditing) {
                    meetingViewModel.clearUpdateMeetingState()
                } else {
                    meetingViewModel.clearCreateMeetingState()
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
                    text = if (isEditing) "Edit Meeting" else "Create Meeting",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
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
            // Meeting Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Meeting Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Meeting Title") },
                        placeholder = { Text("Enter meeting title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Enter meeting description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    OutlinedTextField(
                        value = meetingLink,
                        onValueChange = { meetingLink = it },
                        label = { Text("Meeting Link (Optional)") },
                        placeholder = { Text("https://meet.google.com/xxx-xxxx-xxx") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )
                }
            }

            // Date & Time Section - Updated with separate date and time fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    // Start Date and Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Date
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start Date") },
                            trailingIcon = {
                                IconButton(onClick = { showStartDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showStartDatePicker = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )

                        // Start Time - Always editable
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start Time") },
                            placeholder = { Text("Select time") },
                            trailingIcon = {
                                IconButton(onClick = { showStartTimePicker = true }) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Select Time")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showStartTimePicker = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )
                    }

                    // End Date and Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // End Date
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("End Date") },
                            trailingIcon = {
                                IconButton(onClick = { showEndDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showEndDatePicker = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )

                        // End Time - Always editable
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("End Time") },
                            placeholder = { Text("Select time") },
                            trailingIcon = {
                                IconButton(onClick = { showEndTimePicker = true }) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Select Time")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showEndTimePicker = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple
                            )
                        )
                    }

                    // Helper text
                    if (startDate.isNotBlank() && startTime.isBlank()) {
                        Text(
                            text = "👆 Please select start time",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (endDate.isNotBlank() && endTime.isBlank()) {
                        Text(
                            text = "👆 Please select end time",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Participants Section (keep the same as before)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Participants",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )

                        TextButton(
                            onClick = { showEmployeeSelectionDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Participants")
                        }
                    }

                    if (selectedEmployees.isNotEmpty()) {
                        selectedEmployees.forEach { employee ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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

                                Column(modifier = Modifier.weight(1f)) {
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

                                IconButton(
                                    onClick = {
                                        selectedEmployees = selectedEmployees.filter { it.userId != employee.userId }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No participants selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (validateForm(title, description, startDate, startTime, endDate, endTime, selectedEmployees)) {
                        val startDateTime = startDate + "T$startTime:00"
                        val endDateTime = endDate + "T$endTime:00"
                        val participantIds = selectedEmployees.mapNotNull { employee ->
                            employee.userId.takeIf { it.isNotBlank() }
                        }

                        if (isEditing && meetingId != null) {
                            meetingViewModel.updateMeeting(
                                meetingId = meetingId,
                                title = title,
                                description = description,
                                startTime = startDateTime,
                                endTime = endDateTime,
                                meetingLink = meetingLink.takeIf { it.isNotBlank() },
                                participants = participantIds
                            )
                        } else {
                            meetingViewModel.createMeeting(
                                title = title,
                                description = description,
                                startTime = startDateTime,
                                endTime = endDateTime,
                                meetingLink = meetingLink.takeIf { it.isNotBlank() },
                                participants = participantIds
                            )
                            Log.i("AuthVik","$startDateTime $endDateTime")
                        }
                    } else {
                        ToastHelper.showErrorToast(context, "Please fill all required fields")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (createMeetingState !is Resource.Loading) && (updateMeetingState !is Resource.Loading),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                val isLoading = (createMeetingState is Resource.Loading) || (updateMeetingState is Resource.Loading)
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (isEditing) "Update Meeting" else "Create Meeting",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Employee Selection Dialog (keep the same as before with search and select all)
    if (showEmployeeSelectionDialog) {
        EmployeeSelectionDialog(
            employees = when (val state = approvedEmployeesState) {
                is Resource.Success -> state.data.filter { employee ->
                    !employee.userId.isNullOrBlank()
                }
                else -> emptyList()
            },
            selectedEmployees = selectedEmployees,
            onEmployeeToggle = { employee ->
                selectedEmployees = if (selectedEmployees.any { it.userId == employee.userId }) {
                    selectedEmployees.filter { it.userId != employee.userId }
                } else {
                    selectedEmployees + employee
                }
            },
            onSelectAll = { employees ->
                selectedEmployees = employees
            },
            onDismiss = { showEmployeeSelectionDialog = false },
            onConfirm = { showEmployeeSelectionDialog = false }
        )
    }

    // Date and Time Pickers
    if (showStartDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
                // Auto-show time picker if time is not set
                if (startTime.isBlank()) {
                    showStartTimePicker = true
                }
            },
            onDismiss = { showStartDatePicker = false },
            initialDate = startDate.ifBlank { LocalDateTime.now().toLocalDate().toString() }
        )
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                startTime = time
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
            initialTime = startTime.ifBlank { "09:00" }
        )
    }

    if (showEndDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
                // Auto-show time picker if time is not set
                if (endTime.isBlank()) {
                    showEndTimePicker = true
                }
            },
            onDismiss = { showEndDatePicker = false },
            initialDate = endDate.ifBlank { startDate.ifBlank { LocalDateTime.now().toLocalDate().toString() } }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                endTime = time
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
            initialTime = endTime.ifBlank { "10:00" }
        )
    }
}

// Updated EmployeeSelectionDialog with Search and Select All
@Composable
fun EmployeeSelectionDialog(
    employees: List<UserDto>,
    selectedEmployees: List<UserDto>,
    onEmployeeToggle: (UserDto) -> Unit,
    onSelectAll: (List<UserDto>) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredEmployees = employees.filter { employee ->
        employee.name.contains(searchQuery, ignoreCase = true) ||
                employee.email.contains(searchQuery, ignoreCase = true)
    }

    val allSelected = filteredEmployees.isNotEmpty() &&
            filteredEmployees.all { employee -> selectedEmployees.any { it.userId == employee.userId } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Participants",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search employees") },
                    placeholder = { Text("Enter name or email") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select All Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (allSelected) {
                                // Deselect all filtered employees
                                val remainingSelected = selectedEmployees.filter { selected ->
                                    filteredEmployees.none { it.userId == selected.userId }
                                }
                                onSelectAll(remainingSelected)
                            } else {
                                // Select all filtered employees
                                val newSelected = (selectedEmployees + filteredEmployees).distinctBy { it.userId }
                                onSelectAll(newSelected)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                val newSelected = (selectedEmployees + filteredEmployees).distinctBy { it.userId }
                                onSelectAll(newSelected)
                            } else {
                                val remainingSelected = selectedEmployees.filter { selected ->
                                    filteredEmployees.none { it.userId == selected.userId }
                                }
                                onSelectAll(remainingSelected)
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Select All (${filteredEmployees.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEmployees) { employee ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEmployeeToggle(employee) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedEmployees.any { it.userId == employee.userId },
                                onCheckedChange = { onEmployeeToggle(employee) },
                                colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

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

                            Column {
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Done (${selectedEmployees.size})")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    initialDate: String
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            java.time.LocalDate.parse(initialDate).toEpochDay() * 24 * 60 * 60 * 1000
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(selectedDate.toString())
                    }
                }
            ) {
                Text("OK", color = PrimaryPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Select Date",
                    modifier = Modifier.padding(16.dp)
                )
            },
            headline = {
                Text(
                    text = "Choose meeting date",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    initialTime: String
) {
    val timePickerState = rememberTimePickerState(
        initialHour = try {
            initialTime.split(":")[0].toInt()
        } catch (e: Exception) { 9 },
        initialMinute = try {
            initialTime.split(":")[1].toInt()
        } catch (e: Exception) { 0 }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.wrapContentSize(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val hour = String.format("%02d", timePickerState.hour)
                            val minute = String.format("%02d", timePickerState.minute)
                            onTimeSelected("$hour:$minute")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

private fun validateForm(
    title: String,
    description: String,
    startDate: String,
    startTime: String,
    endDate: String,
    endTime: String,
    selectedEmployees: List<UserDto>
): Boolean {
    return title.isNotBlank() &&
            description.isNotBlank() &&
            startDate.isNotBlank() &&
            startTime.isNotBlank() &&
            endDate.isNotBlank() &&
            endTime.isNotBlank() &&
            selectedEmployees.isNotEmpty()
}