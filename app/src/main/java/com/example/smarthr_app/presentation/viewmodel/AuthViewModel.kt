package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val authState: StateFlow<Resource<AuthResponse>?> = _authState

    private val _registerState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val registerState: StateFlow<Resource<AuthResponse>?> = _registerState

    private val _updateProfileState = MutableStateFlow<Resource<UserDto>?>(null)
    val updateProfileState: StateFlow<Resource<UserDto>?> = _updateProfileState

    private val _updateCompanyState = MutableStateFlow<Resource<UserDto>?>(null)
    val updateCompanyState: StateFlow<Resource<UserDto>?> = _updateCompanyState

    private val _leaveCompanyState = MutableStateFlow<Resource<UserDto>?>(null)
    val leaveCompanyState: StateFlow<Resource<UserDto>?> = _leaveCompanyState

    val user: Flow<User?> = authRepository.user
    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = authRepository.login(request)
        }
    }

    fun registerUser(request: UserRegisterRequest) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            _registerState.value = authRepository.registerUser(request)
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile()
        }
    }

    fun updateProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _updateProfileState.value = Resource.Loading()
            _updateProfileState.value = authRepository.updateProfile(request)
        }
    }

    fun updateCompanyCode(companyCode: String) {
        viewModelScope.launch {
            _updateCompanyState.value = Resource.Loading()
            _updateCompanyState.value = authRepository.updateCompanyCode(companyCode)
        }
    }

    fun leaveCompany() {
        viewModelScope.launch {
            _leaveCompanyState.value = Resource.Loading()
            _leaveCompanyState.value = authRepository.leaveCompany()
        }
    }

    fun removeFromWaitlist() {
        viewModelScope.launch {
            _leaveCompanyState.value = Resource.Loading()
            _leaveCompanyState.value = authRepository.removeWaitlistCompany()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearAuthState() {
        _authState.value = null
    }

    fun clearRegisterState() {
        _registerState.value = null
    }

    fun clearUpdateProfileState() {
        _updateProfileState.value = null
    }

    fun clearUpdateCompanyState() {
        _updateCompanyState.value = null
    }

    fun clearLeaveCompanyState() {
        _leaveCompanyState.value = null
    }
}