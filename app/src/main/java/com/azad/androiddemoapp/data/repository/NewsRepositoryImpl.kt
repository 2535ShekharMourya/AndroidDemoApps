package com.azad.androiddemoapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.azad.androiddemoapp.data.local.AppDatabase
import com.azad.androiddemoapp.data.local.entity.SearchHistoryEntity
import com.azad.androiddemoapp.data.mapper.toArticle
import com.azad.androiddemoapp.data.mapper.toFavoriteArticleEntity
import com.azad.androiddemoapp.data.remote.NewsApiService
import com.azad.androiddemoapp.domain.model.Article
import com.azad.androiddemoapp.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val apiService: NewsApiService
) : NewsRepository {

    private val articleDao = database.articleDao()
    private val favoriteDao = database.favoriteDao()
    private val searchHistoryDao = database.searchHistoryDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedTopHeadlines(category: Category): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            remoteMediator = NewsRemoteMediator(database, apiService, category),
            pagingSourceFactory = { articleDao.getArticlesByCategory(category.key) }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                val isFav = favoriteDao.isFavorite(entity.url)
                entity.toArticle(isFav)
            }
        }
    }

    override fun searchArticles(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchPagingSource(apiService, query, favoriteDao) }
        ).flow
    }

    override fun getFavoriteArticles(): Flow<List<Article>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { it.toArticle() }
        }
    }

    override fun isFavoriteFlow(url: String): Flow<Boolean> {
        return favoriteDao.isFavoriteFlow(url)
    }

    override suspend fun toggleFavorite(article: Article) {
        val isFav = favoriteDao.isFavorite(article.url)
        if (isFav) {
            favoriteDao.deleteFavorite(article.url)
        } else {
            favoriteDao.insertFavorite(article.toFavoriteArticleEntity())
        }
    }

    override fun getSearchHistory(): Flow<List<String>> {
        return searchHistoryDao.getSearchHistory().map { entities ->
            entities.map { it.query }
        }
    }

    override suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query.trim()))
        }
    }

    override suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteSearch(query)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearHistory()
    }
}
