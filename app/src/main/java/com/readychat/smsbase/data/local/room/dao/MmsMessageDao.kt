package com.readychat.smsbase.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.readychat.smsbase.data.local.room.entities.ChatWithMmsMessages
import com.readychat.smsbase.data.local.room.entities.MmsMessageEntity
import com.readychat.smsbase.data.local.room.entities.MmsPartEntity
import com.readychat.smsbase.data.local.room.entities.MmsWithParts
import kotlinx.coroutines.flow.Flow

@Dao
interface MmsMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMmsMessage(mmsMessage: MmsMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMmsMessages(mmsMessages: List<MmsMessageEntity>)

    @Query("SELECT * FROM mms_messages WHERE chatId = :chatId ORDER BY timeStamp ASC")
    fun getMmsMessagesByChatId(chatId: Long): Flow<List<MmsMessageEntity>>

    @Query("SELECT * FROM mms_messages WHERE mmsId = :mmsId LIMIT 1")
    suspend fun getMmsMessageByMmsId(mmsId: Long): MmsMessageEntity?

    @Transaction
    @Query("SELECT * FROM mms_messages WHERE id = :mmsMessageId")
    fun getMmsWithParts(mmsMessageId: Int): Flow<MmsWithParts>

    @Transaction
    @Query("SELECT * FROM chat_details WHERE id = :chatId")
    fun getChatWithMmsMessages(chatId: Long): Flow<ChatWithMmsMessages>

    @Query("UPDATE mms_messages SET read = :read WHERE id = :mmsMessageId")
    suspend fun updateReadStatus(mmsMessageId: Int, read: Boolean)

    @Delete
    suspend fun deleteMmsMessage(mmsMessage: MmsMessageEntity)

    @Query("DELETE FROM mms_messages WHERE chatId = :chatId")
    suspend fun deleteMmsMessagesByChatId(chatId: Long)
}

@Dao
interface MmsPartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMmsPart(mmsPart: MmsPartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMmsParts(mmsParts: List<MmsPartEntity>)

    @Query("SELECT * FROM mms_parts WHERE mmsMessageId = :mmsMessageId")
    fun getMmsPartsByMmsMessageId(mmsMessageId: Int): Flow<List<MmsPartEntity>>

    @Query("SELECT * FROM mms_parts WHERE contentType LIKE '%text%' AND mmsMessageId = :mmsMessageId LIMIT 1")
    suspend fun getTextPartByMmsMessageId(mmsMessageId: Int): MmsPartEntity?

    @Delete
    suspend fun deleteMmsPart(mmsPart: MmsPartEntity)

    @Query("DELETE FROM mms_parts WHERE mmsMessageId = :mmsMessageId")
    suspend fun deleteMmsPartsByMmsMessageId(mmsMessageId: Int)
}