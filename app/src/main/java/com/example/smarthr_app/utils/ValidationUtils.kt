package com.example.smarthr_app.utils

import android.util.Patterns

object ValidationUtils {

    fun validateName(name: String): ValidationResult {
        val nameWithoutSpaces = name.replace(" ", "")
        return when {
            name.isBlank() -> ValidationResult(false, "Name is required")
            nameWithoutSpaces.length < 3 -> ValidationResult(false, "Name must be at least 3 characters (excluding spaces)")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationResult(false, "Name should only contain alphabets and spaces")
            else -> ValidationResult(true, "")
        }
    }

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult(false, "Please enter a valid email address")
            !email.endsWith(".com", ignoreCase = true) -> ValidationResult(false, "Email must end with .com")
            else -> ValidationResult(true, "")
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        val cleanPhone = phone.replace("+91", "").trim()
        return when {
            phone.isBlank() -> ValidationResult(false, "Phone number is required")
            cleanPhone.length != 10 -> ValidationResult(false, "Phone number must be exactly 10 digits")
            !cleanPhone.matches(Regex("^[0-9]+$")) -> ValidationResult(false, "Phone number should only contain digits")
            !cleanPhone.matches(Regex("^[6-9][0-9]{9}$")) -> ValidationResult(false, "Please enter a valid Indian mobile number")
            else -> ValidationResult(true, "")
        }
    }

    // Updated password validation to match backend pattern
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 6 -> ValidationResult(false, "Password must be at least 6 characters")
            !password.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@#\$%^&+=!]{6,}$")) ->
                ValidationResult(false, "Password must include letters and numbers")
            else -> ValidationResult(true, "")
        }
    }

    fun validateCompanyCode(companyCode: String, isRequired: Boolean = false): ValidationResult {
        return when {
            isRequired && companyCode.isBlank() -> ValidationResult(false, "Company code is required")
            companyCode.isNotBlank() && companyCode.length < 3 -> ValidationResult(false, "Company code must be at least 3 characters")
            else -> ValidationResult(true, "")
        }
    }

    fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace("+91", "").replace(" ", "").trim()
        return if (cleanPhone.length == 10 && cleanPhone.matches(Regex("^[0-9]+$"))) {
            "+91$cleanPhone"
        } else {
            phone
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)