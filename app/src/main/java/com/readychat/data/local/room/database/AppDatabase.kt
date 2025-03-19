package com.readychat.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.readychat.data.local.room.dao.ChatDetailsDao
import com.readychat.data.local.room.dao.ChatSummaryDao
import com.readychat.data.local.room.entity.ChatDetailsEntity
import com.readychat.data.local.room.entity.ChatSummaryEntity
import com.readychat.data.local.room.entity.MessageEntity

@Database(entities = [ChatSummaryEntity::class, ChatDetailsEntity::class, MessageEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatSummaryDao(): ChatSummaryDao
    abstract fun chatDetailsDao(): ChatDetailsDao
}