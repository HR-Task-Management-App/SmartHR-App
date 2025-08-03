package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.EmployeeLeaveResponseDto
import com.example.smarthr_app.data.model.HRLeaveResponseDto
import com.example.smarthr_app.data.model.LeaveRequestDto
import com.example.smarthr_app.data.model.LeaveSummary
import com.example.smarthr_app.data.model.SuccessApiResponseMessage
import com.example.smarthr_app.data.repository.LeaveRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaveViewModel(private val leaveRepository: LeaveRepository) : ViewModel() {

    private val _submitLeaveState = MutableStateFlow<Resource<EmployeeLeaveResponseDto>?>(null)
    val submitLeaveState: StateFlow<Resource<EmployeeLeaveResponseDto>?> = _submitLeaveState

    private val _employeeLeavesState = MutableStateFlow<Resource<List<EmployeeLeaveResponseDto>>?>(null)
    val employeeLeavesState: StateFlow<Resource<List<EmployeeLeaveResponseDto>>?> = _employeeLeavesState

    private val _companyLeavesState = MutableStateFlow<Resource<List<HRLeaveResponseDto>>?>(null)
    val companyLeavesState: StateFlow<Resource<List<HRLeaveResponseDto>>?> = _companyLeavesState

    private val _updateLeaveState = MutableStateFlow<Resource<EmployeeLeaveResponseDto>?>(null)
    val updateLeaveState: StateFlow<Resource<EmployeeLeaveResponseDto>?> = _updateLeaveState

    private val _leaveActionState = MutableStateFlow<Resource<SuccessApiResponseMessage>?>(null)
    val leaveActionState: StateFlow<Resource<SuccessApiResponseMessage>?> = _leaveActionState

    private val _leaveSummary = MutableStateFlow(LeaveSummary())
    val leaveSummary: StateFlow<LeaveSummary> = _leaveSummary

    fun submitLeaveRequest(leaveRequest: LeaveRequestDto) {
        viewModelScope.launch {
            _submitLeaveState.value = Resource.Loading()
            _submitLeaveState.value = leaveRepository.submitLeaveRequest(leaveRequest)
        }
    }

    fun loadEmployeeLeaves() {
        viewModelScope.launch {
            _employeeLeavesState.value = Resource.Loading()
            val result = leaveRepository.getEmployeeLeaves()
            _employeeLeavesState.value = result

            // Calculate leave summary
            if (result is Resource.Success) {
                val approvedLeaves = result.data.filter { it.status == "APPROVED" }
                val totalDaysUsed = approvedLeaves.sumOf { leave ->
                    calculateLeaveDays(leave.startDate, leave.endDate)
                }
                _leaveSummary.value = LeaveSummary(
                    totalLeave = 20,
                    leaveUsed = totalDaysUsed,
                    available = 20 - totalDaysUsed
                )
            }
        }
    }

    fun loadCompanyLeaves() {
        viewModelScope.launch {
            _companyLeavesState.value = Resource.Loading()
            _companyLeavesState.value = leaveRepository.getCompanyLeaves()
        }
    }

    fun updateLeaveRequest(leaveId: String, leaveRequest: LeaveRequestDto) {
        viewModelScope.launch {
            _updateLeaveState.value = Resource.Loading()
            _updateLeaveState.value = leaveRepository.updateLeaveRequest(leaveId, leaveRequest)
        }
    }

    fun updateLeaveStatus(leaveId: String, status: String) {
        viewModelScope.launch {
            _leaveActionState.value = Resource.Loading()
            _leaveActionState.value = leaveRepository.updateLeaveStatus(leaveId, status)
        }
    }

    fun removeHRResponse(leaveId: String) {
        viewModelScope.launch {
            _leaveActionState.value = Resource.Loading()
            _leaveActionState.value = leaveRepository.removeHRResponse(leaveId)
        }
    }

    fun clearSubmitLeaveState() {
        _submitLeaveState.value = null
    }

    fun clearUpdateLeaveState() {
        _updateLeaveState.value = null
    }

    fun clearLeaveActionState() {
        _leaveActionState.value = null
    }

    private fun calculateLeaveDays(startDate: String, endDate: String): Int {
        return try {
            val start = java.time.LocalDate.parse(startDate)
            val end = java.time.LocalDate.parse(endDate)
            (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).toInt()
        } catch (e: Exception) {
            1
        }
    }
}