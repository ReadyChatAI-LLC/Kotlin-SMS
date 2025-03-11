package com.aireply.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aireply.data.local.room.entity.ChatSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSummaryDao {
    @Query("SELECT * FROM chat_summary ORDER BY updatedAt DESC")
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

    @Update
    suspend fun updateChatSummary(summary: ChatSummaryEntity)

    @Delete
    suspend fun deleteChatSummary(summary: ChatSummaryEntity)

    @Update
    suspend fun updateChatSummaries(summaries: List<ChatSummaryEntity>)

    @Query("SELECT * FROM chat_summary WHERE id = :id")
    suspend fun getChatSummaryById(id: Int): ChatSummaryEntity?
}