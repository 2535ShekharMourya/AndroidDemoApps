package com.azad.androiddemoapp.di

import android.content.Context
import androidx.room.Room
import com.azad.androiddemoapp.data.local.AppDatabase
import com.azad.androiddemoapp.data.local.dao.ArticleDao
import com.azad.androiddemoapp.data.local.dao.FavoriteDao
import com.azad.androiddemoapp.data.local.dao.RemoteKeysDao
import com.azad.androiddemoapp.data.local.dao.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao = database.articleDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    fun provideRemoteKeysDao(database: AppDatabase): RemoteKeysDao = database.remoteKeysDao()
}
