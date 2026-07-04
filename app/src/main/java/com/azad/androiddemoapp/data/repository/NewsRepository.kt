package com.azad.androiddemoapp.data.repository

import androidx.paging.PagingData
import com.azad.androiddemoapp.domain.model.Article
import com.azad.androiddemoapp.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getPagedTopHeadlines(category: Category): Flow<PagingData<Article>>
    fun searchArticles(query: String): Flow<PagingData<Article>>
    fun getFavoriteArticles(): Flow<List<Article>>
    fun isFavoriteFlow(url: String): Flow<Boolean>
    suspend fun toggleFavorite(article: Article)
    fun getSearchHistory(): Flow<List<String>>
    suspend fun addSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
    suspend fun clearSearchHistory()
}