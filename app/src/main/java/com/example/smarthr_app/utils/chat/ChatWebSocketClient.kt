package com.example.smarthr_app.utils.chat

import android.util.Log
import com.example.smarthr_app.data.model.ChatMessage
import com.example.smarthr_app.data.model.SeenMessage
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class ChatWebSocketClient(
    private val userId: String,
    private val onMessageReceived: (ChatMessage) -> Unit,
    private val handleSeenMessage: (SeenMessage) -> Unit
) : WebSocketListener() {

    private val gson = Gson()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val url = "wss://smarthr-backend-jx0v.onrender.com/chat-websocket/websocket"
    private var isConnected = false


    fun connect() {
        if (isConnected) return
        isConnected = true
        Log.d("WebSocket", "Connecting to: $url")
        val request = Request
            .Builder()
            .addHeader("user-id", userId)
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, this)
    }

    fun disconnect() {
        isConnected = false
        webSocket?.close(1000, "Client disconnected")
    }

    fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        companyCode: String,
        type: String = "TEXT"
    ) {
        val message = mapOf(
            "sender" to senderId,
            "receiver" to receiverId,
            "content" to content,
            "companyCode" to companyCode,
            "messageType" to type
        )
        val json = gson.toJson(message)

        val stompFrame = buildStompFrame(
            command = "SEND",
            headers = mapOf("destination" to "/msg/chat/send"),
            body = json
        )
        webSocket?.send(stompFrame)
    }

    fun sendSeenMessageInfo(
        chatId:String,
        userId: String
    ) {
        val message = mapOf(
            "chatId" to chatId,
            "userId" to userId
        )
        val json = gson.toJson(message)

        val stompFrame = buildStompFrame(
            command = "SEND",
            headers = mapOf("destination" to "/msg/chat/seen"),
            body = json
        )
        webSocket?.send(stompFrame)
    }

    private fun subscribeToIncomingMessages(){
        subscribeToTopic("/user/queue/messages","sub-messages")
        subscribeToTopic("/user/queue/message-seen","sub-notification")
    }

    private fun subscribeToTopic(topic:String,id:String) {
        val frame = buildStompFrame(
            command = "SUBSCRIBE",
            headers = mapOf(
                "id" to id,
                "destination" to topic,
            )
        )
        webSocket?.send(frame)
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "WebSocket opened")

        // Send STOMP CONNECT frame
        val connectFrame = buildStompFrame(
            command = "CONNECT",
            headers = mapOf(
                "accept-version" to "1.2",
                "host" to "localhost",
                "heart-beat" to "10000,10000",
                "user-id" to userId
            )
        )
        webSocket.send(connectFrame)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "Received: $text")

        when {
            text.startsWith("CONNECTED") -> {
                Log.d("WebSocket", "STOMP connected")
                subscribeToIncomingMessages()
            }

            text.startsWith("MESSAGE") -> {
                val headers = text.lines()
                val destination = headers.firstOrNull { it.startsWith("destination:") }
                    ?.substringAfter("destination:")

                val body = text.substringAfter("\n\n").substringBeforeLast("\u0000")
                when (destination) {
                    "/user/queue/messages" -> {
                        val message = Gson().fromJson(body, ChatMessage::class.java)
                        onMessageReceived(message)
                    }
                    "/user/queue/message-seen" -> {
                        val message = Gson().fromJson(body, SeenMessage::class.java)
                        handleSeenMessage(message)
                    }
                    else -> Log.w("WebSocket", "Unhandled destination: $destination")
                }
                Log.d("WebSocket", "Message body: $body")
            }

            text.startsWith("RECEIPT") -> {
                Log.d("WebSocket", "Receipt received")
            }

            text.startsWith("ERROR") -> {
                Log.e("WebSocket", "STOMP error: $text")
            }

            else -> Log.d("WebSocket", "Unhandled frame: $text")
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "WebSocket error", t)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "WebSocket closing: $reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "WebSocket closed: $reason")
    }

    private fun buildStompFrame(
        command: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null
    ): String {
        val builder = StringBuilder()
        builder.append(command).append("\n")
        headers.forEach { (key, value) ->
            builder.append("$key:$value\n")
        }
        builder.append("\n")
        if (body != null) builder.append(body)
        builder.append("\u0000") // STOMP frame terminator
        return builder.toString()
    }
}