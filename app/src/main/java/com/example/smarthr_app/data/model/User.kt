package com.example.smarthr_app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = "",
    val role: UserRole = UserRole.ROLE_USER,
    val companyCode: String? = null,
    val imageUrl: String? = null,
    val createdAt: String = "",
    val gender: String? = null,
    val position: String? = null,
    val department: String? = null,
    val waitingCompanyCode: String? = null,
    val joiningStatus: String? = null
) : Parcelable

enum class UserRole {
    ROLE_HR, ROLE_USER
}