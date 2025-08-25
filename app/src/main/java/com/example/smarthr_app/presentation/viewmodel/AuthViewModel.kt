package com.example.smarthr_app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.AuthResponse
import com.example.smarthr_app.data.model.GoogleLoginRequest
import com.example.smarthr_app.data.model.GoogleSignUpRequest
import com.example.smarthr_app.data.model.LoginRequest
import com.example.smarthr_app.data.model.UpdateProfileRequest
import com.example.smarthr_app.data.model.User
import com.example.smarthr_app.data.model.UserDto
import com.example.smarthr_app.data.model.UserRegisterRequest
import com.example.smarthr_app.data.repository.AuthRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val authState: StateFlow<Resource<AuthResponse>?> = _authState

    private val _googleLoginAuthState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val googleLoginAuthState: StateFlow<Resource<AuthResponse>?> = _googleLoginAuthState

    private val _googleSignUpAuthState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val googleSignUpAuthState: StateFlow<Resource<AuthResponse>?> = _googleSignUpAuthState

    private val _registerState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val registerState: StateFlow<Resource<AuthResponse>?> = _registerState

    private val _updateProfileState = MutableStateFlow<Resource<UserDto>?>(null)
    val updateProfileState: StateFlow<Resource<UserDto>?> = _updateProfileState

    private val _updateCompanyState = MutableStateFlow<Resource<UserDto>?>(null)
    val updateCompanyState: StateFlow<Resource<UserDto>?> = _updateCompanyState

    private val _leaveCompanyState = MutableStateFlow<Resource<UserDto>?>(null)
    val leaveCompanyState: StateFlow<Resource<UserDto>?> = _leaveCompanyState

    private val _uploadImageState = MutableStateFlow<Resource<String>?>(null)
    val uploadImageState: StateFlow<Resource<String>?> = _uploadImageState.asStateFlow()

    val user: Flow<User?> = authRepository.user
    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn


    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = authRepository.login(request)
        }
    }

    fun loginWithGoogle(request: GoogleLoginRequest?) {
        if (request == null || request.idToken.isBlank()) {
            _googleLoginAuthState.value = Resource.Error("Invalid ID Token")
            return
        }

        _googleLoginAuthState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(request)
            _googleLoginAuthState.value = result
        }
    }

    fun signUpWithGoogle(request: GoogleSignUpRequest?) {
        if (request == null || request.idToken.isBlank()) {
            _googleSignUpAuthState.value = Resource.Error("Invalid ID Token")
            return
        }

        _googleSignUpAuthState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.signUpWithGoogle(request)
            _googleSignUpAuthState.value = result
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
            val result = authRepository.updateProfile(request)
            _updateProfileState.value = result
            if (result is Resource.Success) {
                refreshProfile()
            }
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uploadImageState.value = Resource.Loading()
            _uploadImageState.value = authRepository.uploadProfileImage(context, imageUri)
        }
    }

    fun updateCompanyCode(companyCode: String) {
        viewModelScope.launch {
            _updateCompanyState.value = Resource.Loading()
            val result = authRepository.updateCompanyCode(companyCode)
            _updateCompanyState.value = result
            if (result is Resource.Success) {
                refreshProfile()
            }
        }
    }

    fun leaveCompany() {
        viewModelScope.launch {
            _leaveCompanyState.value = Resource.Loading()
            val result = authRepository.leaveCompany()
            _leaveCompanyState.value = result
            if (result is Resource.Success) {
                refreshProfile()
            }
        }
    }

    fun removeFromWaitlist() {
        viewModelScope.launch {
            _leaveCompanyState.value = Resource.Loading()
            val result = authRepository.removeWaitlistCompany()
            _leaveCompanyState.value = result
            if (result is Resource.Success) {
                refreshProfile()
            }
        }
    }



    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearAuthState() {
        _authState.value = null
        _googleLoginAuthState.value = null
        _googleSignUpAuthState.value = null
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

    fun clearUploadImageState() {
        _uploadImageState.value = null
    }

}