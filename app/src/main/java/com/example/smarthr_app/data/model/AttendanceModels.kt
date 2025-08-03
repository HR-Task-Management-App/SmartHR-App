package com.example.smarthr_app.data.model

// Office Location Models
data class OfficeLocationRequestDto(
    val latitude: String,
    val longitude: String,
    val radius: String     // (in meters)
)

data class OfficeLocationResponseDto(
    val id: String,
    val companyCode: String,
    val createdBy: UserInfo,
    val latitude: String,
    val longitude: String,
    val radius: String,
    val createdAt: String,
    val updatedAt: String
)

// Attendance Models
data class AttendanceRequestDto(
    val type: String,       // "CHECKIN" or "CHECKOUT"
    val latitude: String,
    val longitude: String
)

data class AttendanceResponseDto(
    val id: String,
    val employee: UserInfo,
    val companyCode: String,
    val latitude: String,
    val longitude: String,
    val checkIn: String?,
    val checkOut: String?
)

enum class AttendanceType {
    CHECKIN, CHECKOUT
}