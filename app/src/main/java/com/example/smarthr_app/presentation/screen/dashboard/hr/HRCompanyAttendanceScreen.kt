package com.example.smarthr_app.presentation.screen.dashboard.hr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.smarthr_app.data.model.AttendanceResponseDto
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AttendanceViewModel
import com.example.smarthr_app.utils.Resource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRCompanyAttendanceScreen(
    attendanceViewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val companyAttendanceState by attendanceViewModel.companyAttendanceState.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        attendanceViewModel.loadCompanyAttendance() // Load today's attendance
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

                Column {
                    Text(
                        text = "Company Attendance",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today - ${getCurrentDate()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        when (val attendanceState = companyAttendanceState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            is Resource.Success -> {
                // Filter employees based on their actual attendance status
                val presentEmployees = attendanceState.data.filter {
                    it.checkIn != null // Has checked in today
                }
                val checkedOutEmployees = attendanceState.data.filter {
                    it.checkOut != null // Has checked out today
                }
                val checkedInOnlyEmployees = attendanceState.data.filter {
                    it.checkIn != null && it.checkOut == null // Checked in but not out
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Summary Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AttendanceSummaryCard(
                                title = "Total Records",
                                count = attendanceState.data.size,
                                color = PrimaryPurple,
                                modifier = Modifier.weight(1f)
                            )

                            AttendanceSummaryCard(
                                title = "Checked In",
                                count = checkedInOnlyEmployees.size,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )

                            AttendanceSummaryCard(
                                title = "Completed",
                                count = checkedOutEmployees.size,
                                color = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (attendanceState.data.isNotEmpty()) {
                        item {
                            Text(
                                text = "Today's Attendance Records (${attendanceState.data.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(attendanceState.data) { employee ->
                            EmployeeAttendanceCard(employee = employee)
                        }
                    } else {
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
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No attendance records found for today",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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
                            text = "Error loading attendance data",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { attendanceViewModel.loadCompanyAttendance() },
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
fun AttendanceSummaryCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmployeeAttendanceCard(employee: AttendanceResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                if (!employee.employee.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = employee.employee.imageUrl,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.employee.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = employee.employee.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Attendance Status
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        employee.checkOut != null -> Color(0xFF2196F3) // Checked out (Complete)
                        employee.checkIn != null -> Color(0xFF4CAF50)  // Checked in only
                        else -> Color(0xFFFF5722) // No record (should not happen in this context)
                    }
                ) {
                    Text(
                        text = when {
                            employee.checkOut != null -> "Complete"
                            employee.checkIn != null -> "Present"
                            else -> "No Record"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Show check-in time
                if (employee.checkIn != null) {
                    Text(
                        text = "In: ${formatTime(employee.checkIn)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Show check-out time if available
                if (employee.checkOut != null) {
                    Text(
                        text = "Out: ${formatTime(employee.checkOut)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Calculate and show total working hours
                    val workingHours = calculateWorkingHours(employee.checkIn, employee.checkOut)
                    if (workingHours.isNotEmpty()) {
                        Text(
                            text = "Total: $workingHours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (employee.checkIn != null) {
                    Text(
                        text = "Still working...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun getCurrentDate(): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return today.format(formatter)
}

private fun formatTime(timeString: String): String {
    return try {
        // Handle ISO datetime format from backend
        val dateTime = java.time.LocalDateTime.parse(timeString.replace("Z", ""))
        val time = dateTime.toLocalTime()
        time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        // Fallback for different time formats
        try {
            val time = java.time.LocalTime.parse(timeString)
            time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e2: Exception) {
            timeString
        }
    }
}

private fun calculateWorkingHours(checkIn: String?, checkOut: String?): String {
    return try {
        if (checkIn != null && checkOut != null) {
            val checkInTime = java.time.LocalDateTime.parse(checkIn.replace("Z", ""))
            val checkOutTime = java.time.LocalDateTime.parse(checkOut.replace("Z", ""))

            val duration = java.time.Duration.between(checkInTime, checkOutTime)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60

            "${hours}h ${minutes}m"
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}