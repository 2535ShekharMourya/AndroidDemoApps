package com.azad.androiddemoapp.data.repository

import androidx.paging.PagingData
import com.azad.androiddemoapp.data.local.entity.FavoriteEntity
import com.azad.androiddemoapp.data.local.entity.ProductEntity
import com.azad.androiddemoapp.data.local.entity.SearchHistoryEntity
import com.azad.androiddemoapp.data.remote.model.ProductDto
import com.azad.androiddemoapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun getProducts(category: String?): Flow<PagingData<ProductEntity>>
    fun searchProducts(query: String): Flow<PagingData<ProductDto>>
    
    suspend fun getCategories(): Resource<List<String>>

    // Favorites
    fun getFavorites(): Flow<List<FavoriteEntity>>
    suspend fun addFavorite(product: ProductEntity)
    suspend fun removeFavorite(productId: Int)
    fun isFavorite(productId: Int): Flow<Boolean>

    // Search History
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>
    suspend fun addSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
    suspend fun clearSearchHistory()

    // Cache clearing
    suspend fun clearCachedData()
}
