package com.example.smarthr_app.data.repository

import android.util.Log
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.Chat
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.data.model.SuccessApiResponseMessage
import com.example.smarthr_app.data.model.UserInfo
import com.example.smarthr_app.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.first

class ChatRepository(private val dataStoreManager: DataStoreManager) {


    suspend fun getMyChatList(companyCode: String): List<Chat>? {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getMyChatList("Bearer $token", companyCode)
                Log.d("ChatList", "Response: ${response.body()}")
                if (response.isSuccessful) {
                   response.body()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllUsers(): List<UserInfo>? {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getAllHrAndEmployeeOfCompany("Bearer $token")
                Log.d("User List", "Response: ${response.body()}")
                if (response.isSuccessful) {
                    response.body()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getChatBetweenUser(
        companyCode: String,
        otherUerId: String
    ): List<ChatMessage>? {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getChatBetweenUser("Bearer $token", companyCode = companyCode, otherUserId = otherUerId)
                Log.i("FatUsers", "Response: ${response.body()}")
                if (response.isSuccessful) {
                    response.body()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markChatAsSeen(
       chatId:String,
       userId:String,
    ): SuccessApiResponseMessage? {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.markChatSeen(token = "Bearer $token", chatId = chatId, userId = userId)
                Log.i("ChatSeen", "Response: ${response.body()}")
                if (response.isSuccessful) {
                    response.body()
                } else {

null                }
            } else {
               null

            }
        } catch (e: Exception) {
            null
        }
    }


}