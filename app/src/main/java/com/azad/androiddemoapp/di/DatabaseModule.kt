package com.azad.androiddemoapp.di

import android.content.Context
import com.azad.androiddemoapp.data.local.provider.ContentProviderHelpers
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
    fun provideContentProviderHelpers(@ApplicationContext context: Context): ContentProviderHelpers {
        return ContentProviderHelpers(context)
    }
}
