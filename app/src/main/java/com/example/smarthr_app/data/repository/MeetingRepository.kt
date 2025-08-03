package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.MeetingCreateRequestDto
import com.example.smarthr_app.data.model.MeetingResponseDto
import com.example.smarthr_app.data.model.MeetingUpdateRequestDto
import com.example.smarthr_app.data.model.SuccessApiResponseMessage
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.first

class MeetingRepository(private val dataStoreManager: DataStoreManager) {

    suspend fun createMeeting(
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        meetingLink: String?,
        participants: List<String>
    ): Resource<MeetingResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = MeetingCreateRequestDto(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    meetingLink = meetingLink,
                    participants = participants
                )
                val response = RetrofitInstance.api.createMeeting("Bearer $token", request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Meeting created but no data received")
                } else {
                    Resource.Error("Failed to create meeting: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun getMyMeetings(): Resource<List<MeetingResponseDto>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getMyMeetings("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No meetings data received")
                } else {
                    Resource.Error("Failed to load meetings: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun getMeetingById(meetingId: String): Resource<MeetingResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getMeetingById("Bearer $token", meetingId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No meeting data received")
                } else {
                    Resource.Error("Failed to load meeting: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun updateMeeting(
        meetingId: String,
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        meetingLink: String?,
        participants: List<String>
    ): Resource<MeetingResponseDto> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = MeetingUpdateRequestDto(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    meetingLink = meetingLink,
                    participants = participants
                )
                val response = RetrofitInstance.api.updateMeeting("Bearer $token", meetingId, request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Meeting updated but no data received")
                } else {
                    Resource.Error("Failed to update meeting: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun cancelMeeting(meetingId: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.cancelMeeting("Bearer $token", meetingId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Meeting cancelled but no confirmation received")
                } else {
                    Resource.Error("Failed to cancel meeting: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

    suspend fun respondToMeeting(meetingId: String, status: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.respondToMeeting("Bearer $token", meetingId, status)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Response sent but no confirmation received")
                } else {
                    Resource.Error("Failed to respond to meeting: ${response.message()}")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }
}