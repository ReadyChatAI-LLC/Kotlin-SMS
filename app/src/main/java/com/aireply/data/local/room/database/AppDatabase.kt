package com.aireply.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aireply.data.local.room.dao.ChatDetailsDao
import com.aireply.data.local.room.dao.ChatSummaryDao
import com.aireply.data.local.room.entity.ChatDetailsEntity
import com.aireply.data.local.room.entity.ChatSummaryEntity
import com.aireply.data.local.room.entity.MessageEntity

@Database(entities = [ChatSummaryEntity::class, ChatDetailsEntity::class, MessageEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatSummaryDao(): ChatSummaryDao
    abstract fun chatDetailsDao(): ChatDetailsDao
}