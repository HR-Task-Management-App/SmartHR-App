package com.example.smarthr_app.utils

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.ConferenceData
import com.google.api.services.calendar.model.ConferenceSolutionKey
import com.google.api.services.calendar.model.CreateConferenceRequest
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class GoogleCalendarService(private val context: Context) {

    suspend fun createMeetingWithGoogleMeet(
        title: String,
        description: String,
        startDateTime: Date,
        endDateTime: Date,
        attendeeEmails: List<String>
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken()
                if (accessToken == null) {
                    return@withContext Result.failure(Exception("Failed to get access token"))
                }

                val credential = GoogleCredential().setAccessToken(accessToken)
                val transport = NetHttpTransport()
                val jsonFactory = GsonFactory.getDefaultInstance()

                val service = Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName("SmartHR")
                    .build()

                val event = Event().apply {
                    summary = title
                    this.description = description

                    // Set start time
                    val start = EventDateTime()
                        .setDateTime(DateTime(startDateTime))
                        .setTimeZone("UTC")
                    this.start = start

                    // Set end time
                    val end = EventDateTime()
                        .setDateTime(DateTime(endDateTime))
                        .setTimeZone("UTC")
                    this.end = end

                    // Add attendees
                    val attendees = attendeeEmails.map {
                        EventAttendee().setEmail(it)
                    }
                    this.attendees = attendees

                    // Configure Google Meet
                    val conferenceData = ConferenceData()
                    val createRequest = CreateConferenceRequest().apply {
                        requestId = UUID.randomUUID().toString()
                        conferenceSolutionKey = ConferenceSolutionKey().setType("hangoutsMeet")
                    }
                    conferenceData.createRequest = createRequest
                    this.conferenceData = conferenceData
                }

                // Create the event
                val createdEvent = service.events().insert("primary", event)
                    .setConferenceDataVersion(1)
                    .execute()

                // Return the Google Meet link
                val meetLink = createdEvent.hangoutLink
                    ?: createdEvent.conferenceData?.entryPoints
                        ?.find { it.entryPointType == "video" }?.uri
                    ?: throw Exception("Failed to generate Google Meet link")

                Result.success(meetLink)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account?.account == null) {
                    return@withContext null
                }

                GoogleAuthUtil.getToken(
                    context,
                    account.account!!,
                    "oauth2:https://www.googleapis.com/auth/calendar.events"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun isSignedInWithCalendarPermission(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(
            account,
            com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar.events")
        )
    }
}