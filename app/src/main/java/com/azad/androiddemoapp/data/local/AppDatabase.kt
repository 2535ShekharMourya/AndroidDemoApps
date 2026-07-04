package com.azad.androiddemoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.azad.androiddemoapp.data.local.dao.ArticleDao
import com.azad.androiddemoapp.data.local.dao.FavoriteDao
import com.azad.androiddemoapp.data.local.dao.RemoteKeysDao
import com.azad.androiddemoapp.data.local.dao.SearchHistoryDao
import com.azad.androiddemoapp.data.local.entity.ArticleEntity
import com.azad.androiddemoapp.data.local.entity.FavoriteArticleEntity
import com.azad.androiddemoapp.data.local.entity.RemoteKeysEntity
import com.azad.androiddemoapp.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        ArticleEntity::class,
        FavoriteArticleEntity::class,
        SearchHistoryEntity::class,
        RemoteKeysEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        const val DATABASE_NAME = "news_database"
    }
}
