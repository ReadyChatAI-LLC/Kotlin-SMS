package com.readychat.smsbase.domain.repositories

import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import kotlinx.coroutines.flow.Flow

interface IChatDetailsRepository {
    fun getChatDetails(longAddress: String): Flow<ChatDetailsModel>
    suspend fun isChatAddressSaved(longAddress: String): Boolean
    suspend fun loadChatDetailsToRoom(address: String)
    suspend fun addTextMessage(textMessage: TextMessageModel)
    suspend fun removeMessages(messages: List<MessageModel>, addressOnChatSummaryChange: String?, newMessage: MessageModel?)
    suspend fun deleteChats(chatsToBeDeleted: List<String>)
}