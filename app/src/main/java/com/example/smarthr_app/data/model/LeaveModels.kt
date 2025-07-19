package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

// Leave Request/Response Models
data class LeaveRequestDto(
    val type: String,
    val emergencyContact: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String,   // yyyy-MM-dd
    val leaveDescription: String
)

data class EmployeeLeaveResponseDto(
    val id: String,
    val type: String,
    val emergencyContact: String,
    val startDate: String,
    val endDate: String,
    val leaveDescription: String,
    val status: String, // PENDING, APPROVED, REJECTED
    val appliedAt: String,
    val respondedAt: String?,
    val responseBy: UserInfo?
)

data class HRLeaveResponseDto(
    val id: String,
    val employee: UserInfo,
    val type: String,
    val emergencyContact: String,
    val startDate: String,
    val endDate: String,
    val leaveDescription: String,
    val status: String,
    val appliedAt: String,
    val respondedAt: String?,
    val responseBy: UserInfo?
)

// Enums
enum class LeaveType {
    SICK, CASUAL, UNPAID, VACATION, MATERNITY, PATERNITY, OTHERS
}

enum class LeaveStatus {
    PENDING, APPROVED, REJECTED
}

// Leave Summary Data
data class LeaveSummary(
    val totalLeave: Int = 20,
    val leaveUsed: Int = 0,
    val available: Int = 20,
    val period: String = "1 Jan 2025 - 31 Dec 2025"
)