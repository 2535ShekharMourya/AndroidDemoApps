package com.azad.androiddemoapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.azad.androiddemoapp.data.local.AppDatabase
import com.azad.androiddemoapp.data.local.entity.ArticleEntity
import com.azad.androiddemoapp.data.local.entity.RemoteKeysEntity
import com.azad.androiddemoapp.data.mapper.toArticleEntity
import com.azad.androiddemoapp.data.remote.NewsApiService
import com.azad.androiddemoapp.domain.model.Category
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val database: AppDatabase,
    private val apiService: NewsApiService,
    private val category: Category
) : RemoteMediator<Int, ArticleEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = apiService.getTopHeadlines(
                category = category.key,
                page = page,
                pageSize = state.config.pageSize
            )

            val articles = response.articles.filter { !it.url.isNullOrBlank() }
            val endOfPaginationReached = articles.isEmpty() || (page * state.config.pageSize) >= response.totalResults

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.articleDao().clearArticlesByCategory(category.key)
                    database.remoteKeysDao().clearRemoteKeysByCategory(category.key)
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                
                val keys = articles.map {
                    RemoteKeysEntity(
                        articleUrl = it.url!!,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }

                val entities = articles.map { it.toArticleEntity(category.key) }

                database.remoteKeysDao().insertAll(keys)
                database.articleDao().insertAll(entities)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ArticleEntity>): RemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { article ->
                database.remoteKeysDao().getRemoteKeysByArticleUrl(article.url)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ArticleEntity>): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.url?.let { url ->
                database.remoteKeysDao().getRemoteKeysByArticleUrl(url)
            }
        }
    }
}
