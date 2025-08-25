package com.example.smarthr_app.utils

import android.content.Context
import android.widget.Toast

object ToastHelper {

    fun showSuccessToast(context: Context, message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_SHORT).show()
    }

    fun showErrorToast(context: Context, message: String) {
        val displayMessage = when {
            message.contains("Calendar permission", ignoreCase = true) ->
                "📅 $message Please sign out and sign in again to grant calendar access."
            message.contains("conflict", ignoreCase = true) -> "⚠️ Time Conflict: $message"
            message.contains("overlap", ignoreCase = true) -> "⚠️ Schedule Overlap: $message"
            message.contains("already scheduled", ignoreCase = true) -> "⚠️ Already Scheduled: $message"
            message.contains("Google Meet", ignoreCase = true) -> "🎥 Google Meet Error: $message"
            else -> "❌ $message"
        }
        Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show()
    }

    fun showInfoToast(context: Context, message: String) {
        Toast.makeText(context, "ℹ️ $message", Toast.LENGTH_SHORT).show()
    }

    fun showWarningToast(context: Context, message: String) {
        Toast.makeText(context, "⚠️ $message", Toast.LENGTH_SHORT).show()
    }

    fun showMeetLinkToast(context: Context, message: String) {
        Toast.makeText(context, "🎥 $message", Toast.LENGTH_SHORT).show()
    }
}