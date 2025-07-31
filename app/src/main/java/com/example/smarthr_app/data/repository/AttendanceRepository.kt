package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceRepository(private val dataStoreManager: DataStoreManager) {

    // Office Location methods
    suspend fun createOfficeLocation(
        latitude: String,
        longitude: String,
        radius: String
    ): Resource<OfficeLocationResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = OfficeLocationRequestDto(latitude, longitude, radius)
                val response = RetrofitInstance.api.createOfficeLocation("Bearer $token", request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Office location created but no data received")
                } else {
                    Resource.Error("Failed to create office location: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun updateOfficeLocation(
        locationId: String,
        latitude: String,
        longitude: String,
        radius: String
    ): Resource<OfficeLocationResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = OfficeLocationRequestDto(latitude, longitude, radius)
                val response = RetrofitInstance.api.updateOfficeLocation("Bearer $token", locationId, request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Office location updated but no data received")
                } else {
                    Resource.Error("Failed to update office location: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun getCompanyOfficeLocation(): Resource<OfficeLocationResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getCompanyOfficeLocation("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No office location data received")
                } else {
                    Resource.Error("Failed to load office location: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    // Attendance methods
    suspend fun markAttendance(
        type: String,
        latitude: String,
        longitude: String
    ): Resource<AttendanceResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = AttendanceRequestDto(type, latitude, longitude)
                val response = RetrofitInstance.api.markAttendance("Bearer $token", request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Attendance marked but no data received")
                } else {
                    Resource.Error("Failed to mark attendance: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun getEmployeeAttendanceHistory(): Resource<List<AttendanceResponseDto>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getEmployeeAttendanceHistory("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No attendance history data received")
                } else {
                    Resource.Error("Failed to load attendance history: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun getCompanyAttendanceByDate(date: String? = null): Resource<List<AttendanceResponseDto>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getCompanyAttendanceByDate("Bearer $token", date)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No company attendance data received")
                } else {
                    Resource.Error("Failed to load company attendance: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    // Helper method to get today's date in required format
    fun getTodayDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}