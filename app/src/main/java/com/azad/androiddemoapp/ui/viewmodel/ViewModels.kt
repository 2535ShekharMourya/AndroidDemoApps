package com.azad.androiddemoapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azad.androiddemoapp.data.local.provider.ContentProviderHelpers
import com.azad.androiddemoapp.model.CalendarEvent
import com.azad.androiddemoapp.model.Contact
import com.azad.androiddemoapp.model.MediaItem
import com.azad.androiddemoapp.model.ResourceState
import com.azad.androiddemoapp.model.SmsMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val providerHelpers: ContentProviderHelpers
) : ViewModel() {

    private val _contactsState = MutableStateFlow<ResourceState<List<Contact>>>(ResourceState.Loading)
    val contactsState: StateFlow<ResourceState<List<Contact>>> = _contactsState.asStateFlow()

    private val _selectedLetter = MutableStateFlow<Char?>(null)
    val selectedLetter: StateFlow<Char?> = _selectedLetter.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts(letter: Char? = null) {
        _selectedLetter.value = letter
        viewModelScope.launch {
            _contactsState.value = ResourceState.Loading
            try {
                val contacts = providerHelpers.getContacts(letter)
                _contactsState.value = ResourceState.Success(contacts)
            } catch (e: Exception) {
                _contactsState.value = ResourceState.Error.UnknownError(e)
            }
        }
    }
}

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val providerHelpers: ContentProviderHelpers
) : ViewModel() {

    private val _mediaState = MutableStateFlow<ResourceState<List<MediaItem>>>(ResourceState.Loading)
    val mediaState: StateFlow<ResourceState<List<MediaItem>>> = _mediaState.asStateFlow()

    private val _isVideoOnly = MutableStateFlow<Boolean?>(null)
    val isVideoOnly: StateFlow<Boolean?> = _isVideoOnly.asStateFlow()

    init {
        loadMedia()
    }

    fun loadMedia(videoOnly: Boolean? = null) {
        _isVideoOnly.value = videoOnly
        viewModelScope.launch {
            _mediaState.value = ResourceState.Loading
            try {
                val media = providerHelpers.getMediaItems(videoOnly)
                _mediaState.value = ResourceState.Success(media)
            } catch (e: Exception) {
                _mediaState.value = ResourceState.Error.UnknownError(e)
            }
        }
    }
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val providerHelpers: ContentProviderHelpers
) : ViewModel() {

    private val _eventsState = MutableStateFlow<ResourceState<List<CalendarEvent>>>(ResourceState.Loading)
    val eventsState: StateFlow<ResourceState<List<CalendarEvent>>> = _eventsState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = ResourceState.Loading
            try {
                val events = providerHelpers.getCalendarEvents()
                _eventsState.value = ResourceState.Success(events)
            } catch (e: Exception) {
                _eventsState.value = ResourceState.Error.UnknownError(e)
            }
        }
    }

    fun addEvent(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        location: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            val success = providerHelpers.addCalendarEvent(title, description, startTime, endTime, location)
            if (success) {
                loadEvents()
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun deleteEvent(eventId: Long, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = providerHelpers.deleteCalendarEvent(eventId)
            if (success) {
                loadEvents()
                onSuccess()
            } else {
                onError()
            }
        }
    }
}

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val providerHelpers: ContentProviderHelpers
) : ViewModel() {

    private val _smsState = MutableStateFlow<ResourceState<List<SmsMessage>>>(ResourceState.Loading)
    val smsState: StateFlow<ResourceState<List<SmsMessage>>> = _smsState.asStateFlow()

    init {
        loadSms()
    }

    fun loadSms() {
        viewModelScope.launch {
            _smsState.value = ResourceState.Loading
            try {
                val sms = providerHelpers.getRecentSms()
                _smsState.value = ResourceState.Success(sms)
            } catch (e: Exception) {
                _smsState.value = ResourceState.Error.UnknownError(e)
            }
        }
    }
}
