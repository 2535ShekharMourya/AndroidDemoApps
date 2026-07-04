package com.azad.androiddemoapp.data.remote.dto

data class ArticleDto(
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?,
    val source: SourceDto?
)
