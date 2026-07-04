package com.azad.androiddemoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azad.androiddemoapp.data.local.entity.RemoteKeysEntity

@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeysEntity>)

    @Query("SELECT * FROM remote_keys WHERE articleUrl = :url")
    suspend fun getRemoteKeysByArticleUrl(url: String): RemoteKeysEntity?

    @Query("DELETE FROM remote_keys WHERE articleUrl IN (SELECT url FROM articles WHERE category = :category)")
    suspend fun clearRemoteKeysByCategory(category: String)
}
