package com.readychat.data.local.repositories

import android.util.Log
import com.readychat.data.local.contentResolver.SmsContentResolver
import com.readychat.data.local.room.dao.ChatDetailsDao
import com.readychat.data.local.room.dao.ChatSummaryDao
import com.readychat.data.local.room.entity.ChatSummaryEntity
import com.readychat.data.local.room.entity.ChatWithMessages
import com.readychat.data.local.room.entity.toDomain
import com.readychat.domain.models.ChatDetailsModel
import com.readychat.domain.models.ChatSummaryModel
import com.readychat.domain.models.MessageModel
import com.readychat.domain.models.TextMessageModel
import com.readychat.domain.models.toMessageEntity
import com.readychat.util.Converters
import com.readychat.util.PhoneNumberParser
import com.readychat.util.RandomColor
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

    suspend fun getContactByAddress(address: String): String = withContext(Dispatchers.IO) {
        smsContentResolver.getContactName(address) ?: address
    }

    private fun mapChatSummaryEntityToDomain(chatSummaryEntity: List<ChatSummaryEntity>): List<ChatSummaryModel> {
        return chatSummaryEntity.map { it.toDomain() }
    }

    fun getChatSummaries(): Flow<List<ChatSummaryModel>> {
        return chatSummaryDao.getChatSummaries().map(::mapChatSummaryEntityToDomain)
    }

    fun getArchivedChatSummaries(): Flow<List<ChatSummaryModel>> {
        return chatSummaryDao.getArchivedChatSummaries().map(::mapChatSummaryEntityToDomain)
    }

    suspend fun updateArchivedChats(archivedChat: Boolean, id: List<Int>) =
        withContext(Dispatchers.IO) {
            chatSummaryDao.updateArchivedChats(id, archivedChat)
        }

    suspend fun loadChatSummariesToRoom() = withContext(Dispatchers.IO) {
        val smsChats: List<ChatSummaryEntity> =
            smsContentResolver.getChatsSummaryContentResolver().map { it.toMessageEntity() }

        chatSummaryDao.insertChatSummaries(smsChats)
    }

    suspend fun addTextMessage(textMessage: TextMessageModel) = withContext(Dispatchers.IO) {
        Log.d("prueba", "Agregando nuevo mensaje: $textMessage")

        chatDetailsDao.getChatIdByAddress(textMessage.sender)?.let {
            chatDetailsDao.insertTextMessage(textMessage.toMessageEntity(it))

            val chatSummary =
                chatSummaryDao.getChatSummaryByAddress(textMessage.sender)?.let { obj ->
                    obj.copy(
                        content = textMessage.content,
                        timeStamp = textMessage.timeStamp,
                        status = textMessage.status,
                        type = textMessage.type,
                        updatedAt = textMessage.timeStamp,
                        archivedChat = obj.archivedChat
                    )
                } ?: ChatSummaryEntity(
                    address = textMessage.sender,
                    content = textMessage.content,
                    timeStamp = textMessage.timeStamp,
                    status = textMessage.status,
                    type = textMessage.type,
                    contact = getContactByAddress(textMessage.sender),
                    updatedAt = textMessage.timeStamp,
                    archivedChat = false,
                    accountLogoColor = Converters.fromColor(RandomColor.randomColor())
                )

            chatSummaryDao.insertChatSummary(chatSummary)
        }
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

    suspend fun deleteChat(chatsToBeDeleted: List<String>) = withContext(Dispatchers.IO) {
        chatSummaryDao.deleteChatSummariesByAddresses(chatsToBeDeleted)

        val chatIds = chatDetailsDao.getChatIdsByAddresses(chatsToBeDeleted)
        chatDetailsDao.deleteMessagesByChatIds(chatIds)
    }
}