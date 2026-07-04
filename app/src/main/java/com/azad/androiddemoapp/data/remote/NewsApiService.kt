package com.azad.androiddemoapp.data.remote

import com.azad.androiddemoapp.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("country") country: String = "us"
    ): NewsResponseDto

    @GET("everything")
    suspend fun searchEverything(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("language") language: String = "en"
    ): NewsResponseDto

    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
    }
}
