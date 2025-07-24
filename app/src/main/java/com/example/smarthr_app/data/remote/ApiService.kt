package com.example.smarthr_app.data.remote

import com.example.smarthr_app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import okhttp3.RequestBody

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

    @Multipart
    @POST("users/profile-image")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
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

    // Task endpoints

    @Multipart
    @POST("tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("status") status: RequestBody,
        @Part employees: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part?
    ): Response<TaskFullDetailResponse>

    // Alternative method for when no employees are selected
    @Multipart
    @POST("tasks")
    suspend fun createTaskWithoutEmployees(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("status") status: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<TaskFullDetailResponse>

    @GET("tasks/{id}")
    suspend fun getTaskById(
        @Header("Authorization") token: String,
        @Path("id") taskId: String
    ): Response<TaskResponse>

    @GET("tasks/user")
    suspend fun getUserTasks(
        @Header("Authorization") token: String
    ): Response<List<TaskResponse>>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") taskId: String,
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") taskId: String
    ): Response<SuccessApiResponseMessage>

    @PUT("tasks/status/{id}")
    suspend fun updateTaskStatus(
        @Header("Authorization") token: String,
        @Path("id") taskId: String,
        @Body request: UpdateTaskStatusRequest
    ): Response<TaskResponse>

    // Comment endpoints
    @POST("comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Body request: CommentRequest
    ): Response<CommentResponse>

    @GET("comments/{taskId}")
    suspend fun getTaskComments(
        @Header("Authorization") token: String,
        @Path("taskId") taskId: String
    ): Response<List<CommentResponse>>

    // Leave endpoints
    @POST("leaves")
    suspend fun submitLeaveRequest(
        @Header("Authorization") token: String,
        @Body request: LeaveRequestDto
    ): Response<EmployeeLeaveResponseDto>

    @GET("leaves")
    suspend fun getEmployeeLeaves(
        @Header("Authorization") token: String
    ): Response<List<EmployeeLeaveResponseDto>>

    @GET("leaves/company")
    suspend fun getCompanyLeaves(
        @Header("Authorization") token: String
    ): Response<List<HRLeaveResponseDto>>

    @POST("leaves/{leaveId}")
    suspend fun updateLeaveRequest(
        @Header("Authorization") token: String,
        @Path("leaveId") leaveId: String,
        @Body request: LeaveRequestDto
    ): Response<EmployeeLeaveResponseDto>

    @POST("leaves/status/{leaveId}/{status}")
    suspend fun updateLeaveStatus(
        @Header("Authorization") token: String,
        @Path("leaveId") leaveId: String,
        @Path("status") status: String
    ): Response<SuccessApiResponseMessage>

    @POST("leaves/response/{leaveId}")
    suspend fun removeHRResponse(
        @Header("Authorization") token: String,
        @Path("leaveId") leaveId: String
    ): Response<SuccessApiResponseMessage>

    @GET("chats/myChats")
    suspend fun getMyChatList(
        @Header("Authorization") token: String,
        @Query("companyCode") companyCode: String
        ) : Response<List<Chat>>

    @GET("companies/everybody")
    suspend fun getAllHrAndEmployeeOfCompany(
        @Header("Authorization") token: String,
        ) : Response<List<UserInfo>>

    @GET("chats/history")
    suspend fun getChatBetweenUser(
        @Header("Authorization") token: String,
        @Query("companyCode") companyCode: String,
        @Query("otherUserId") otherUserId: String
    ) : Response<List<ChatMessage>>

    @PUT("chats/seen/{chatId}")
    suspend fun markChatSeen(
        @Header("Authorization") token: String,
        @Path("chatId") chatId:String,
        @Query("userId") userId : String,
        ): Response<SuccessApiResponseMessage>
    
}