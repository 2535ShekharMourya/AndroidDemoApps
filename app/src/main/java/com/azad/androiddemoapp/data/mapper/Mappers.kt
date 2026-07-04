package com.azad.androiddemoapp.data.mapper

import com.azad.androiddemoapp.data.local.entity.ArticleEntity
import com.azad.androiddemoapp.data.local.entity.FavoriteArticleEntity
import com.azad.androiddemoapp.data.remote.dto.ArticleDto
import com.azad.androiddemoapp.domain.model.Article

fun ArticleDto.toArticleEntity(category: String): ArticleEntity {
    return ArticleEntity(
        url = url ?: "",
        title = title ?: "",
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        sourceName = source?.name,
        author = author,
        content = content,
        category = category
    )
}

fun ArticleEntity.toArticle(isFavorite: Boolean = false): Article {
    return Article(
        url = url,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        sourceName = sourceName,
        author = author,
        content = content,
        category = category,
        isFavorite = isFavorite
    )
}

fun FavoriteArticleEntity.toArticle(): Article {
    return Article(
        url = url,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        sourceName = sourceName,
        author = author,
        content = content,
        category = null,
        isFavorite = true
    )
}

fun Article.toFavoriteArticleEntity(): FavoriteArticleEntity {
    return FavoriteArticleEntity(
        url = url,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        sourceName = sourceName,
        author = author,
        content = content
    )
}
