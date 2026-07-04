package com.azad.androiddemoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val sourceName: String?,
    val author: String?,
    val content: String?,
    val category: String?
)
