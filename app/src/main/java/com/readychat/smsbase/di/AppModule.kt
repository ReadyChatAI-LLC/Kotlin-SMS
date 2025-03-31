package com.readychat.smsbase.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.readychat.smsbase.data.local.contentResolver.SmsContentResolver
import com.readychat.smsbase.data.local.repositories.LocalSmsRepository
import com.readychat.smsbase.data.local.room.dao.ChatDetailsDao
import com.readychat.smsbase.data.local.room.dao.ChatSummaryDao
import com.readychat.smsbase.data.local.room.database.AppDatabase
import com.readychat.smsbase.presentation.viewmodel.util.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val SMS_DATABASE_NAME = "sms"

    private val Context.initializationDataStore: DataStore<Preferences> by preferencesDataStore(name = "initialization_settings")

    @Provides
    @Singleton
    fun provideInitializationDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.initializationDataStore
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, SMS_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideChatSummaryDao(appDatabase: AppDatabase): ChatSummaryDao {
        return appDatabase.chatSummaryDao()
    }

    @Provides
    @Singleton
    fun provideChatDetailsDao(appDatabase: AppDatabase): ChatDetailsDao {
        return appDatabase.chatDetailsDao()
    }

    @Provides
    @Singleton
    fun provideLocalSmsRepository(smsContentResolver: SmsContentResolver, chatSummaryDao: ChatSummaryDao, chatDetailsDao: ChatDetailsDao): LocalSmsRepository{
        return LocalSmsRepository(smsContentResolver, chatSummaryDao, chatDetailsDao)
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }
}