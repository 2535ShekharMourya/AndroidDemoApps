package com.azad.androiddemoapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.azad.androiddemoapp.data.local.ShoppingDatabase
import com.azad.androiddemoapp.data.local.entity.CartEntity
import com.azad.androiddemoapp.data.local.entity.FavoriteEntity
import com.azad.androiddemoapp.data.local.entity.ProductEntity
import com.azad.androiddemoapp.data.local.entity.SearchHistoryEntity
import com.azad.androiddemoapp.data.remote.DummyJsonService
import com.azad.androiddemoapp.data.remote.ProductRemoteMediator
import com.azad.androiddemoapp.data.remote.model.ProductDto
import com.azad.androiddemoapp.util.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingRepositoryImpl @Inject constructor(
    private val apiService: DummyJsonService,
    private val database: ShoppingDatabase
) : ShoppingRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getProducts(category: String?): Flow<PagingData<ProductEntity>> {
        val pagingSourceFactory = {
            if (category.isNullOrEmpty() || category == "All") {
                database.productDao().pagingSource()
            } else {
                database.productDao().pagingSourceByCategory(category)
            }
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            remoteMediator = ProductRemoteMediator(
                apiService = apiService,
                database = database,
                category = category
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    override fun searchProducts(query: String): Flow<PagingData<ProductDto>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchPagingSource(apiService, query) }
        ).flow
    }

    override suspend fun getCategories(): Resource<List<String>> {
        return try {
            val remoteCategories = apiService.getCategories()
            Resource.Success(listOf("All") + remoteCategories)
        } catch (e: SocketTimeoutException) {
            Resource.Error(e, "Request timed out. Please try again.")
        } catch (e: IOException) {
            Resource.Error(e, "No internet connection.")
        } catch (e: HttpException) {
            Resource.Error(e, "Server error: ${e.code()}")
        } catch (e: Exception) {
            Resource.Error(e, "An unknown error occurred.")
        }
    }

    override fun getFavorites(): Flow<List<FavoriteEntity>> {
        return database.favoriteDao().getAllFavoritesFlow()
    }

    override suspend fun addFavorite(product: ProductEntity) {
        val favorite = FavoriteEntity(
            id = product.id,
            title = product.title,
            description = product.description,
            category = product.category,
            price = product.price,
            discountPercentage = product.discountPercentage,
            rating = product.rating,
            stock = product.stock,
            brand = product.brand,
            thumbnail = product.thumbnail
        )
        database.favoriteDao().insertFavorite(favorite)
    }

    override suspend fun removeFavorite(productId: Int) {
        database.favoriteDao().deleteFavoriteById(productId)
    }

    override fun isFavorite(productId: Int): Flow<Boolean> {
        return database.favoriteDao().isFavoriteFlow(productId)
    }

    override fun getRecentSearches(): Flow<List<SearchHistoryEntity>> {
        return database.searchHistoryDao().getRecentSearchesFlow()
    }

    override suspend fun addSearchQuery(query: String) {
        if (query.isNotBlank()) {
            database.searchHistoryDao().insertSearch(SearchHistoryEntity(query.trim()))
        }
    }

    override suspend fun deleteSearchQuery(query: String) {
        database.searchHistoryDao().deleteSearch(query)
    }

    override suspend fun clearSearchHistory() {
        database.searchHistoryDao().clearHistory()
    }

    override suspend fun clearCachedData() {
        database.runInTransaction {
            // Using runInTransaction for generic Room operations
            // clearAll and clearRemoteKeys
            // Since we can run both database actions
            database.productDao().pagingSource().invalidate()
            database.runInTransaction {
                try {
                    // Let's call them directly
                } catch(e: Exception) {}
            }
        }
        database.productDao().clearAll()
        database.remoteKeyDao().clearRemoteKeys()
        database.searchHistoryDao().clearHistory()
    }

    override fun getCartItems(): Flow<List<CartEntity>> {
        return database.cartDao().getCartItemsFlow()
    }

    override suspend fun addToCart(product: ProductEntity) {
        val existing = database.cartDao().getCartItemById(product.id)
        if (existing != null) {
            database.cartDao().insertOrUpdateCartItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            database.cartDao().insertOrUpdateCartItem(
                CartEntity(
                    id = product.id,
                    title = product.title,
                    price = product.price,
                    discountPercentage = product.discountPercentage,
                    thumbnail = product.thumbnail,
                    quantity = 1
                )
            )
        }
    }

    override suspend fun addToCart(product: ProductDto) {
        val existing = database.cartDao().getCartItemById(product.id)
        if (existing != null) {
            database.cartDao().insertOrUpdateCartItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            database.cartDao().insertOrUpdateCartItem(
                CartEntity(
                    id = product.id,
                    title = product.title,
                    price = product.price,
                    discountPercentage = product.discountPercentage,
                    thumbnail = product.thumbnail,
                    quantity = 1
                )
            )
        }
    }

    override suspend fun updateCartQuantity(productId: Int, quantity: Int) {
        val existing = database.cartDao().getCartItemById(productId)
        if (existing != null) {
            if (quantity > 0) {
                database.cartDao().insertOrUpdateCartItem(existing.copy(quantity = quantity))
            } else {
                database.cartDao().deleteCartItemById(productId)
            }
        }
    }

    override suspend fun removeFromCart(productId: Int) {
        database.cartDao().deleteCartItemById(productId)
    }

    override suspend fun clearCart() {
        database.cartDao().clearCart()
    }

    override suspend fun getProductById(productId: Int): Resource<ProductEntity> {
        // Try local Room cache first
        val localProduct = database.productDao().getProductById(productId)
        if (localProduct != null) {
            return Resource.Success(localProduct)
        }

        // Fetch from Retrofit if not cached
        return try {
            val remoteDto = apiService.getProductById(productId)
            val entity = ProductEntity(
                id = remoteDto.id,
                title = remoteDto.title,
                description = remoteDto.description,
                category = remoteDto.category,
                price = remoteDto.price,
                discountPercentage = remoteDto.discountPercentage,
                rating = remoteDto.rating,
                stock = remoteDto.stock,
                brand = remoteDto.brand,
                thumbnail = remoteDto.thumbnail
            )
            // Cache it in DB
            database.productDao().insertAll(listOf(entity))
            Resource.Success(entity)
        } catch (e: SocketTimeoutException) {
            Resource.Error(e, "Request timed out. Please try again.")
        } catch (e: IOException) {
            Resource.Error(e, "No internet connection.")
        } catch (e: HttpException) {
            Resource.Error(e, "Server error: ${e.code()}")
        } catch (e: Exception) {
            Resource.Error(e, "An unknown error occurred.")
        }
    }
}

class SearchPagingSource(
    private val apiService: DummyJsonService,
    private val query: String
) : PagingSource<Int, ProductDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductDto> {
        val position = params.key ?: 0
        val limit = params.loadSize
        return try {
            val response = apiService.searchProducts(query, limit, position)
            val products = response.products
            LoadResult.Page(
                data = products,
                prevKey = if (position == 0) null else position - limit,
                nextKey = if (position + products.size >= response.total || products.isEmpty()) null else position + products.size
            )
        } catch (e: SocketTimeoutException) {
            LoadResult.Error(e)
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ProductDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }
    }
}
