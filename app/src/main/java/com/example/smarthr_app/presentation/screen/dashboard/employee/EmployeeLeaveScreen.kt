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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthr_app.data.model.EmployeeLeaveResponseDto
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.LeaveViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import com.example.smarthr_app.presentation.components.CompanyLockScreen
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeLeaveScreen(
    leaveViewModel: LeaveViewModel,
    authViewModel: AuthViewModel,
    onNavigateToCompanyManagement: () -> Unit
) {
    val context = LocalContext.current
    val employeeLeavesState by leaveViewModel.employeeLeavesState.collectAsState(initial = null)
    val submitLeaveState by leaveViewModel.submitLeaveState.collectAsState(initial = null)
    val updateLeaveState by leaveViewModel.updateLeaveState.collectAsState(initial = null)
    val leaveSummary by leaveViewModel.leaveSummary.collectAsState()
    val user by authViewModel.user.collectAsState(initial = null)

    // Check if user has joined a company
    val hasJoinedCompany = !user?.companyCode.isNullOrBlank()
    val isWaitlisted = !user?.waitingCompanyCode.isNullOrBlank()

    var selectedFilter by remember { mutableStateOf("Review") }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var editingLeave by remember { mutableStateOf<EmployeeLeaveResponseDto?>(null) }

    LaunchedEffect(Unit) {
        if (hasJoinedCompany) {
            leaveViewModel.loadEmployeeLeaves()
        }
    }

    // Show lock screen if user hasn't joined a company
    if (!hasJoinedCompany && !isWaitlisted) {
        CompanyLockScreen(
            title = "Leave Feature Locked",
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

    // Handle submit/update responses
    LaunchedEffect(submitLeaveState) {
        when (val state = submitLeaveState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Leave request submitted successfully!")
                leaveViewModel.clearSubmitLeaveState()
                leaveViewModel.loadEmployeeLeaves()
                showSubmitDialog = false
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                leaveViewModel.clearSubmitLeaveState()
            }
            else -> {}
        }
    }

    LaunchedEffect(updateLeaveState) {
        when (val state = updateLeaveState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Leave request updated successfully!")
                leaveViewModel.clearUpdateLeaveState()
                leaveViewModel.loadEmployeeLeaves()
                editingLeave = null
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                leaveViewModel.clearUpdateLeaveState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryPurple, PrimaryPurple.copy(alpha = 0.8f))
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Leave Summary",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Illustration icon
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Leave",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Leave Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 1.0f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Leave",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = leaveSummary.period,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Available",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${leaveSummary.available}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Yellow, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Leave Used",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${leaveSummary.leaveUsed}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Tabs
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("Review", "Approved", "Rejected")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(filter)
                        if (filter != "Review") {
                            val count = when (filter) {
                                "Approved" -> (employeeLeavesState as? Resource.Success)?.data?.count {
                                    it.status == "APPROVED"
                                } ?: 0
                                "Rejected" -> (employeeLeavesState as? Resource.Success)?.data?.count {
                                    it.status == "REJECTED"
                                } ?: 0
                                else -> (employeeLeavesState as? Resource.Success)?.data?.count {
                                    it.status == "PENDING"
                                } ?: 0
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

        // Leave List
        when (val currentLeavesState = employeeLeavesState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            is Resource.Success -> {
                val filteredLeaves = when (selectedFilter) {
                    "Approved" -> currentLeavesState.data.filter { it.status == "APPROVED" }
                    "Rejected" -> currentLeavesState.data.filter { it.status == "REJECTED" }
                    else -> currentLeavesState.data.filter { it.status == "PENDING" }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLeaves) { leave ->
                        EmployeeLeaveCard(
                            leave = leave,
                            onEdit = { editingLeave = it },
                            canEdit = leave.status == "PENDING" && leave.responseBy == null
                        )
                    }

                    if (filteredLeaves.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventBusy,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No ${selectedFilter.lowercase()} leaves",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
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
                            text = "Error loading leaves",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { leaveViewModel.loadEmployeeLeaves() },
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

        // Submit Leave Button
        Button(
            onClick = { showSubmitDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Text(
                text = "Submit Leave",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Submit/Edit Leave Dialog
    if (showSubmitDialog || editingLeave != null) {
        LeaveRequestDialog(
            existingLeave = editingLeave,
            onDismiss = {
                showSubmitDialog = false
                editingLeave = null
            },
            onSubmit = { leaveRequest ->
                if (editingLeave != null) {
                    leaveViewModel.updateLeaveRequest(editingLeave!!.id, leaveRequest)
                } else {
                    leaveViewModel.submitLeaveRequest(leaveRequest)
                }
            },
            isLoading = submitLeaveState is Resource.Loading || updateLeaveState is Resource.Loading
        )
    }
}

@Composable
fun EmployeeLeaveCard(
    leave: EmployeeLeaveResponseDto,
    onEdit: (EmployeeLeaveResponseDto) -> Unit,
    canEdit: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with date and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (leave.status) {
                            "PENDING" -> Color(0xFFFF9800)
                            "APPROVED" -> Color(0xFF4CAF50)
                            "REJECTED" -> Color(0xFFFF5722)
                            else -> Color.Gray
                        }
                    ) {
                        Text(
                            text = when (leave.status) {
                                "PENDING" -> "Under Review"
                                "APPROVED" -> "Approved"
                                "REJECTED" -> "Rejected"
                                else -> leave.status
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (canEdit) {
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { onEdit(leave) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Leave",
                                modifier = Modifier.size(16.dp),
                                tint = PrimaryPurple
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Leave details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Leave Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatDisplayDate(leave.startDate)} - ${formatDisplayDate(leave.endDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Leave",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${calculateLeaveDays(leave.startDate, leave.endDate)} Days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Leave type and description
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryPurple.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = leave.type,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = leave.emergencyContact,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = leave.leaveDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Response information
            if (leave.responseBy != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (leave.status) {
                            "APPROVED" -> Icons.Default.CheckCircle
                            "REJECTED" -> Icons.Default.Cancel
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = when (leave.status) {
                            "APPROVED" -> Color(0xFF4CAF50)
                            "REJECTED" -> Color(0xFFFF5722)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${leave.status.lowercase().replaceFirstChar { it.uppercase() }} by ${leave.responseBy.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (leave.respondedAt != null) {
                        Text(
                            text = " • ${formatDisplayDate(leave.respondedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatDateRange(startDate: String, endDate: String): String {
    return try {
        val start = java.time.LocalDate.parse(startDate)
        val end = java.time.LocalDate.parse(endDate)
        val startDay = start.dayOfMonth
        val endDay = end.dayOfMonth
        val month = start.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        "$startDay ${month} - $endDay ${month}"
    } catch (e: Exception) {
        "$startDate - $endDate"
    }
}

private fun formatDisplayDate(dateString: String): String {
    return try {
        val date = java.time.LocalDate.parse(dateString)
        "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)}"
    } catch (e: Exception) {
        dateString
    }
}

private fun calculateLeaveDays(startDate: String, endDate: String): Int {
    return try {
        val start = java.time.LocalDate.parse(startDate)
        val end = java.time.LocalDate.parse(endDate)
        (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).toInt()
    } catch (e: Exception) {
        1
    }
}