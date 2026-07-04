package com.azad.androiddemoapp.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.azad.androiddemoapp.data.local.dao.FavoriteDao
import com.azad.androiddemoapp.data.remote.NewsApiService
import com.azad.androiddemoapp.domain.model.Article
import retrofit2.HttpException
import java.io.IOException

class SearchPagingSource(
    private val apiService: NewsApiService,
    private val query: String,
    private val favoriteDao: FavoriteDao
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val position = params.key ?: 1
        return try {
            val response = apiService.searchEverything(
                query = query,
                page = position,
                pageSize = params.loadSize
            )
            
            val articlesDto = response.articles.filter { !it.url.isNullOrBlank() }
            val articles = articlesDto.map { dto ->
                val isFav = favoriteDao.isFavorite(dto.url!!)
                Article(
                    url = dto.url,
                    title = dto.title ?: "",
                    description = dto.description,
                    urlToImage = dto.urlToImage,
                    publishedAt = dto.publishedAt,
                    sourceName = dto.source?.name,
                    author = dto.author,
                    content = dto.content,
                    category = null,
                    isFavorite = isFav
                )
            }

            val endOfPaginationReached = articles.isEmpty() || (position * params.loadSize) >= response.totalResults

            LoadResult.Page(
                data = articles,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (endOfPaginationReached) null else position + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}
