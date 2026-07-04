package com.azad.androiddemoapp.di

import com.azad.androiddemoapp.data.repository.ShoppingRepository
import com.azad.androiddemoapp.data.repository.ShoppingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShoppingRepository(
        shoppingRepositoryImpl: ShoppingRepositoryImpl
    ): ShoppingRepository
}
