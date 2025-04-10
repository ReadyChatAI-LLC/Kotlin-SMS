package com.readychat.smsbase.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.readychat.smsbase.data.local.room.entities.ChatDetailsEntity
import com.readychat.smsbase.data.local.room.entities.ChatWithMessages
import com.readychat.smsbase.data.local.room.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDetailsDao {
    @Insert
    suspend fun insertChat(chat: ChatDetailsEntity): Long

    @Insert
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert
    suspend fun insertTextMessage(message: MessageEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM chat_details WHERE address = :address)")
    suspend fun isChatAddressSaved(address: String): Boolean

    @Transaction
    @Query("SELECT * FROM chat_details WHERE address = :address")
    fun getChatWithMessages(address: String): Flow<ChatWithMessages>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ChatDetailsEntity>): List<Long>

    @Query("SELECT * FROM chat_details")
    suspend fun getAllContacts(): List<ChatDetailsEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: ChatDetailsEntity): Long

    @Query("SELECT * FROM chat_details WHERE address LIKE '%' || :query || '%' OR contact LIKE '%' || :query || '%'")
    suspend fun searchContacts(query: String): List<ChatDetailsEntity>

    @Query("UPDATE chat_details SET isArchived = :archived WHERE address IN (:addresses)")
    suspend fun updateArchivedChats(archived: Boolean, addresses: List<String>)

    @Query("UPDATE chat_details SET isBlocked = :blocked WHERE address IN (:addresses)")
    suspend fun updateBlockedChats(blocked: Boolean, addresses: List<String>): Int

    @Query("SELECT contact FROM chat_details WHERE address = :address")
    suspend fun getContactNameByAddress(address: String): String?

    @Query("SELECT id FROM chat_details WHERE address = :address")
    suspend fun getChatIdByAddress(address: String): Long?

    @Query("SELECT id FROM chat_details WHERE address IN (:addresses)")
    suspend fun getChatIdsByAddresses(addresses: List<String>): List<Long>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND messageId = :messageId LIMIT 1")
    suspend fun findMessage(chatId: Long, messageId: Int): MessageEntity?

    @Delete
    suspend fun deleteMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM chat_details WHERE address = :address")
    suspend fun deleteConversation(address: String): Int

    @Query("DELETE FROM messages WHERE chatId IN (:chatIds)")
    suspend fun deleteMessagesByChatIds(chatIds: List<Long>)
}
