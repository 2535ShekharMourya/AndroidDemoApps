package com.azad.androiddemoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeysEntity(
    @PrimaryKey val articleUrl: String,
    val prevKey: Int?,
    val nextKey: Int?
)
