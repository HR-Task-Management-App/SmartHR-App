package com.example.smarthr_app.data.repository

import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.Chat
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.data.model.SuccessApiResponseMessage
import com.example.smarthr_app.data.model.UserInfo
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class ChatRepository(private val dataStoreManager: DataStoreManager) {

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody)
                jsonObject.optString("error", "Unknown error occurred")
            } else {
                "Unknown error occurred"
            }
        } catch (e: Exception) {
            "Failed to parse error message"
        }
    }

    suspend fun getMyChatList(companyCode: String): Resource<List<Chat>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getMyChatList("Bearer $token", companyCode)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No data received")
                } else {
                    val message = parseErrorMessage(response.errorBody()?.string())
                    Resource.Error(message)
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to load chat list: ${e.message}")
        }
    }

    suspend fun getAllUsers(): Resource<List<UserInfo>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getAllHrAndEmployeeOfCompany("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No data received")
                } else {
                    val message = parseErrorMessage(response.errorBody()?.string())
                    Resource.Error(message)
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to load user list: ${e.message}")
        }
    }

    suspend fun getChatBetweenUser(
        companyCode: String,
        otherUerId: String
    ): Resource<List<ChatMessage>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getChatBetweenUser("Bearer $token", companyCode = companyCode, otherUserId = otherUerId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No data received")
                } else {
                    val message = parseErrorMessage(response.errorBody()?.string())
                    Resource.Error(message)
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to load chat: ${e.message}")
        }
    }

    suspend fun markChatAsSeen(
        chatId: String,
        userId: String,
    ): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.markChatSeen(token = "Bearer $token", chatId = chatId, userId = userId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No data received")
                } else {
                    val message = parseErrorMessage(response.errorBody()?.string())
                    Resource.Error(message)
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to mark chat as seen: ${e.message}")
        }
    }
}
