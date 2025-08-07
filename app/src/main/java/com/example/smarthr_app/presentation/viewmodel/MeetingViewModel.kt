package com.example.smarthr_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.repository.MeetingRepository
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MeetingViewModel(private val meetingRepository: MeetingRepository) : ViewModel() {

    private val _meetingsState = MutableStateFlow<Resource<List<MeetingResponseDto>>?>(null)
    val meetingsState: StateFlow<Resource<List<MeetingResponseDto>>?> = _meetingsState

    private val _createMeetingState = MutableStateFlow<Resource<MeetingResponseDto>?>(null)
    val createMeetingState: StateFlow<Resource<MeetingResponseDto>?> = _createMeetingState

    private val _updateMeetingState = MutableStateFlow<Resource<MeetingResponseDto>?>(null)
    val updateMeetingState: StateFlow<Resource<MeetingResponseDto>?> = _updateMeetingState

    private val _cancelMeetingState = MutableStateFlow<Resource<SuccessApiResponseMessage>?>(null)
    val cancelMeetingState: StateFlow<Resource<SuccessApiResponseMessage>?> = _cancelMeetingState

    private val _meetingDetailState = MutableStateFlow<Resource<MeetingResponseDto>?>(null)
    val meetingDetailState: StateFlow<Resource<MeetingResponseDto>?> = _meetingDetailState

    private val _respondToMeetingState = MutableStateFlow<Resource<SuccessApiResponseMessage>?>(null)
    val respondToMeetingState: StateFlow<Resource<SuccessApiResponseMessage>?> = _respondToMeetingState

    fun createMeeting(
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        meetingLink: String?,
        participants: List<String>
    ) {
        viewModelScope.launch {
            _createMeetingState.value = Resource.Loading()
            _createMeetingState.value = meetingRepository.createMeeting(
                title, description, startTime, endTime, meetingLink, participants
            )
        }
    }

    fun loadMeetings() {
        viewModelScope.launch {
            _meetingsState.value = Resource.Loading()
            _meetingsState.value = meetingRepository.getMyMeetings()
        }
    }

    fun loadMeetingDetail(meetingId: String) {
        viewModelScope.launch {
            _meetingDetailState.value = Resource.Loading()
            _meetingDetailState.value = meetingRepository.getMeetingById(meetingId)
        }
    }

    fun updateMeeting(
        meetingId: String,
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        meetingLink: String?,
        participants: List<String>
    ) {
        viewModelScope.launch {
            _updateMeetingState.value = Resource.Loading()
            _updateMeetingState.value = meetingRepository.updateMeeting(
                meetingId, title, description, startTime, endTime, meetingLink, participants
            )
        }
    }

    fun cancelMeeting(meetingId: String) {
        viewModelScope.launch {
            _cancelMeetingState.value = Resource.Loading()
            _cancelMeetingState.value = meetingRepository.cancelMeeting(meetingId)
        }
    }

    fun respondToMeeting(meetingId: String, status: String) {
        viewModelScope.launch {
            _respondToMeetingState.value = Resource.Loading()
            _respondToMeetingState.value = meetingRepository.respondToMeeting(meetingId, status)
        }
    }

    // Clear states
    fun clearCreateMeetingState() {
        _createMeetingState.value = null
    }

    fun clearUpdateMeetingState() {
        _updateMeetingState.value = null
    }

    fun clearCancelMeetingState() {
        _cancelMeetingState.value = null
    }

    fun clearRespondToMeetingState() {
        _respondToMeetingState.value = null
    }
}