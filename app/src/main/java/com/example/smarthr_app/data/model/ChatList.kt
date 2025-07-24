package com.example.smarthr_app.data.model

data class Chat(
    val companyCode: String,
    val id: String, //chat id
    val lastMessage: String,
    val lastMessageStatus: String, //SEEN,DELIVERED
    val lastMessageType: String,
    val lastMessageSender:String,
    val lastUpdated: String,
    val user1: UserInfo, // me
    val user2: UserInfo
)