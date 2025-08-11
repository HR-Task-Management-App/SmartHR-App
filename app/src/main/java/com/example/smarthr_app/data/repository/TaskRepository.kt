package com.example.smarthr_app.data.repository

import android.content.Context
import android.net.Uri
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class TaskRepository(private val dataStoreManager: DataStoreManager) {

    suspend fun createTask(
        context: Context,
        title: String,
        description: String,
        priority: String,
        status: String,
        employees: List<String>,
        imageUri: Uri?
    ): Resource<TaskResponse> { // Changed return type
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val priorityBody = priority.toRequestBody("text/plain".toMediaTypeOrNull())

                val employeeParts = employees.map { employeeId ->
                    MultipartBody.Part.createFormData("employees", employeeId)
                }

                val imagePart = imageUri?.let { uri ->
                    val file = createImageFile(context, uri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    file.deleteOnExit()
                    part
                }

                val response = if (employeeParts.isNotEmpty()) {
                    RetrofitInstance.api.createTask(
                        "Bearer $token",
                        titleBody,
                        descBody,
                        priorityBody,
                        employeeParts,
                        imagePart
                    )
                } else {
                    RetrofitInstance.api.createTaskWithoutEmployees(
                        "Bearer $token",
                        titleBody,
                        descBody,
                        priorityBody,
                        imagePart
                    )
                }

                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Task created but no data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to create task: ${response.code()}"
                    }
                    Resource.Error("Failed to create task: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getUserTasks(): Resource<List<TaskResponse>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getUserTasks("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { taskList ->
                        Resource.Success(taskList)
                    } ?: Resource.Error("No tasks data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to load tasks: ${response.code()}"
                    }
                    Resource.Error("Failed to load tasks: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getCompanyTasks(): Resource<List<TaskResponse>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getCompanyTasks("Bearer $token")
                if (response.isSuccessful) {
                    response.body()?.let { taskList ->
                        Resource.Success(taskList)
                    } ?: Resource.Error("No tasks data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to load company tasks: ${response.code()}"
                    }
                    Resource.Error("Failed to load company tasks: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getTaskById(taskId: String): Resource<TaskResponse> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getTaskById("Bearer $token", taskId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No task data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to load task: ${response.code()}"
                    }
                    Resource.Error("Failed to load task: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        priority: String,
        status: String,
        employees: List<String>
    ): Resource<TaskResponse> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = TaskRequest(title, description, priority, status, employees)
                val response = RetrofitInstance.api.updateTask("Bearer $token", taskId, request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Task updated but no data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to update task: ${response.code()}"
                    }
                    Resource.Error("Failed to update task: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Resource<TaskResponse> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = UpdateTaskStatusRequest(status)
                val response = RetrofitInstance.api.updateTaskStatus("Bearer $token", taskId, request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Status updated but no data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to update status: ${response.code()}"
                    }
                    Resource.Error("Failed to update status: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun deleteTask(taskId: String): Resource<SuccessApiResponseMessage> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.deleteTask("Bearer $token", taskId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Task deleted but no confirmation received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to delete task: ${response.code()}"
                    }
                    Resource.Error("Failed to delete task: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun addComment(taskId: String, text: String): Resource<CommentResponse> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val request = CommentRequest(taskId, text)
                val response = RetrofitInstance.api.addComment("Bearer $token", request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Comment added but no data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to add comment: ${response.code()}"
                    }
                    Resource.Error("Failed to add comment: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getTaskComments(taskId: String): Resource<List<CommentResponse>> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                val response = RetrofitInstance.api.getTaskComments("Bearer $token", taskId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("No comments data received")
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Unknown error occurred"
                    } catch (e: Exception) {
                        "Failed to load comments: ${response.code()}"
                    }
                    Resource.Error("Failed to load comments: $errorMessage")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }


    suspend fun checkAndUpdateTaskCompletion(taskId: String): Resource<TaskResponse> {
        return try {
            val token = dataStoreManager.token.first()
            if (token != null) {
                // First get the current task
                val taskResponse = RetrofitInstance.api.getTaskById("Bearer $token", taskId)
                if (taskResponse.isSuccessful) {
                    taskResponse.body()?.let { task ->
                        // Check if all employees have finished
                        val allEmployeesFinished = task.employees?.all { it.taskStatus == TaskStatus.FINISHED } == true

                        if (allEmployeesFinished && task.status != TaskStatus.FINISHED) {
                            // Auto-complete the task
                            updateTaskStatus(taskId, TaskStatus.FINISHED.name)
                        } else {
                            Resource.Success(task)
                        }
                    } ?: Resource.Error("No task data received")
                } else {
                    Resource.Error("Failed to load task")
                }
            } else {
                Resource.Error("No authentication token found")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }



    private fun createImageFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
}