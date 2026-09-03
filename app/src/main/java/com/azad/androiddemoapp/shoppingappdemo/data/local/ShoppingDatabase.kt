package com.azad.androiddemoapp.shoppingappdemo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.azad.androiddemoapp.shoppingappdemo.data.local.dao.CartDao
import com.azad.androiddemoapp.shoppingappdemo.data.local.dao.FavoriteDao
import com.azad.androiddemoapp.shoppingappdemo.data.local.dao.ProductDao
import com.azad.androiddemoapp.shoppingappdemo.data.local.dao.RemoteKeyDao
import com.azad.androiddemoapp.shoppingappdemo.data.local.dao.SearchHistoryDao
import com.azad.androiddemoapp.shoppingappdemo.data.local.entity.CartEntity
import com.azad.androiddemoapp.shoppingappdemo.data.local.entity.FavoriteEntity
import com.azad.androiddemoapp.shoppingappdemo.data.local.entity.ProductEntity
import com.azad.androiddemoapp.shoppingappdemo.data.local.entity.RemoteKeyEntity
import com.azad.androiddemoapp.shoppingappdemo.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        ProductEntity::class,
        FavoriteEntity::class,
        SearchHistoryEntity::class,
        RemoteKeyEntity::class,
        CartEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ShoppingDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun cartDao(): CartDao
}
