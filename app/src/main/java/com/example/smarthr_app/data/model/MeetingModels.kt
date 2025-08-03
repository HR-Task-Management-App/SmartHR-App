package com.example.smarthr_app.data.model

// Meeting Request Models
data class MeetingCreateRequestDto(
    val title: String,
    val description: String,
    val startTime: String, // ISO format: "2025-07-22T10:00:00"
    val endTime: String,   // ISO format: "2025-07-22T11:00:00"
    val meetingLink: String? = null,
    val participants: List<String> // List of participant IDs
)

data class MeetingUpdateRequestDto(
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val meetingLink: String? = null,
    val participants: List<String>
)

// Meeting Response Models
data class MeetingResponseDto(
    val id: String,
    val title: String,
    val description: String,
    val organizer: String, // HR ID
    val companyCode: String,
    val meetingLink: String?,
    val startTime: String,
    val endTime: String,
    val participants: List<UserInfo>,
    val responses: List<MeetingResponseInfo>,
    val status: String // "SCHEDULED", "CANCELLED", etc.
)

data class MeetingResponseInfo(
    val participant: UserInfo,
    val status: String // "ACCEPTED", "DECLINED", "PENDING"
)

// Enums
enum class MeetingStatus {
    SCHEDULED, CANCELLED, COMPLETED
}

enum class ParticipantResponseStatus {
    PENDING, ACCEPTED, DECLINED
}