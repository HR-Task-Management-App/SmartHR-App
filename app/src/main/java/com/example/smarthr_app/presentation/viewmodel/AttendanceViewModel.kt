package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.AttendanceResponseDto
import com.example.smarthr_app.data.model.OfficeLocationResponseDto
import com.example.smarthr_app.data.repository.AttendanceRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttendanceViewModel(private val attendanceRepository: AttendanceRepository) : ViewModel() {

    // Office Location States
    private val _officeLocationState = MutableStateFlow<Resource<OfficeLocationResponseDto>?>(null)
    val officeLocationState: StateFlow<Resource<OfficeLocationResponseDto>?> = _officeLocationState

    private val _createOfficeLocationState = MutableStateFlow<Resource<OfficeLocationResponseDto>?>(null)
    val createOfficeLocationState: StateFlow<Resource<OfficeLocationResponseDto>?> = _createOfficeLocationState

    // Attendance States (Updated)
    private val _markAttendanceState = MutableStateFlow<Resource<AttendanceResponseDto>?>(null)
    val markAttendanceState: StateFlow<Resource<AttendanceResponseDto>?> = _markAttendanceState

    private val _attendanceHistoryState = MutableStateFlow<Resource<List<AttendanceResponseDto>>?>(null)
    val attendanceHistoryState: StateFlow<Resource<List<AttendanceResponseDto>>?> = _attendanceHistoryState

    private val _companyAttendanceState = MutableStateFlow<Resource<List<AttendanceResponseDto>>?>(null)
    val companyAttendanceState: StateFlow<Resource<List<AttendanceResponseDto>>?> = _companyAttendanceState

    // Office Location functions (Updated parameter types)
    fun createOfficeLocation(latitude: String, longitude: String, radius: String) {
        viewModelScope.launch {
            _createOfficeLocationState.value = Resource.Loading()
            _createOfficeLocationState.value = attendanceRepository.createOfficeLocation(latitude, longitude, radius)
        }
    }

    fun updateOfficeLocation(locationId: String, latitude: String, longitude: String, radius: String) {
        viewModelScope.launch {
            _createOfficeLocationState.value = Resource.Loading()
            _createOfficeLocationState.value = attendanceRepository.updateOfficeLocation(locationId, latitude, longitude, radius)
        }
    }

    fun loadOfficeLocation() {
        viewModelScope.launch {
            _officeLocationState.value = Resource.Loading()
            _officeLocationState.value = attendanceRepository.getCompanyOfficeLocation()
        }
    }

    // Attendance functions (Updated)
    fun markAttendance(type: String, latitude: String, longitude: String) {
        viewModelScope.launch {
            _markAttendanceState.value = Resource.Loading()
            _markAttendanceState.value = attendanceRepository.markAttendance(type, latitude, longitude)
        }
    }

    fun loadAttendanceHistory() {
        viewModelScope.launch {
            _attendanceHistoryState.value = Resource.Loading()
            _attendanceHistoryState.value = attendanceRepository.getEmployeeAttendanceHistory()
        }
    }

    fun loadCompanyAttendance(date: String? = null) {
        viewModelScope.launch {
            _companyAttendanceState.value = Resource.Loading()
            _companyAttendanceState.value = attendanceRepository.getCompanyAttendanceByDate(date)
        }
    }

    // Clear states
    fun clearCreateOfficeLocationState() {
        _createOfficeLocationState.value = null
    }

    fun clearMarkAttendanceState() {
        _markAttendanceState.value = null
    }
}