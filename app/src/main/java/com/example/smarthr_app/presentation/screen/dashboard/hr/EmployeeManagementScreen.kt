package com.example.smarthr_app.presentation.screen.dashboard.hr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.CompanyViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(
    companyViewModel: CompanyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val waitlistEmployees by companyViewModel.waitlistEmployees.collectAsState(initial = null)
    val approvedEmployees by companyViewModel.approvedEmployees.collectAsState(initial = null)
    val actionState by companyViewModel.actionState.collectAsState(initial = null)

    var showRemoveDialog by remember { mutableStateOf(false) }
    var employeeToRemove by remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(Unit) {
        companyViewModel.refreshAll()
    }

    // Handle action success
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Action completed successfully!")
                companyViewModel.refreshAll()
                companyViewModel.clearActionState()
                showRemoveDialog = false
                employeeToRemove = null
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                companyViewModel.clearActionState()
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
                    text = "Employee Management",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Main Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pending Requests Section
            item {
                Text(
                    text = "Pending Employee Requests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            when (val currentWaitlistEmployees = waitlistEmployees) {
                is Resource.Loading -> {
                    item {
                        LoadingCard()
                    }
                }

                is Resource.Success -> {
                    if (currentWaitlistEmployees.data.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.CheckCircle,
                                title = "No pending requests",
                                message = "All employee requests have been processed"
                            )
                        }
                    } else {
                        items(currentWaitlistEmployees.data.size) { index ->
                            val employee = currentWaitlistEmployees.data[index]
                            PendingEmployeeItem(
                                employee = employee,
                                onAccept = { companyViewModel.acceptEmployee(employee.userId) },
                                onReject = { companyViewModel.rejectEmployee(employee.userId) },
                                isLoading = actionState is Resource.Loading
                            )
                        }
                    }
                }

                is Resource.Error -> {
                    item {
                        ErrorCard(
                            icon = Icons.Default.Error,
                            title = "Error loading requests",
                            message = currentWaitlistEmployees.message,
                            onRetry = { companyViewModel.loadWaitlistEmployees() }
                        )
                    }
                }

                null -> {
                    // Initial state
                }
            }

            // Approved Employees Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Your Employees",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            when (val currentApprovedEmployees = approvedEmployees) {
                is Resource.Loading -> {
                    item {
                        LoadingCard()
                    }
                }

                is Resource.Success -> {
                    if (currentApprovedEmployees.data.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Group,
                                title = "No employees yet",
                                message = "Approved employees will appear here"
                            )
                        }
                    } else {
                        items(currentApprovedEmployees.data.size) { index ->
                            val employee = currentApprovedEmployees.data[index]
                            ApprovedEmployeeItem(
                                employee = employee,
                                onRemove = {
                                    employeeToRemove = employee
                                    showRemoveDialog = true
                                },
                                isLoading = actionState is Resource.Loading
                            )
                        }
                    }
                }

                is Resource.Error -> {
                    item {
                        ErrorCard(
                            icon = Icons.Default.Error,
                            title = "Error loading employees",
                            message = currentApprovedEmployees.message,
                            onRetry = { companyViewModel.loadApprovedEmployees() }
                        )
                    }
                }

                null -> {
                    // Initial state
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Remove Employee Confirmation Dialog
    if (showRemoveDialog && employeeToRemove != null) {
        AlertDialog(
            onDismissRequest = {
                showRemoveDialog = false
                employeeToRemove = null
            },
            title = {
                Text(
                    text = "Remove Employee",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${employeeToRemove?.name ?: "this employee"} from your company? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        employeeToRemove?.let { employee ->
                            companyViewModel.removeEmployee(employee.userId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = actionState !is Resource.Loading
                ) {
                    if (actionState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        employeeToRemove = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PendingEmployeeItem(
    employee: UserDto,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Employee Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Employee",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Employee Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = employee.name ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    employee.email?.let { email ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    employee.phone?.let { phone ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    employee.position?.let { position ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = position,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    employee.department?.let { department ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = department,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📤 Wants to join your company",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAccept,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onReject,
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovedEmployeeItem(
    employee: UserDto,
    onRemove: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Employee Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Employee",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Employee Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = employee.name ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Approved",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    employee.email?.let { email ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    employee.phone?.let { phone ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    employee.position?.let { position ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = position,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    employee.department?.let { department ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = department,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "✅ Active Employee",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Remove Button
            OutlinedButton(
                onClick = onRemove,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove from Company")
                }
            }
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryPurple)
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Retry")
            }
        }
    }
}