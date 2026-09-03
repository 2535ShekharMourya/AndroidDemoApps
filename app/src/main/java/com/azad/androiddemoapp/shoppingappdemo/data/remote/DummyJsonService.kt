package com.azad.androiddemoapp.shoppingappdemo.data.remote

import com.azad.androiddemoapp.shoppingappdemo.data.remote.model.ProductDto
import com.azad.androiddemoapp.shoppingappdemo.data.remote.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DummyJsonService {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): ProductResponse

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): ProductResponse

    @GET("products/category-list")
    suspend fun getCategories(): List<String>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): ProductResponse

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int
    ): ProductDto
}
