package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName

data class TaskRequest(
    val title: String,
    val description: String,
    val priority: String,
    val status: String? = null,
    val employees: List<String>
)

data class UpdateTaskStatusRequest(
    val status: String
)

data class TaskResponse(
    val id: String,
    val imageUrl: String?,
    val companyCode: String,
    val title: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val assignee: String
)

data class TaskFullDetailResponse(
    val id: String,
    val imageUrl: String?,
    val companyCode: String,
    val title: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val assignee: UserInfo,
    val employees: List<UserInfo>
)

data class CommentRequest(
    val taskId: String,
    val text: String
)

data class CommentResponse(
    val id: String,
    val taskId: String,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
    val author: UserInfo
)

data class UserInfo(
    val id: String,
    val name: String,
    val email: String,
    val imageUrl: String?
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskStatus {
    NOT_STARTED, IN_PROGRESS, FINISHED
}