package com.readychat.smsbase.domain.repositories

import com.readychat.smsbase.data.local.room.entities.ChatSummaryEntity
import com.readychat.smsbase.domain.models.ChatSummaryModel
import kotlinx.coroutines.flow.Flow

interface IChatSummaryRepository {
    fun getChatSummaries(): Flow<List<ChatSummaryModel>>
    fun getArchivedChatSummaries(): Flow<List<ChatSummaryModel>>
    suspend fun updateArchivedChats(archivedChat: Boolean, id: List<Int>)
    suspend fun loadChatSummariesToRoom()
    suspend fun updateChatSummary(summaryEntity: ChatSummaryEntity)
}