package com.example.smarthr_app.data.model

data class ChatMessage(
val id: String, // message id
val chatId:String,
val sender: UserInfo,
val receiver: UserInfo,
val content: String,
val messageType: String,
val companyCode: String,
val timestamp: String,
val messageStatus: String
)