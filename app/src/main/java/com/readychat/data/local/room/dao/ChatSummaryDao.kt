package com.readychat.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.readychat.data.local.room.entities.ChatSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSummaryDao {
    @Query("SELECT * FROM chat_summary WHERE archivedChat = 0 ORDER BY updatedAt DESC")
    fun getChatSummaries(): Flow<List<ChatSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSummaries(summaries: List<ChatSummaryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSummary(summary: ChatSummaryEntity)

    @Query("""
        UPDATE chat_summary 
        SET content = :content, 
            timeStamp = :timeStamp, 
            status = :status, 
            type = :type,
            updatedAt = :updatedAt
        WHERE address = :address
    """)
    suspend fun updateChatSummaryByAddress(
        address: String,
        content: String,
        timeStamp: Long,
        status: String,
        type: String,
        updatedAt: Long
    )

    @Query("SELECT * FROM chat_summary WHERE archivedChat = 1")
    fun getArchivedChatSummaries(): Flow<List<ChatSummaryEntity>>

    @Query("UPDATE chat_summary SET archivedChat = :archived WHERE id IN (:ids)")
    suspend fun updateArchivedChats(ids: List<Int>, archived: Boolean)

    @Update
    suspend fun updateChatSummary(summary: ChatSummaryEntity)

    @Query("DELETE FROM chat_summary WHERE address IN (:addresses)")
    suspend fun deleteChatSummariesByAddresses(addresses: List<String>)

    @Update
    suspend fun updateChatSummaries(summaries: List<ChatSummaryEntity>)

    @Query("SELECT * FROM chat_summary WHERE id = :id")
    suspend fun getChatSummaryById(id: Int): ChatSummaryEntity?

    @Query("SELECT * FROM chat_summary WHERE address = :address")
    suspend fun getChatSummaryByAddress(address: String): ChatSummaryEntity?
}