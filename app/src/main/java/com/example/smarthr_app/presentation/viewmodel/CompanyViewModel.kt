package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.repository.CompanyRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompanyViewModel(private val companyRepository: CompanyRepository) : ViewModel() {

    private val _waitlistEmployees = MutableStateFlow<Resource<List<UserDto>>?>(null)
    val waitlistEmployees: StateFlow<Resource<List<UserDto>>?> = _waitlistEmployees

    private val _approvedEmployees = MutableStateFlow<Resource<List<UserDto>>?>(null)
    val approvedEmployees: StateFlow<Resource<List<UserDto>>?> = _approvedEmployees

    private val _actionState = MutableStateFlow<Resource<SuccessApiResponseMessage>?>(null)
    val actionState: StateFlow<Resource<SuccessApiResponseMessage>?> = _actionState

    fun loadWaitlistEmployees() {
        viewModelScope.launch {
            _waitlistEmployees.value = Resource.Loading()
            _waitlistEmployees.value = companyRepository.getWaitlistEmployees()
        }
    }

    fun loadApprovedEmployees() {
        viewModelScope.launch {
            _approvedEmployees.value = Resource.Loading()
            _approvedEmployees.value = companyRepository.getApprovedEmployees()
        }
    }

    fun acceptEmployee(employeeId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            _actionState.value = companyRepository.acceptEmployee(employeeId)
        }
    }

    fun rejectEmployee(employeeId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            _actionState.value = companyRepository.rejectEmployee(employeeId)
        }
    }

    fun removeEmployee(employeeId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            _actionState.value = companyRepository.removeEmployee(employeeId)
        }
    }

    fun clearActionState() {
        _actionState.value = null
    }

    fun refreshAll() {
        loadWaitlistEmployees()
        loadApprovedEmployees()
    }
}