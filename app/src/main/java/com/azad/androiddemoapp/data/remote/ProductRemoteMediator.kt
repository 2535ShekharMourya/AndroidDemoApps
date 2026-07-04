package com.azad.androiddemoapp.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.azad.androiddemoapp.data.local.ShoppingDatabase
import com.azad.androiddemoapp.data.local.entity.ProductEntity
import com.azad.androiddemoapp.data.local.entity.RemoteKeyEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ProductRemoteMediator(
    private val apiService: DummyJsonService,
    private val database: ShoppingDatabase,
    private val category: String? = null
) : RemoteMediator<Int, ProductEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ProductEntity>
    ): MediatorResult {
        val limit = state.config.pageSize
        val skip = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(limit) ?: 0
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = if (category.isNullOrEmpty() || category == "All") {
                apiService.getProducts(limit = limit, skip = skip)
            } else {
                apiService.getProductsByCategory(category = category, limit = limit, skip = skip)
            }

            val products = response.products
            val endOfPaginationReached = skip + products.size >= response.total || products.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    if (category.isNullOrEmpty() || category == "All") {
                        database.productDao().clearAll()
                    }
                    database.remoteKeyDao().clearRemoteKeys()
                }

                val prevKey = if (skip == 0) null else skip - limit
                val nextKey = if (endOfPaginationReached) null else skip + products.size

                val keys = products.map {
                    RemoteKeyEntity(productId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeyDao().insertAll(keys)

                val entities = products.map { dto ->
                    ProductEntity(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        category = dto.category,
                        price = dto.price,
                        discountPercentage = dto.discountPercentage,
                        rating = dto.rating,
                        stock = dto.stock,
                        brand = dto.brand,
                        thumbnail = dto.thumbnail
                    )
                }
                database.productDao().insertAll(entities)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ProductEntity>): RemoteKeyEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { product ->
                database.remoteKeyDao().remoteKeysProductId(product.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ProductEntity>): RemoteKeyEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { product ->
                database.remoteKeyDao().remoteKeysProductId(product.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ProductEntity>): RemoteKeyEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { productId ->
                database.remoteKeyDao().remoteKeysProductId(productId)
            }
        }
    }
}
