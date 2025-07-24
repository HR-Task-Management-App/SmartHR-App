package com.example.smarthr_app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

fun createNotificationChannel(context: Context) {
    val name = "Messages"
    val descriptionText = "Chat message notifications"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel("chat_messages", name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
