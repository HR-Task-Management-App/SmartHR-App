package com.example.smarthr_app.data.repository

import android.content.Context
import android.net.Uri
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

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
                val errorMessage = parseErrorMessage(response.errorBody()?.string(), response.code())
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
                val errorMessage = parseErrorMessage(response.errorBody()?.string(), response.code(), isLogin = true)
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
                        updateLocalUser(userDto)
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
                        updateLocalUser(userDto)
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

    suspend fun uploadProfileImage(context: Context, imageUri: Uri): Resource<UserDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                // Convert URI to File
                val file = createImageFile(context, imageUri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val response = RetrofitInstance.api.uploadProfileImage("Bearer $token", imagePart)

                // Clean up temporary file
                file.delete()

                if (response.isSuccessful) {
                    response.body()?.let { userDto ->
                        updateLocalUser(userDto)
                        Resource.Success(userDto)
                    } ?: Resource.Error("Upload successful but no user data received")
                } else {
                    Resource.Error("Failed to upload image: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to upload image: ${e.message}")
        }
    }

    private fun createImageFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    suspend fun updateCompanyCode(companyCode: String): Resource<UserDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.updateCompanyCode("Bearer $token", companyCode)
                if (response.isSuccessful) {
                    response.body()?.let { userDto ->
                        updateLocalUser(userDto)
                        Resource.Success(userDto)
                    } ?: Resource.Error("Update successful but no user data received")
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string(), response.code())
                    Resource.Error(errorMessage)
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
                        updateLocalUser(userDto)
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
                        updateLocalUser(userDto)
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

    private suspend fun updateLocalUser(userDto: UserDto) {
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
    }

    private fun parseErrorMessage(errorBody: String?, statusCode: Int, isLogin: Boolean = false): String {
        return try {
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody)
                when {
                    jsonObject.has("message") -> jsonObject.getString("message")
                    jsonObject.has("error") -> jsonObject.getString("error")
                    else -> if (isLogin) getLoginErrorMessage(statusCode) else getDefaultErrorMessage(statusCode)
                }
            } else {
                if (isLogin) getLoginErrorMessage(statusCode) else getDefaultErrorMessage(statusCode)
            }
        } catch (e: Exception) {
            if (isLogin) getLoginErrorMessage(statusCode) else getDefaultErrorMessage(statusCode)
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid input data provided"
            401 -> "Unauthorized access"
            409 -> "Account with this email already exists"
            422 -> "Company code does not exist"
            500 -> "Server error. Please try again later."
            else -> "Request failed. Please try again."
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