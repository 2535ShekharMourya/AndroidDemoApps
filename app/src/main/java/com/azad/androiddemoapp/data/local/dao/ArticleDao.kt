package com.azad.androiddemoapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azad.androiddemoapp.data.local.entity.ArticleEntity

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Query("SELECT * FROM articles WHERE category = :category")
    fun getArticlesByCategory(category: String): PagingSource<Int, ArticleEntity>

    @Query("DELETE FROM articles WHERE category = :category")
    suspend fun clearArticlesByCategory(category: String)
}
