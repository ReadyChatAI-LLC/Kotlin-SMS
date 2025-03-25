package com.readychat.smsbase.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.readychat.smsbase.data.local.room.dao.ChatDetailsDao
import com.readychat.smsbase.data.local.room.dao.ChatSummaryDao
import com.readychat.smsbase.data.local.room.dao.MmsMessageDao
import com.readychat.smsbase.data.local.room.dao.MmsPartDao
import com.readychat.smsbase.data.local.room.entities.ChatDetailsEntity
import com.readychat.smsbase.data.local.room.entities.ChatSummaryEntity
import com.readychat.smsbase.data.local.room.entities.MessageEntity
import com.readychat.smsbase.data.local.room.entities.MmsMessageEntity
import com.readychat.smsbase.data.local.room.entities.MmsPartEntity

@Database(
    entities = [
        ChatSummaryEntity::class,
        ChatDetailsEntity::class,
        MessageEntity::class,
        MmsMessageEntity::class,
        MmsPartEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatSummaryDao(): ChatSummaryDao
    abstract fun chatDetailsDao(): ChatDetailsDao
    abstract fun mmsMessageDao(): MmsMessageDao
    abstract fun mmsPartDao(): MmsPartDao
}