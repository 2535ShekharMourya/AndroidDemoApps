package com.azad.androiddemoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azad.androiddemoapp.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCartItem(cartItem: CartEntity)

    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_items WHERE id = :id")
    suspend fun getCartItemById(id: Int): CartEntity?

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
