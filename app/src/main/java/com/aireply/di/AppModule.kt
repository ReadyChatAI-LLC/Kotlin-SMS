package com.aireply.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.aireply.data.local.contentResolver.SmsContentResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.initializationDataStore: DataStore<Preferences> by preferencesDataStore(name = "initialization_settings")

    @Provides
    @Singleton
    fun provideInitializationDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.initializationDataStore
    }
}