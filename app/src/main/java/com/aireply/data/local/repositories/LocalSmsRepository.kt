package com.aireply.data.local.repositories

import android.util.Log
import com.aireply.data.local.contentResolver.SmsContentResolver
import com.aireply.data.local.room.dao.ChatDetailsDao
import com.aireply.data.local.room.dao.ChatSummaryDao
import com.aireply.data.local.room.entity.ChatSummaryEntity
import com.aireply.data.local.room.entity.ChatWithMessages
import com.aireply.data.local.room.entity.toDomain
import com.aireply.domain.models.ChatDetailsModel
import com.aireply.domain.models.ChatSummaryModel
import com.aireply.domain.models.MessageModel
import com.aireply.domain.models.TextMessageModel
import com.aireply.domain.models.toMessageEntity
import com.aireply.util.PhoneNumberParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalSmsRepository @Inject constructor(
    private val smsContentResolver: SmsContentResolver,
    private val chatSummaryDao: ChatSummaryDao,
    private val chatDetailsDao: ChatDetailsDao
) {

    private fun mapChatSummaryEntityToDomain(chatSummaryEntity: List<ChatSummaryEntity>): List<ChatSummaryModel> {
        return chatSummaryEntity.map { it.toDomain() }
    }

    fun getChatSummaries(): Flow<List<ChatSummaryModel>> {
        return chatSummaryDao.getChatSummaries().map(::mapChatSummaryEntityToDomain)
    }

    suspend fun loadChatSummariesToRoom() = withContext(Dispatchers.IO) {
        val smsChats: List<ChatSummaryEntity> =
            smsContentResolver.getChatsSummaryContentResolver().map { it.toMessageEntity() }

        chatSummaryDao.insertChatSummaries(smsChats)
    }

    suspend fun addTextMessage(textMessage: TextMessageModel) = withContext(Dispatchers.IO) {
        val address = PhoneNumberParser.getPhoneNumberInfo(textMessage.sender)
        val chatId = chatDetailsDao.getChatIdByAddress(textMessage.sender)?.let {
            chatDetailsDao.insertTextMessage(textMessage.toMessageEntity(it))

            chatSummaryDao.updateChatSummaryByAddress(
                address = textMessage.sender,
                content = textMessage.content,
                timeStamp = textMessage.timeStamp,
                status = textMessage.status,
                type = textMessage.type,
                updatedAt = textMessage.timeStamp
            )
        }?: Log.e("prueba","No se agrego por alguna razon: +${address.countryCode} ${textMessage.sender}")
    }

    suspend fun updateChatSummary(summaryEntity: ChatSummaryEntity) = withContext(Dispatchers.IO) {
        chatSummaryDao.updateChatSummary(summaryEntity)
    }

    private fun mapChatWithMessagesToDomain(chatWithMessages: ChatWithMessages): ChatDetailsModel {
        return chatWithMessages.toDomain()
    }

    fun getChatDetails(longAddress: String): Flow<ChatDetailsModel> {
        val address = PhoneNumberParser.phoneNumberParser(longAddress)
        return chatDetailsDao.getChatWithMessages(address)
            .map(::mapChatWithMessagesToDomain)
    }

    suspend fun isChatAddressSaved(longAddress: String): Boolean = withContext(Dispatchers.IO) {
        val address = PhoneNumberParser.phoneNumberParser(longAddress)

        chatDetailsDao.isChatAddressSaved(address)
    }

    suspend fun loadChatDetailsToRoom(address: String) = withContext(Dispatchers.IO) {
        val chatsDetails = smsContentResolver.getChatDetailsByNumber(address)

        val chatId = chatDetailsDao.insertChat(chatsDetails.toMessageEntity())

        val insertedChat =
            chatDetailsDao.insertMessages(chatsDetails.chatList.map { it.toMessageEntity(chatId) })

        Log.d("prueba", "Resultado de importando detalles de un chat a Room: $insertedChat")
    }

    suspend fun getAllContactsName(): List<ChatDetailsModel> = withContext(Dispatchers.IO) {
        chatDetailsDao.getAllContacts().map { it.toDomain() }
    }

    suspend fun searchContact(searchText: String): List<ChatDetailsModel> =
        withContext(Dispatchers.IO) {
            chatDetailsDao.searchContacts(searchText).map { it.toDomain() }
        }

    suspend fun loadContactsToRoom() = withContext(Dispatchers.IO) {
        val contacts = smsContentResolver.getAllContacts()
        chatDetailsDao.insertContacts(contacts)
    }

    suspend fun removeMessages(
        messages: List<MessageModel>,
        addressOnChatSummaryChange: String?,
        newMessage: MessageModel?
    ) = withContext(Dispatchers.IO) {
        if (addressOnChatSummaryChange != null && newMessage != null) {
            newMessage.let {
                chatSummaryDao.updateChatSummaryByAddress(
                    address = addressOnChatSummaryChange,
                    content = it.content,
                    timeStamp = it.timeStamp,
                    status = it.status,
                    type = it.type,
                    updatedAt = it.timeStamp
                )
            }
        }
        chatDetailsDao.deleteMessages(messages.map { it.toMessageEntity() })
    }

    suspend fun deleteConversation(longAddress: String): Boolean = withContext(Dispatchers.IO) {
        val address = PhoneNumberParser.phoneNumberParser(longAddress)

        chatDetailsDao.deleteConversation(address) > 0
    }
}