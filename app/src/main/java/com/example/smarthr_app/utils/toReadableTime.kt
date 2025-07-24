package com.example.smarthr_app.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.toReadableTime(): String {
    val dateTime = LocalDateTime.parse(this)
    val today = LocalDate.now()
    val date = dateTime.toLocalDate()

    return when {
        date == today -> dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        date == today.minusDays(1) -> "Yesterday"
        date.year == today.year -> dateTime.format(DateTimeFormatter.ofPattern("dd MMM"))
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}
