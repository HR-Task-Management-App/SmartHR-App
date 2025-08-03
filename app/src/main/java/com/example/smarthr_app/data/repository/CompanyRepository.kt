package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.SuccessApiResponseMessage
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.first

class CompanyRepository(private val dataStoreManager: DataStoreManager) {

    suspend fun getWaitlistEmployees(): Resource<List<UserDto>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getCompanyWaitlistEmployees("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it.users)
                    } ?: Resource.Error("No data received")
                } else {
                    Resource.Error("Failed to load waitlist employees: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to load waitlist employees: ${e.message}")
        }
    }

    suspend fun getApprovedEmployees(): Resource<List<UserDto>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getApprovedEmployees("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it.users)
                    } ?: Resource.Error("No data received")
                } else {
                    Resource.Error("Failed to load approved employees: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to load approved employees: ${e.message}")
        }
    }

    suspend fun acceptEmployee(employeeId: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.acceptEmployee("Bearer $token", employeeId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Accept successful but no response received")
                } else {
                    Resource.Error("Failed to accept employee: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to accept employee: ${e.message}")
        }
    }

    suspend fun rejectEmployee(employeeId: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.rejectEmployee("Bearer $token", employeeId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Reject successful but no response received")
                } else {
                    Resource.Error("Failed to reject employee: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to reject employee: ${e.message}")
        }
    }

    suspend fun removeEmployee(employeeId: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.removeEmployee("Bearer $token", employeeId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Remove successful but no response received")
                } else {
                    Resource.Error("Failed to remove employee: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to remove employee: ${e.message}")
        }
    }
}