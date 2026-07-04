package com.azad.androiddemoapp.di

import android.content.Context
import androidx.room.Room
import com.azad.androiddemoapp.data.local.ShoppingDatabase
import com.azad.androiddemoapp.data.local.dao.FavoriteDao
import com.azad.androiddemoapp.data.local.dao.ProductDao
import com.azad.androiddemoapp.data.local.dao.RemoteKeyDao
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
    ): ShoppingDatabase {
        return Room.databaseBuilder(
            context,
            ShoppingDatabase::class.java,
            "shopping_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProductDao(db: ShoppingDatabase): ProductDao = db.productDao()

    @Provides
    fun provideFavoriteDao(db: ShoppingDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideSearchHistoryDao(db: ShoppingDatabase): SearchHistoryDao = db.searchHistoryDao()

    @Provides
    fun provideRemoteKeyDao(db: ShoppingDatabase): RemoteKeyDao = db.remoteKeyDao()
}
