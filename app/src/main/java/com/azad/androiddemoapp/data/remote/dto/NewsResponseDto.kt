package com.azad.androiddemoapp.data.remote.dto

data class NewsResponseDto(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleDto>,
    val code: String?,
    val message: String?
)
