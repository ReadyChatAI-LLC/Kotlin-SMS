package com.readychat.smsbase.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.readychat.smsbase.data.local.room.entities.ChatSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSummaryDao {
    @Query("SELECT * FROM chat_summary WHERE isArchived = 0 ORDER BY isPinned DESC, timeStamp DESC")
    fun getChatSummaries(): Flow<List<ChatSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSummaries(summaries: List<ChatSummaryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSummary(summary: ChatSummaryEntity): Long

    @Query("""
        UPDATE chat_summary 
        SET content = :content, 
            timeStamp = :timeStamp, 
            status = :status, 
            type = :type
        WHERE address = :address
    """)
    suspend fun updateChatSummaryByAddress(
        address: String,
        content: String,
        timeStamp: Long,
        status: String,
        type: String
    )

    @Query("SELECT * FROM chat_summary WHERE isArchived = 1")
    fun getArchivedChatSummaries(): Flow<List<ChatSummaryEntity>>

    @Query("UPDATE chat_summary SET isArchived = :archived WHERE id IN (:ids)")
    suspend fun updateArchivedChats(archived: Boolean, ids: List<Int>)

    @Query("UPDATE chat_summary SET isPinned = :pinned WHERE id IN (:ids)")
    suspend fun updatePinnedChats(pinned: Boolean, ids: List<Int>)

    @Query("UPDATE chat_summary SET isBlocked = :blocked WHERE id IN (:ids)")
    suspend fun updateBlockedChats(blocked: Boolean, ids: List<Int>)

    @Update
    suspend fun updateChatSummary(summary: ChatSummaryEntity)

    @Query("DELETE FROM chat_summary WHERE address IN (:addresses)")
    suspend fun deleteChatSummariesByAddresses(addresses: List<String>)

    @Update
    suspend fun updateChatSummaries(summaries: List<ChatSummaryEntity>)

    @Query("SELECT * FROM chat_summary WHERE id = :id")
    suspend fun getChatSummaryById(id: Int): ChatSummaryEntity?

    @Query("SELECT address FROM chat_summary WHERE id IN (:ids)")
    suspend fun getAddressesByIds(ids: List<Int>): List<String>

    @Query("SELECT * FROM chat_summary WHERE address = :address")
    suspend fun getChatSummaryByAddress(address: String): ChatSummaryEntity?
}