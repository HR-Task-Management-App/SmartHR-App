package com.example.smarthr_app.presentation.screen.dashboard.employee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.smarthr_app.data.model.EmployeeLeaveResponseDto
import com.example.smarthr_app.data.model.LeaveRequestDto
import com.example.smarthr_app.data.model.LeaveType
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestDialog(
    existingLeave: EmployeeLeaveResponseDto? = null,
    onDismiss: () -> Unit,
    onSubmit: (LeaveRequestDto) -> Unit,
    isLoading: Boolean = false
) {
    var selectedLeaveType by remember { mutableStateOf(existingLeave?.type ?: "") }
    var emergencyContact by remember { mutableStateOf(existingLeave?.emergencyContact ?: "") }
    var startDate by remember { mutableStateOf(existingLeave?.startDate ?: "") }
    var endDate by remember { mutableStateOf(existingLeave?.endDate ?: "") }
    var leaveDescription by remember { mutableStateOf(existingLeave?.leaveDescription ?: "") }

    var expandedLeaveType by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val isEditing = existingLeave != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp), // Increased height to accommodate separate date lines
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Leave Request" else "Submit Leave Request",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Leave Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedLeaveType,
                    onExpandedChange = { expandedLeaveType = !expandedLeaveType }
                ) {
                    OutlinedTextField(
                        value = selectedLeaveType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Leave Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandedLeaveType
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
                        expanded = expandedLeaveType,
                        onDismissRequest = { expandedLeaveType = false }
                    ) {
                        LeaveType.values().forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (type) {
                                                LeaveType.SICK -> Icons.Default.LocalHospital
                                                LeaveType.CASUAL -> Icons.Default.BeachAccess
                                                LeaveType.VACATION -> Icons.Default.Flight
                                                LeaveType.MATERNITY -> Icons.Default.ChildCare
                                                LeaveType.PATERNITY -> Icons.Default.Person
                                                LeaveType.UNPAID -> Icons.Default.MoneyOff
                                                LeaveType.OTHERS -> Icons.Default.MoreHoriz
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = PrimaryPurple
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                },
                                onClick = {
                                    selectedLeaveType = type.name
                                    expandedLeaveType = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Contact
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact") },
                    placeholder = { Text("+911234567890") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = PrimaryPurple
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selection - Separate lines
                // Start Date
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    placeholder = { Text("Select start date") },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Start Date",
                                tint = PrimaryPurple
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showStartDatePicker = true
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // End Date
                OutlinedTextField(
                    value = endDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date") },
                    placeholder = { Text("Select end date") },
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select End Date",
                                tint = PrimaryPurple
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showEndDatePicker = true
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Leave Description
                OutlinedTextField(
                    value = leaveDescription,
                    onValueChange = { leaveDescription = it },
                    label = { Text("Leave Reason") },
                    placeholder = { Text("Please provide details about your leave request...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val leaveRequest = LeaveRequestDto(
                                type = selectedLeaveType,
                                emergencyContact = emergencyContact,
                                startDate = startDate,
                                endDate = endDate,
                                leaveDescription = leaveDescription
                            )
                            onSubmit(leaveRequest)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && selectedLeaveType.isNotBlank() &&
                                emergencyContact.isNotBlank() && startDate.isNotBlank() &&
                                endDate.isNotBlank() && leaveDescription.length >= 10,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text(if (isEditing) "Update" else "Submit")
                        }
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
                // Auto-set end date if not already set
                if (endDate.isBlank()) {
                    endDate = date
                }
            },
            onDismiss = { showStartDatePicker = false },
            initialDate = startDate.ifBlank { LocalDate.now().toString() }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            initialDate = endDate.ifBlank {
                if (startDate.isNotBlank()) startDate else LocalDate.now().toString()
            },
            minDate = startDate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    initialDate: String,
    minDate: String? = null
) {
    // Convert string date to milliseconds for DatePickerState
    val initialDateMillis = try {
        LocalDate.parse(initialDate).toEpochDay() * 24 * 60 * 60 * 1000
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        val today = LocalDate.now()

                        // Manual validation since dateValidator is not available
                        if (selectedDate.isBefore(today)) {
                            // Don't allow past dates - you could show a toast here
                            return@let
                        }

                        minDate?.let { min ->
                            if (selectedDate.isBefore(LocalDate.parse(min))) {
                                // Don't allow dates before minimum date
                                return@let
                            }
                        }

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
                    text = "Choose your leave date",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        )
    }
}