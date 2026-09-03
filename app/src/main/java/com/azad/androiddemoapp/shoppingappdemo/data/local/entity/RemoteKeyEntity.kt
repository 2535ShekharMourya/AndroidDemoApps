package com.azad.androiddemoapp.shoppingappdemo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val productId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
