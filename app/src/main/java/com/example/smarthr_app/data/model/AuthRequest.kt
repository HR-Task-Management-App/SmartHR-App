package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

data class UserRegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val gender: String = "M",
    val role: String,
    val companyCode: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    @SerializedName("id")
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val gender: String?,
    val role: String,
    val companyCode: String?,
    val imageUrl: String?,
    val position: String? = null,
    val department: String? = null,
    val waitingCompanyCode: String? = null,
    val joiningStatus: String? = null
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String?,
    val gender: String?,
    val position: String?,
    val department: String?
)

data class CompanyWaitlistResponse(
    val companyCode: String,
    val users: List<UserDto>
)

data class CompanyEmployeesResponse(
    val companyCode: String,
    val users: List<UserDto>
)

data class SuccessApiResponseMessage(
    val message: String
)

// Enums for dropdowns
enum class Position {
    INTERN, JUNIOR_DEVELOPER, SENIOR_DEVELOPER, TEAM_LEAD, MANAGER, HR, CTO, CEO, OTHERS
}

enum class Department {
    HR, ENGINEERING, SALES, MARKETING, FINANCE, OPERATIONS, ADMINISTRATION, SUPPORT, OTHERS
}

enum class Gender {
    M, F
}