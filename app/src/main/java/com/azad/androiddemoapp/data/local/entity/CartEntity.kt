package com.azad.androiddemoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val price: Double,
    val discountPercentage: Double,
    val thumbnail: String,
    val quantity: Int = 1
)
