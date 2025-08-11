package com.example.smarthr_app.data.remote

import com.example.smarthr_app.data.model.AttendanceRequestDto
import com.example.smarthr_app.data.model.AttendanceResponseDto
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.Chat
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.data.model.CommentRequest
import com.example.smarthr_app.data.model.CommentResponse
import com.example.smarthr_app.data.model.CompanyEmployeesResponse
import com.example.smarthr_app.data.model.CompanyWaitlistResponse
import com.example.smarthr_app.data.model.EmployeeLeaveResponseDto
import com.example.smarthr_app.data.model.GoogleLoginRequest
import com.example.smarthr_app.data.model.GoogleSignUpRequest
import com.example.smarthr_app.data.model.HRLeaveResponseDto
import com.example.smarthr_app.data.model.LeaveRequestDto
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.data.model.MeetingCreateRequestDto
import com.example.smarthr_app.data.model.MeetingResponseDto
import com.example.smarthr_app.data.model.MeetingUpdateRequestDto
import com.example.smarthr_app.data.model.OfficeLocationRequestDto
import com.example.smarthr_app.data.model.OfficeLocationResponseDto
import com.example.smarthr_app.data.model.SuccessApiResponseMessage
import com.example.smarthr_app.data.model.TaskFullDetailResponse
import com.example.smarthr_app.data.model.TaskRequest
import com.example.smarthr_app.data.model.TaskResponse
import com.example.smarthr_app.data.model.UpdateProfileRequest
import com.example.smarthr_app.data.model.UpdateTaskStatusRequest
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.model.UserInfo
import com.example.smarthr_app.data.model.UserRegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("users")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/googleLogin")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<AuthResponse>

    @POST("auth/googleSignUp")
    suspend fun signUpWithGoogle(@Body request: GoogleSignUpRequest): Response<AuthResponse>

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
        @Part employees: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part?
    ): Response<TaskResponse> // Changed from TaskFullDetailResponse

    // Alternative method for when no employees are selected
    @Multipart
    @POST("tasks")
    suspend fun createTaskWithoutEmployees(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<TaskResponse> // Changed from TaskFullDetailResponse

    @GET("tasks/{id}")
    suspend fun getTaskById(
        @Header("Authorization") token: String,
        @Path("id") taskId: String
    ): Response<TaskResponse>

    @GET("tasks/userTasks")
    suspend fun getUserTasks(
        @Header("Authorization") token: String
    ): Response<List<TaskResponse>>

    @GET("tasks/companyTasks")
    suspend fun getCompanyTasks(
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

    // Office Location endpoints
    @POST("offices")
    suspend fun createOfficeLocation(
        @Header("Authorization") token: String,
        @Body request: OfficeLocationRequestDto
    ): Response<OfficeLocationResponseDto>

    @PUT("offices/{id}")
    suspend fun updateOfficeLocation(
        @Header("Authorization") token: String,
        @Path("id") locationId: String,
        @Body request: OfficeLocationRequestDto
    ): Response<OfficeLocationResponseDto>

    @GET("offices")
    suspend fun getCompanyOfficeLocation(
        @Header("Authorization") token: String
    ): Response<OfficeLocationResponseDto>

    // Attendance endpoints
    @POST("attendances")
    suspend fun markAttendance(
        @Header("Authorization") token: String,
        @Body request: AttendanceRequestDto
    ): Response<AttendanceResponseDto>

    @GET("attendances/history")
    suspend fun getEmployeeAttendanceHistory(
        @Header("Authorization") token: String
    ): Response<List<AttendanceResponseDto>>

    @GET("attendances/company")
    suspend fun getCompanyAttendanceByDate(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null  // Optional date parameter, defaults to today
    ): Response<List<AttendanceResponseDto>>

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

    // Meeting endpoints
    @POST("meetings/create")
    suspend fun createMeeting(
        @Header("Authorization") token: String,
        @Body request: MeetingCreateRequestDto
    ): Response<MeetingResponseDto>

    @GET("meetings/myMeetings")
    suspend fun getMyMeetings(
        @Header("Authorization") token: String
    ): Response<List<MeetingResponseDto>>

    @GET("meetings/{id}")
    suspend fun getMeetingById(
        @Header("Authorization") token: String,
        @Path("id") meetingId: String
    ): Response<MeetingResponseDto>

    @POST("meetings/{id}")
    suspend fun updateMeeting(
        @Header("Authorization") token: String,
        @Path("id") meetingId: String,
        @Body request: MeetingUpdateRequestDto
    ): Response<MeetingResponseDto>

    @POST("meetings/cancel/{id}")
    suspend fun cancelMeeting(
        @Header("Authorization") token: String,
        @Path("id") meetingId: String
    ): Response<SuccessApiResponseMessage>

    @POST("meetings/respond/{id}")
    suspend fun respondToMeeting(
        @Header("Authorization") token: String,
        @Path("id") meetingId: String,
        @Query("status") status: String // "ACCEPTED" or "DECLINED"
    ): Response<SuccessApiResponseMessage>

}