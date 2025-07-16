package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class AuthRepository(private val dataStoreManager: DataStoreManager) {

    suspend fun registerUser(request: UserRegisterRequest): Resource<AuthResponse> {
        return try {
            val response = RetrofitInstance.api.registerUser(request)
            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    val loginRequest = LoginRequest(
                        email = request.email,
                        password = request.password
                    )
                    return login(loginRequest)
                } ?: Resource.Error("Registration successful but no user data received")
            } else {
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val jsonObject = JSONObject(errorBody)
                            when {
                                jsonObject.has("message") -> jsonObject.getString("message")
                                jsonObject.has("error") -> jsonObject.getString("error")
                                else -> getDefaultErrorMessage(response.code())
                            }
                        } catch (e: Exception) {
                            getDefaultErrorMessage(response.code())
                        }
                    } else {
                        getDefaultErrorMessage(response.code())
                    }
                } catch (e: Exception) {
                    getDefaultErrorMessage(response.code())
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error("Network error. Please check your connection and try again.")
        }
    }

    suspend fun login(request: LoginRequest): Resource<AuthResponse> {
        return try {
            val response = RetrofitInstance.api.login(request)
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    val user = User(
                        userId = authResponse.user.userId,
                        name = authResponse.user.name,
                        email = authResponse.user.email,
                        phone = authResponse.user.phone,
                        role = if (authResponse.user.role == "ROLE_HR") UserRole.ROLE_HR else UserRole.ROLE_USER,
                        companyCode = authResponse.user.companyCode,
                        imageUrl = authResponse.user.imageUrl,
                        gender = authResponse.user.gender,
                        position = authResponse.user.position,
                        department = authResponse.user.department,
                        waitingCompanyCode = authResponse.user.waitingCompanyCode,
                        joiningStatus = authResponse.user.joiningStatus
                    )
                    dataStoreManager.saveUser(user)
                    dataStoreManager.saveToken(authResponse.token)
                    Resource.Success(authResponse)
                } ?: Resource.Error("Login successful but no data received")
            } else {
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val jsonObject = JSONObject(errorBody)
                            when {
                                jsonObject.has("message") -> jsonObject.getString("message")
                                jsonObject.has("error") -> jsonObject.getString("error")
                                else -> getLoginErrorMessage(response.code())
                            }
                        } catch (e: Exception) {
                            getLoginErrorMessage(response.code())
                        }
                    } else {
                        getLoginErrorMessage(response.code())
                    }
                } catch (e: Exception) {
                    getLoginErrorMessage(response.code())
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error("Network error. Please check your connection and try again.")
        }
    }

    suspend fun getUserProfile(): Resource<UserDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { userDto ->
                        // Update local user data
                        val user = User(
                            userId = userDto.userId,
                            name = userDto.name,
                            email = userDto.email,
                            phone = userDto.phone,
                            role = if (userDto.role == "ROLE_HR") UserRole.ROLE_HR else UserRole.ROLE_USER,
                            companyCode = userDto.companyCode,
                            imageUrl = userDto.imageUrl,
                            gender = userDto.gender,
                            position = userDto.position,
                            department = userDto.department,
                            waitingCompanyCode = userDto.waitingCompanyCode,
                            joiningStatus = userDto.joiningStatus
                        )
                        dataStoreManager.saveUser(user)
                        Resource.Success(userDto)
                    } ?: Resource.Error("No user data received")
                } else {
                    Resource.Error("Failed to load profile: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<UserDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.updateProfile("Bearer $token", request)
                if (response.isSuccessful) {
                    response.body()?.let { userDto ->
                        val user = User(
                            userId = userDto.userId,
                            name = userDto.name,
                            email = userDto.email,
                            phone = userDto.phone,
                            role = if (userDto.role == "ROLE_HR") UserRole.ROLE_HR else UserRole.ROLE_USER,
                            companyCode = userDto.companyCode,
                            imageUrl = userDto.imageUrl,
                            gender = userDto.gender,
                            position = userDto.position,
                            department = userDto.department,
                            waitingCompanyCode = userDto.waitingCompanyCode,
                            joiningStatus = userDto.joiningStatus
                        )
                        dataStoreManager.saveUser(user)
                        Resource.Success(userDto)
                    } ?: Resource.Error("Update successful but no user data received")
                } else {
                    Resource.Error("Failed to update profile: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun updateCompanyCode(companyCode: String): Resource<UserDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.updateCompanyCode("Bearer $token", companyCode)
                if (response.isSuccessful) {
                    response.body()?.let { userDto ->
                        val user = User(
                            userId = userDto.userId,
                            name = userDto.name,
                            email = userDto.email,
                            phone = userDto.phone,
                            role = if (userDto.role == "ROLE_HR") UserRole.ROLE_HR else UserRole.ROLE_USER,
                            companyCode = userDto.companyCode,
                            imageUrl = userDto.imageUrl,
                            gender = userDto.gender,
                            position = userDto.position,
                            department = userDto.department,
                            waitingCompanyCode = userDto.waitingCompanyCode,
                            joiningStatus = userDto.joiningStatus
                        )
                        dataStoreManager.saveUser(user)
                        Resource.Success(userDto)
                    } ?: Resource.Error("Update successful but no user data received")
                } else {
                    Resource.Error("Failed to update company code: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun leaveCompany(): Resource<UserDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.leaveCompany("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { userDto ->
                        val user = User(
                            userId = userDto.userId,
                            name = userDto.name,
                            email = userDto.email,
                            phone = userDto.phone,
                            role = if (userDto.role == "ROLE_HR") UserRole.ROLE_HR else UserRole.ROLE_USER,
                            companyCode = userDto.companyCode,
                            imageUrl = userDto.imageUrl,
                            gender = userDto.gender,
                            position = userDto.position,
                            department = userDto.department,
                            waitingCompanyCode = userDto.waitingCompanyCode,
                            joiningStatus = userDto.joiningStatus
                        )
                        dataStoreManager.saveUser(user)
                        Resource.Success(userDto)
                    } ?: Resource.Error("Leave successful but no user data received")
                } else {
                    Resource.Error("Failed to leave company: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun removeWaitlistCompany(): Resource<UserDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.removeWaitlistCompany("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { userDto ->
                        val user = User(
                            userId = userDto.userId,
                            name = userDto.name,
                            email = userDto.email,
                            phone = userDto.phone,
                            role = if (userDto.role == "ROLE_HR") UserRole.ROLE_HR else UserRole.ROLE_USER,
                            companyCode = userDto.companyCode,
                            imageUrl = userDto.imageUrl,
                            gender = userDto.gender,
                            position = userDto.position,
                            department = userDto.department,
                            waitingCompanyCode = userDto.waitingCompanyCode,
                            joiningStatus = userDto.joiningStatus
                        )
                        dataStoreManager.saveUser(user)
                        Resource.Success(userDto)
                    } ?: Resource.Error("Remove successful but no user data received")
                } else {
                    Resource.Error("Failed to remove from waitlist: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid input data provided"
            401 -> "Unauthorized access"
            409 -> "Account with this email already exists"
            422 -> "Company code does not exist"
            500 -> "Server error. Please try again later."
            else -> "Registration failed. Please try again."
        }
    }

    private fun getLoginErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid email or password format"
            401 -> "Invalid email or password"
            404 -> "Account not found with this email"
            500 -> "Server error. Please try again later."
            else -> "Login failed. Please try again."
        }
    }

    suspend fun logout() {
        dataStoreManager.logout()
    }

    val user: Flow<User?> = dataStoreManager.user
    val isLoggedIn: Flow<Boolean> = dataStoreManager.isLoggedIn
    val token: Flow<String?> = dataStoreManager.token
}