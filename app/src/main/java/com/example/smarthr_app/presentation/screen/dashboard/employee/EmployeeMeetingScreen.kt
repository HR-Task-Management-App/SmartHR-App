package com.example.smarthr_app.presentation.screen.dashboard.employee

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.smarthr_app.data.model.MeetingResponseDto
import com.example.smarthr_app.data.model.UserInfo
import com.example.smarthr_app.presentation.theme.PrimaryPurple
import com.example.smarthr_app.presentation.viewmodel.MeetingViewModel
import com.example.smarthr_app.presentation.viewmodel.AuthViewModel
import com.example.smarthr_app.utils.Resource
import com.example.smarthr_app.utils.ToastHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeMeetingScreen(
    meetingViewModel: MeetingViewModel,
    authViewModel: AuthViewModel, // Add AuthViewModel to get current user
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val meetingsState by meetingViewModel.meetingsState.collectAsState(initial = null)
    val respondToMeetingState by meetingViewModel.respondToMeetingState.collectAsState(initial = null)
    val currentUser by authViewModel.user.collectAsState(initial = null)

    var selectedFilter by remember { mutableStateOf("upcoming") } // "upcoming" or "past"
    var showParticipantsDialog by remember { mutableStateOf(false) }
    var selectedMeeting by remember { mutableStateOf<MeetingResponseDto?>(null) }

    LaunchedEffect(Unit) {
        meetingViewModel.loadMeetings()
    }

    // Handle respond to meeting response
    LaunchedEffect(respondToMeetingState) {
        when (val state = respondToMeetingState) {
            is Resource.Success -> {
                ToastHelper.showSuccessToast(context, "Response sent successfully")
                meetingViewModel.clearRespondToMeetingState()
                meetingViewModel.loadMeetings()
            }
            is Resource.Error -> {
                ToastHelper.showErrorToast(context, state.message)
                meetingViewModel.clearRespondToMeetingState()
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
                    text = "My Meetings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter Tabs
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                FilterChip(
                    onClick = { selectedFilter = "upcoming" },
                    label = { Text("Upcoming") },
                    selected = selectedFilter == "upcoming",
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple,
                        selectedLabelColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    onClick = { selectedFilter = "past" },
                    label = { Text("Past") },
                    selected = selectedFilter == "past",
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Content
        when (val state = meetingsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }

            is Resource.Success -> {
                val meetings = state.data ?: emptyList() // Safe null check
                val currentDateTime = LocalDateTime.now()
                val filteredMeetings = meetings.filter { meeting ->
                    try {
                        val meetingDateTime = LocalDateTime.parse(meeting?.startTime ?: return@filter false)
                        when (selectedFilter) {
                            "upcoming" -> meetingDateTime.isAfter(currentDateTime) && (meeting.status != "CANCELLED")
                            "past" -> meetingDateTime.isBefore(currentDateTime) || (meeting.status == "CANCELLED")
                            else -> true
                        }
                    } catch (e: Exception) {
                        false // Skip meetings with invalid dates
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMeetings) { meeting ->
                        if (meeting != null) {
                            EmployeeMeetingCard(
                                meeting = meeting,
                                onCopyLink = { link ->
                                    copyToClipboard(context, link)
                                    ToastHelper.showSuccessToast(context, "Meeting link copied!")
                                },
                                onRespondToMeeting = { meetingId, status ->
                                    meetingViewModel.respondToMeeting(meetingId, status)
                                },
                                onParticipantsClick = {
                                    selectedMeeting = meeting
                                    showParticipantsDialog = true
                                },
                                currentUserId = currentUser?.userId, // Pass current user ID
                                isLoading = respondToMeetingState is Resource.Loading
                            )
                        }
                    }

                    if (filteredMeetings.isEmpty()) {
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
                                        imageVector = Icons.Default.EventNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No ${selectedFilter} meetings found",
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
                            text = "Error loading meetings",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { meetingViewModel.loadMeetings() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            null -> {
                // Initial state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
        }
    }

    // Participants Dialog
    if (showParticipantsDialog && selectedMeeting != null) {
        EmployeeParticipantsDialog(
            meeting = selectedMeeting!!,
            onDismiss = {
                showParticipantsDialog = false
                selectedMeeting = null
            }
        )
    }
}

@Composable
fun EmployeeMeetingCard(
    meeting: MeetingResponseDto,
    onCopyLink: (String) -> Unit,
    onRespondToMeeting: (String, String) -> Unit,
    onParticipantsClick: () -> Unit,
    isLoading: Boolean,
    currentUserId: String? // Add current user ID to determine response status
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Meeting header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meeting.title ?: "Untitled Meeting",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = meeting.description ?: "No description",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (meeting.status ?: "UNKNOWN") {
                        "SCHEDULED" -> Color(0xFF4CAF50)
                        "CANCELLED" -> Color(0xFFFF5722)
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = meeting.status ?: "UNKNOWN",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meeting time - Updated to show end time
            if (!meeting.startTime.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${formatDateTime(meeting.startTime)} - ${formatDateTime(meeting.endTime ?: "")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Meeting link
            if (!meeting.meetingLink.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = meeting.meetingLink,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryPurple,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onCopyLink(meeting.meetingLink) }
                    ) {
                        Text("Copy", color = PrimaryPurple)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Participants - Clickable with count
            val participants = meeting.participants ?: emptyList()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onParticipantsClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${participants.size} participants",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryPurple
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryPurple
                )
            }

            // Response status and actions
            if (meeting.status == "SCHEDULED") {
                Spacer(modifier = Modifier.height(12.dp))

                // Find current user's response
                val responses = meeting.responses ?: emptyList()
                val currentUserResponse = responses.find { response ->
                    response.participant?.id == currentUserId
                }
                val responseStatus = currentUserResponse?.status ?: "PENDING"

                if (responseStatus == "PENDING") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                meeting.id?.let { id ->
                                    onRespondToMeeting(id, "ACCEPTED")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Accept")
                            }
                        }

                        Button(
                            onClick = {
                                meeting.id?.let { id ->
                                    onRespondToMeeting(id, "DECLINED")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("Decline")
                        }
                    }
                } else {
                    // Show response status
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = when (responseStatus) {
                            "ACCEPTED" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            "DECLINED" -> Color(0xFFFF5722).copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (responseStatus) {
                                    "ACCEPTED" -> Icons.Default.CheckCircle
                                    "DECLINED" -> Icons.Default.Cancel
                                    else -> Icons.Default.Schedule
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = when (responseStatus) {
                                    "ACCEPTED" -> Color(0xFF4CAF50)
                                    "DECLINED" -> Color(0xFFFF5722)
                                    else -> Color.Gray
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (responseStatus) {
                                    "ACCEPTED" -> "You accepted this meeting"
                                    "DECLINED" -> "You declined this meeting"
                                    else -> "Response pending"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = when (responseStatus) {
                                    "ACCEPTED" -> Color(0xFF4CAF50)
                                    "DECLINED" -> Color(0xFFFF5722)
                                    else -> Color.Gray
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// Employee Participants Dialog
@Composable
fun EmployeeParticipantsDialog(
    meeting: MeetingResponseDto,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Meeting Participants",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(meeting.participants ?: emptyList()) { participant ->
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
                                if (!participant.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = participant.imageUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = (participant.name?.take(1) ?: "?").uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = participant.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = participant.email ?: "No email",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Response status
                            val response = meeting.responses?.find { it.participant?.id == participant.id }
                            val status = response?.status ?: "PENDING"

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (status) {
                                    "ACCEPTED" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    "DECLINED" -> Color(0xFFFF5722).copy(alpha = 0.1f)
                                    else -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                }
                            ) {
                                Text(
                                    text = status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (status) {
                                        "ACCEPTED" -> Color(0xFF4CAF50)
                                        "DECLINED" -> Color(0xFFFF5722)
                                        else -> Color(0xFFFF9800)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ParticipantAvatar(participant: UserInfo) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(PrimaryPurple.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (!participant.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = participant.imageUrl,
                contentDescription = participant.name ?: "Participant",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = (participant.name?.take(1) ?: "?").uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDateTime(dateTimeString: String?): String {
    return try {
        if (dateTimeString.isNullOrBlank()) return "Invalid Date"
        val dateTime = LocalDateTime.parse(dateTimeString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTimeString ?: "Invalid Date"
    }
}

private fun formatTime(dateTimeString: String?): String {
    return try {
        if (dateTimeString.isNullOrBlank()) return "Invalid Time"
        val dateTime = LocalDateTime.parse(dateTimeString)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        dateTime.format(formatter)
    } catch (e: Exception) {
        dateTimeString ?: "Invalid Time"
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Meeting Link", text)
    clipboard.setPrimaryClip(clip)
}