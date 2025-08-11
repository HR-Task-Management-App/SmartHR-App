package com.example.smarthr_app.data.model


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

data class EmployeeTaskInfo(
    val id: String,
    val name: String,
    val email: String,
    val imageUrl: String?,
    val taskStatus: TaskStatus? = null // Employee's individual status for this task
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
    val assignee: UserInfo, // HR who created the task
    val employees: List<EmployeeTaskInfo>? = null // Employees with their individual status
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
    val employees: List<EmployeeTaskInfo>
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