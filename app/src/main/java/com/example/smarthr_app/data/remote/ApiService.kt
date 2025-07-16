package com.example.smarthr_app.data.remote

import com.example.smarthr_app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("users")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("users")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserDto>

    @PATCH("users")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserDto>

    @GET("companies/empWaitlist")
    suspend fun getCompanyWaitlistEmployees(@Header("Authorization") token: String): Response<CompanyWaitlistResponse>

    @GET("companies/employees")
    suspend fun getApprovedEmployees(@Header("Authorization") token: String): Response<CompanyEmployeesResponse>

    @POST("companies/acceptEmployee/{userId}")
    suspend fun acceptEmployee(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<SuccessApiResponseMessage>

    @POST("companies/rejectEmployee/{userId}")
    suspend fun rejectEmployee(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<SuccessApiResponseMessage>

    @DELETE("companies/removeEmployee/{userId}")
    suspend fun removeEmployee(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<SuccessApiResponseMessage>

    @PATCH("users/{companyCode}")
    suspend fun updateCompanyCode(
        @Header("Authorization") token: String,
        @Path("companyCode") companyCode: String
    ): Response<UserDto>

    @PATCH("users/leave-company")
    suspend fun leaveCompany(@Header("Authorization") token: String): Response<UserDto>

    @PATCH("users/remove-wait-company")
    suspend fun removeWaitlistCompany(@Header("Authorization") token: String): Response<UserDto>
}