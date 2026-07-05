package com.azad.androiddemoapp.model

import android.net.Uri

data class Contact(
    val id: String,
    val name: String,
    val phone: String?,
    val photoUri: String?,
    val isStarred: Boolean = false
)

data class MediaItem(
    val id: Long,
    val uriString: String,
    val displayName: String,
    val size: Long,
    val mimeType: String,
    val isVideo: Boolean,
    val duration: Long? = null
) {
    val uri: Uri get() = Uri.parse(uriString)
}

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val location: String?
)

data class SmsMessage(
    val id: String,
    val address: String,
    val body: String,
    val date: Long,
    val isRead: Boolean
)
