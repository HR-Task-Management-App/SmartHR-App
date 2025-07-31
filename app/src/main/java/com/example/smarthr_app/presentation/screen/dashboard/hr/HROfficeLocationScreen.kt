package com.example.smarthr_app.presentation.screen.dashboard.hr

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.AttendanceViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HROfficeLocationScreen(
    attendanceViewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val officeLocationState by attendanceViewModel.officeLocationState.collectAsState(initial = null)
    val createOfficeLocationState by attendanceViewModel.createOfficeLocationState.collectAsState(initial = null)

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }
    var existingLocationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attendanceViewModel.loadOfficeLocation()
    }

    // Load existing office location data
    LaunchedEffect(officeLocationState) {
        when (val state = officeLocationState) {
            is Resource.Success -> {
                latitude = state.data.latitude.toString()
                longitude = state.data.longitude.toString()
                radius = state.data.radius.toString()
                existingLocationId = state.data.id
            }
            is Resource.Error -> {
                // No existing location, keep fields empty
            }
            else -> {}
        }
    }

    // Handle create/update response
    LaunchedEffect(createOfficeLocationState) {
        when (val state = createOfficeLocationState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(
                    context,
                    if (existingLocationId != null) "Office location updated successfully!"
                    else "Office location created successfully!"
                )
                attendanceViewModel.clearCreateOfficeLocationState()
                attendanceViewModel.loadOfficeLocation()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                attendanceViewModel.clearCreateOfficeLocationState()
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
                    text = "Office Location Setup",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryPurple.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Set Office Location",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Configure the office coordinates and radius for employee attendance tracking.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Location Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Office Coordinates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )

                    // Latitude
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        placeholder = { Text("e.g., 28.6139") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryPurple
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    // Longitude
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        placeholder = { Text("e.g., 77.2090") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryPurple
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    // Radius
                    OutlinedTextField(
                        value = radius,
                        onValueChange = { radius = it },
                        label = { Text("Radius (meters)") },
                        placeholder = { Text("e.g., 100") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = PrimaryPurple
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        ),
                        supportingText = {
                            Text("Employees within this radius can mark attendance")
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (latitude.isBlank() || longitude.isBlank() || radius.isBlank()) {
                                ToastHelper.showErrorToast(context, "Please enter all required fields")
                                return@Button
                            }

                            val lat = latitude.toDoubleOrNull()
                            val lng = longitude.toDoubleOrNull()
                            val rad = radius.toDoubleOrNull()

                            if (lat == null || lng == null || rad == null) {
                                ToastHelper.showErrorToast(context, "Please enter valid numeric values")
                                return@Button
                            }

                            if (lat < -90 || lat > 90) {
                                ToastHelper.showErrorToast(context, "Latitude must be between -90 and 90")
                                return@Button
                            }

                            if (lng < -180 || lng > 180) {
                                ToastHelper.showErrorToast(context, "Longitude must be between -180 and 180")
                                return@Button
                            }

                            if (rad <= 0) {
                                ToastHelper.showErrorToast(context, "Radius must be greater than 0")
                                return@Button
                            }

                            if (existingLocationId != null) {
                                attendanceViewModel.updateOfficeLocation(existingLocationId!!, latitude, longitude, radius)
                            } else {
                                attendanceViewModel.createOfficeLocation(latitude, longitude, radius)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = createOfficeLocationState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        if (createOfficeLocationState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (existingLocationId != null) "Update Location" else "Save Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Current Location Status
            when (val locationState = officeLocationState) {
                is Resource.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Office Location Configured",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Created by: ${locationState.data.createdBy.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Last updated: ${formatDisplayDate(locationState.data.updatedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "No office location configured yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                }
                null -> {
                    // Initial state
                }
            }

            // Helper Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to find coordinates:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Open Google Maps\n2. Right-click on your office location\n3. Click on the coordinates that appear\n4. Copy the latitude and longitude values",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDisplayDate(dateString: String): String {
    return try {
        val dateTime = java.time.LocalDateTime.parse(dateString.replace("Z", ""))
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateString
    }
}