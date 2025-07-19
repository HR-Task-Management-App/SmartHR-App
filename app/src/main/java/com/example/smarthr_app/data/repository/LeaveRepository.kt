package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.first

class LeaveRepository(private val dataStoreManager: DataStoreManager) {

    suspend fun submitLeaveRequest(leaveRequest: LeaveRequestDto): Resource<EmployeeLeaveResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.submitLeaveRequest("Bearer $token", leaveRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Leave submitted but no data received")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Resource.Error("Failed to submit leave: $errorBody")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun getEmployeeLeaves(): Resource<List<EmployeeLeaveResponseDto>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getEmployeeLeaves("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No leave data received")
                } else {
                    Resource.Error("Failed to load leaves: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun getCompanyLeaves(): Resource<List<HRLeaveResponseDto>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getCompanyLeaves("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No leave data received")
                } else {
                    Resource.Error("Failed to load leaves: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun updateLeaveRequest(leaveId: String, leaveRequest: LeaveRequestDto): Resource<EmployeeLeaveResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.updateLeaveRequest("Bearer $token", leaveId, leaveRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Leave updated but no data received")
                } else {
                    Resource.Error("Failed to update leave: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun updateLeaveStatus(leaveId: String, status: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.updateLeaveStatus("Bearer $token", leaveId, status)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Status updated but no confirmation received")
                } else {
                    Resource.Error("Failed to update status: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun removeHRResponse(leaveId: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.removeHRResponse("Bearer $token", leaveId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response removed but no confirmation received")
                } else {
                    Resource.Error("Failed to remove response: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }
}