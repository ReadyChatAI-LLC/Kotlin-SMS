package com.readychat.smsbase.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.readychat.smsbase.data.local.room.entities.GroupChatSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupChatSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupChatSummary(groupChat: GroupChatSummaryEntity)

    @Query("SELECT * FROM group_chat_summary ORDER BY updatedAt DESC")
    fun getAllGroupChats(): Flow<List<GroupChatSummaryEntity>>

    @Query("SELECT * FROM group_chat_summary WHERE groupName = :groupName LIMIT 1")
    suspend fun getGroupByName(groupName: String): GroupChatSummaryEntity?

    @Query("""
        UPDATE group_chat_summary 
        SET lastMessage = :lastMessage,
            timeStamp = :timeStamp,
            updatedAt = :updatedAt
        WHERE groupName = :groupName
    """)
    suspend fun updateGroupLastMessage(
        groupName: String,
        lastMessage: String,
        timeStamp: Long,
        updatedAt: Long
    )

    @Query("DELETE FROM group_chat_summary WHERE groupName = :groupName")
    suspend fun deleteGroup(groupName: String)
}
