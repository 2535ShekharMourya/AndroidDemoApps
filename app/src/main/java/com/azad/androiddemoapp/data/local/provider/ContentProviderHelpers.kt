package com.azad.androiddemoapp.data.local.provider

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import com.azad.androiddemoapp.model.CalendarEvent
import com.azad.androiddemoapp.model.Contact
import com.azad.androiddemoapp.model.MediaItem
import com.azad.androiddemoapp.model.SmsMessage
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentProviderHelpers @Inject constructor(
    private val context: Context
) {

    fun getContacts(queryLetter: Char? = null): List<Contact> {
        val contactsList = mutableListOf<Contact>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )
        
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        
        if (queryLetter != null) {
            selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            selectionArgs = arrayOf("$queryLetter%")
        }
        
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        
        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                val starredIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.STARRED)
                
                val uniqueIds = mutableSetOf<String>()
                
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex)
                    if (!uniqueIds.add(id)) continue
                    
                    val name = cursor.getString(nameIndex) ?: "Unknown"
                    val phone = cursor.getString(phoneIndex)
                    val photoUri = cursor.getString(photoIndex)
                    val isStarred = cursor.getInt(starredIndex) == 1
                    
                    contactsList.add(Contact(id, name, phone, photoUri, isStarred))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contactsList
    }

    fun getMediaItems(isVideoOnly: Boolean? = null): List<MediaItem> {
        val mediaList = mutableListOf<MediaItem>()
        val imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val videosUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        
        val loadImages = isVideoOnly == null || isVideoOnly == false
        val loadVideos = isVideoOnly == null || isVideoOnly == true
        
        if (loadImages) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE
            )
            try {
                context.contentResolver.query(
                    imagesUri, projection, null, null,
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )?.use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                    val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                    
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIndex)
                        val name = cursor.getString(nameIndex) ?: "Image_$id"
                        val size = cursor.getLong(sizeIndex)
                        val mimeType = cursor.getString(mimeIndex) ?: "image/*"
                        val contentUri = ContentUris.withAppendedId(imagesUri, id).toString()
                        mediaList.add(MediaItem(id, contentUri, name, size, mimeType, isVideo = false))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        if (loadVideos) {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION
            )
            try {
                context.contentResolver.query(
                    videosUri, projection, null, null,
                    "${MediaStore.Video.Media.DATE_ADDED} DESC"
                )?.use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                    val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idIndex)
                        val name = cursor.getString(nameIndex) ?: "Video_$id"
                        val size = cursor.getLong(sizeIndex)
                        val mimeType = cursor.getString(mimeIndex) ?: "video/*"
                        val duration = cursor.getLong(durationIndex)
                        val contentUri = ContentUris.withAppendedId(videosUri, id).toString()
                        mediaList.add(MediaItem(id, contentUri, name, size, mimeType, isVideo = true, duration = duration))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return mediaList.sortedByDescending { it.id }
    }

    fun getCalendarEvents(): List<CalendarEvent> {
        val eventsList = mutableListOf<CalendarEvent>()
        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
        )
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
        
        try {
            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val descIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)
                val startIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                val endIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
                val locIndex = cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex) ?: "No Title"
                    val description = cursor.getString(descIndex)
                    val startTime = cursor.getLong(startIndex)
                    val endTime = cursor.getLong(endIndex)
                    val location = cursor.getString(locIndex)
                    eventsList.add(CalendarEvent(id, title, description, startTime, endTime, location))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return eventsList
    }

    fun addCalendarEvent(title: String, description: String, startTime: Long, endTime: Long, location: String): Boolean {
        val calendarId = getWriteableCalendarId() ?: return false
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        return try {
            val resultUri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            resultUri != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteCalendarEvent(eventId: Long): Boolean {
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        return try {
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getWriteableCalendarId(): Long? {
        val uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val accessIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val accessLevel = cursor.getInt(accessIndex)
                    
                    if (accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                        return id
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 1L
    }

    fun getRecentSms(): List<SmsMessage> {
        val smsList = mutableListOf<SmsMessage>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            "_id",
            "address",
            "body",
            "date",
            "read"
        )
        val sortOrder = "date DESC LIMIT 50"
        try {
            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow("_id")
                val addressIndex = cursor.getColumnIndexOrThrow("address")
                val bodyIndex = cursor.getColumnIndexOrThrow("body")
                val dateIndex = cursor.getColumnIndexOrThrow("date")
                val readIndex = cursor.getColumnIndexOrThrow("read")
                
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex)
                    val address = cursor.getString(addressIndex) ?: "Unknown"
                    val body = cursor.getString(bodyIndex) ?: ""
                    val date = cursor.getLong(dateIndex)
                    val isRead = cursor.getInt(readIndex) == 1
                    smsList.add(SmsMessage(id, address, body, date, isRead))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return smsList
    }
}
