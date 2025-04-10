package com.readychat.smsbase.data.local.repositories

import com.readychat.smsbase.data.local.contentResolver.SmsContentResolver
import com.readychat.smsbase.data.local.room.dao.ChatSummaryDao
import com.readychat.smsbase.data.local.room.entities.ChatSummaryEntity
import com.readychat.smsbase.data.local.room.entities.toDomain
import com.readychat.smsbase.domain.models.ChatSummaryModel
import com.readychat.smsbase.domain.models.toMessageEntity
import com.readychat.smsbase.domain.repositories.IChatSummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatSummaryRepositoryImpl @Inject constructor(
    private val smsContentResolver: SmsContentResolver,
    private val chatSummaryDao: ChatSummaryDao
) : IChatSummaryRepository {

    private fun mapChatSummaryEntityToDomain(chatSummaryEntity: List<ChatSummaryEntity>): List<ChatSummaryModel> {
        return chatSummaryEntity.map { it.toDomain() }
    }

    override fun getChatSummaries(): Flow<List<ChatSummaryModel>> {
        return chatSummaryDao.getChatSummaries().map(::mapChatSummaryEntityToDomain)
    }

    override fun getArchivedChatSummaries(): Flow<List<ChatSummaryModel>> {
        return chatSummaryDao.getArchivedChatSummaries().map(::mapChatSummaryEntityToDomain)
    }

    override suspend fun updateArchivedChats(archivedChat: Boolean, ids: List<Int>) =
        withContext(Dispatchers.IO) {
            chatSummaryDao.updateArchivedChats(archivedChat, ids)
        }

    override suspend fun updatePinnedChats(pinnedChat: Boolean, ids: List<Int>) =
        withContext(Dispatchers.IO) {
            chatSummaryDao.updatePinnedChats(pinnedChat, ids)
        }

    override suspend fun loadChatSummariesToRoom() {
        withContext(Dispatchers.IO) {
            val smsChats: List<ChatSummaryEntity> =
                smsContentResolver.getChatsSummaryContentResolver().map { it.toMessageEntity() }
            chatSummaryDao.insertChatSummaries(smsChats)
        }
    }

    override suspend fun updateChatSummary(summaryEntity: ChatSummaryEntity) {
        chatSummaryDao.updateChatSummary(summaryEntity)
    }
}