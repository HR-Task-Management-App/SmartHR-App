package com.example.smarthr_app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.repository.TaskRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _createTaskState = MutableStateFlow<Resource<TaskFullDetailResponse>?>(null)
    val createTaskState: StateFlow<Resource<TaskFullDetailResponse>?> = _createTaskState

    private val _tasksState = MutableStateFlow<Resource<List<TaskResponse>>?>(null)
    val tasksState: StateFlow<Resource<List<TaskResponse>>?> = _tasksState

    private val _taskDetailState = MutableStateFlow<Resource<TaskResponse>?>(null)
    val taskDetailState: StateFlow<Resource<TaskResponse>?> = _taskDetailState

    private val _updateTaskState = MutableStateFlow<Resource<TaskResponse>?>(null)
    val updateTaskState: StateFlow<Resource<TaskResponse>?> = _updateTaskState

    private val _deleteTaskState = MutableStateFlow<Resource<SuccessApiResponseMessage>?>(null)
    val deleteTaskState: StateFlow<Resource<SuccessApiResponseMessage>?> = _deleteTaskState

    private val _commentsState = MutableStateFlow<Resource<List<CommentResponse>>?>(null)
    val commentsState: StateFlow<Resource<List<CommentResponse>>?> = _commentsState

    private val _addCommentState = MutableStateFlow<Resource<CommentResponse>?>(null)
    val addCommentState: StateFlow<Resource<CommentResponse>?> = _addCommentState

    fun createTask(
        context: Context,
        title: String,
        description: String,
        priority: String,
        status: String,
        employees: List<String>,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _createTaskState.value = Resource.Loading()
            _createTaskState.value = taskRepository.createTask(
                context, title, description, priority, status, employees, imageUri
            )
        }
    }

    fun loadUserTasks() {
        viewModelScope.launch {
            _tasksState.value = Resource.Loading()
            _tasksState.value = taskRepository.getUserTasks()
        }
    }

    fun loadTaskById(taskId: String) {
        viewModelScope.launch {
            _taskDetailState.value = Resource.Loading()
            _taskDetailState.value = taskRepository.getTaskById(taskId)
        }
    }

    fun updateTask(
        taskId: String,
        title: String,
        description: String,
        priority: String,
        status: String,
        employees: List<String>
    ) {
        viewModelScope.launch {
            _updateTaskState.value = Resource.Loading()
            _updateTaskState.value = taskRepository.updateTask(
                taskId, title, description, priority, status, employees
            )
        }
    }

    fun updateTaskStatus(taskId: String, status: String) {
        viewModelScope.launch {
            _updateTaskState.value = Resource.Loading()
            _updateTaskState.value = taskRepository.updateTaskStatus(taskId, status)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _deleteTaskState.value = Resource.Loading()
            _deleteTaskState.value = taskRepository.deleteTask(taskId)
        }
    }

    fun loadTaskComments(taskId: String) {
        viewModelScope.launch {
            _commentsState.value = Resource.Loading()
            _commentsState.value = taskRepository.getTaskComments(taskId)
        }
    }

    fun addComment(taskId: String, text: String) {
        viewModelScope.launch {
            _addCommentState.value = Resource.Loading()
            _addCommentState.value = taskRepository.addComment(taskId, text)
        }
    }

    fun clearCreateTaskState() {
        _createTaskState.value = null
    }

    fun clearUpdateTaskState() {
        _updateTaskState.value = null
    }

    fun clearDeleteTaskState() {
        _deleteTaskState.value = null
    }

    fun clearAddCommentState() {
        _addCommentState.value = null
    }
}