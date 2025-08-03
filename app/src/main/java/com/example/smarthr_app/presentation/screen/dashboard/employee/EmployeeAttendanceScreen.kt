package com.example.smarthr_app.presentation.screen.dashboard.employee

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthr_app.data.model.AttendanceResponseDto
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AttendanceViewModel
import com.example.smarthr_app.utils.LocationHelper
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import kotlinx.coroutines.launch

@Composable
fun EmployeeAttendanceScreen(
    attendanceViewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    val officeLocationState by attendanceViewModel.officeLocationState.collectAsState(initial = null)
    val markAttendanceState by attendanceViewModel.markAttendanceState.collectAsState(initial = null)
    val attendanceHistoryState by attendanceViewModel.attendanceHistoryState.collectAsState(initial = null)

    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    // Get current attendance status from history
    val currentAttendanceStatus = remember(attendanceHistoryState) {
        when (val state = attendanceHistoryState) {
            is Resource.Success -> {
                // Get today's attendance record
                val today = java.time.LocalDate.now().toString()
                val todayRecord = state.data.find { attendance ->
                    attendance.checkIn?.startsWith(today) == true
                }
                when {
                    todayRecord == null -> "NONE" // No check-in today
                    todayRecord.checkOut != null -> "CHECKOUT" // Already checked out
                    else -> "CHECKIN" // Checked in but not out
                }
            }
            else -> "NONE"
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        // Request location permissions
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        // Load initial data
        attendanceViewModel.loadOfficeLocation()
        attendanceViewModel.loadAttendanceHistory()
    }

    // Handle mark attendance response
    LaunchedEffect(markAttendanceState) {
        when (val state = markAttendanceState) {
            is Resource.Success -> {
                val actionText = if (state.data.checkOut != null) "Checked out successfully!"
                else "Checked in successfully!"
                ToastHelper.showSuccessToast(context, actionText)
                attendanceViewModel.clearMarkAttendanceState()
                attendanceViewModel.loadAttendanceHistory()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                attendanceViewModel.clearMarkAttendanceState()
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
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryPurple, PrimaryPurple.copy(alpha = 0.8f))
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Let's Clock-In!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Don't miss your clock-in schedule",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Clock icon
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Clock",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Today's Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Today's Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (val historyState = attendanceHistoryState) {
                            is Resource.Success -> {
                                val todayRecord = historyState.data.find { attendance ->
                                    attendance.checkIn?.startsWith(java.time.LocalDate.now().toString()) == true
                                }

                                if (todayRecord != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Check In",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatTime(todayRecord.checkIn ?: ""),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Check Out",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (todayRecord.checkOut != null)
                                                    formatTime(todayRecord.checkOut) else "Not yet",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No attendance recorded for today",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            is Resource.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = PrimaryPurple
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = "Unable to load today's status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Check-in/Check-out button
                Button(
                    onClick = {
                        if (!isLocationPermissionGranted) {
                            ToastHelper.showErrorToast(context, "Location permission required")
                            return@Button
                        }

                        when (val officeState = officeLocationState) {
                            is Resource.Success -> {
                                coroutineScope.launch {
                                    markAttendance(
                                        attendanceViewModel = attendanceViewModel,
                                        locationHelper = locationHelper,
                                        officeLocation = officeState.data,
                                        currentStatus = currentAttendanceStatus,
                                        context = context
                                    )
                                }
                            }
                            is Resource.Error -> {
                                ToastHelper.showErrorToast(context, "Office location not set by HR")
                            }
                            else -> {
                                ToastHelper.showErrorToast(context, "Loading office location...")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (currentAttendanceStatus) {
                            "CHECKIN" -> Color(0xFFFF5722) // Red for checkout
                            else -> Color(0xFF4CAF50) // Green for checkin
                        }
                    ),
                    enabled = markAttendanceState !is Resource.Loading && currentAttendanceStatus != "CHECKOUT"
                ) {
                    if (markAttendanceState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = when (currentAttendanceStatus) {
                                "CHECKIN" -> "Check Out"
                                "CHECKOUT" -> "Already Checked Out"
                                else -> "Check In"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Attendance History
        when (val historyState = attendanceHistoryState) {
            is Resource.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyState.data) { record ->
                        AttendanceRecordCard(record = record)
                    }
                }
            }
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
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
                            text = "Error loading attendance history",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
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
fun AttendanceRecordCard(record: AttendanceResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date
            Text(
                text = formatDisplayDate(record.checkIn ?: ""),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Check In Time
                Column {
                    Text(
                        text = "Check In",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (record.checkIn != null) formatTime(record.checkIn) else "Not recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Check Out Time
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Check Out",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (record.checkOut != null) formatTime(record.checkOut) else "Not recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private suspend fun markAttendance(
    attendanceViewModel: AttendanceViewModel,
    locationHelper: LocationHelper,
    officeLocation: com.example.smarthr_app.data.model.OfficeLocationResponseDto,
    currentStatus: String,
    context: android.content.Context
) {
    val currentLocation = locationHelper.getCurrentLocation()
    if (currentLocation == null) {
        ToastHelper.showErrorToast(context, "Unable to get current location")
        return
    }

    val currentLat = currentLocation.latitude.toString()
    val currentLng = currentLocation.longitude.toString()

    val isWithinRange = locationHelper.isWithinRadius(
        currentLat,
        currentLng,
        officeLocation.latitude,
        officeLocation.longitude,
        officeLocation.radius
    )

    if (!isWithinRange) {
        val distance = locationHelper.getDistanceInMeters(
            currentLat,
            currentLng,
            officeLocation.latitude,
            officeLocation.longitude
        )
        ToastHelper.showErrorToast(
            context,
            "You are ${distance.toInt()}m away from office. Please come closer to mark attendance."
        )
        return
    }

    val attendanceType = if (currentStatus == "CHECKIN") "CHECKOUT" else "CHECKIN"
    attendanceViewModel.markAttendance(
        attendanceType,
        currentLat,
        currentLng
    )
}

private fun formatDisplayDate(dateString: String): String {
    return try {
        val dateTime = java.time.LocalDateTime.parse(dateString.replace("Z", ""))
        val date = dateTime.toLocalDate()
        "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
    } catch (e: Exception) {
        dateString
    }
}

private fun formatTime(timeString: String): String {
    return try {
        val dateTime = java.time.LocalDateTime.parse(timeString.replace("Z", ""))
        val time = dateTime.toLocalTime()
        time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        timeString
    }
}