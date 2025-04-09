package com.readychat.smsbase.data.local.repositories

import android.util.Log
import com.readychat.smsbase.data.local.contentResolver.SmsContentResolver
import com.readychat.smsbase.data.local.room.dao.ChatDetailsDao
import com.readychat.smsbase.data.local.room.dao.ChatSummaryDao
import com.readychat.smsbase.data.local.room.entities.ChatSummaryEntity
import com.readychat.smsbase.data.local.room.entities.ChatWithMessages
import com.readychat.smsbase.data.local.room.entities.toDomain
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.models.ChatSummaryModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.domain.models.toMessageEntity
import com.readychat.smsbase.util.Converters
import com.readychat.smsbase.util.PhoneNumberParser
import com.readychat.smsbase.util.RandomColor
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

    suspend fun ensureChatExists(address: String): Long {
        val existingId = chatDetailsDao.getChatIdByAddress(address)
        return existingId ?: chatDetailsDao.insertChat(
            com.readychat.smsbase.data.local.room.entities.ChatDetailsEntity(
                address = address,
                contact = address,
                accountLogoColor = 0xFFAAAAAA.toInt(),
                archivedChat = false,
                updatedAt = System.currentTimeMillis()
            )
        ).also {
            Log.d("manupruebas", "Chat creado en chat_details con address: $address y id: $it")
        }
    }

    suspend fun addTextMessage(
        textMessage: TextMessageModel,
        customContactName: String? = null
    ) = withContext(Dispatchers.IO) {

        val chatId = ensureChatExists(textMessage.sender)

        chatDetailsDao.insertTextMessage(textMessage.toMessageEntity(chatId))

        val chatSummary = chatSummaryDao.getChatSummaryByAddress(textMessage.sender)?.let { obj ->
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
            contact = customContactName ?: getContactByAddress(textMessage.sender),
            updatedAt = textMessage.timeStamp,
            archivedChat = false,
            accountLogoColor = Converters.fromColor(RandomColor.randomColor())
        )

        chatSummaryDao.insertChatSummary(chatSummary)
    }


    private fun mapChatWithMessagesToDomain(chatWithMessages: ChatWithMessages): ChatDetailsModel {
        return chatWithMessages.toDomain()
    }

    fun getChatDetails(longAddress: String): Flow<ChatDetailsModel> {
        val address = longAddress
        return chatDetailsDao.getChatWithMessages(address)
            .map(::mapChatWithMessagesToDomain)
    }

    suspend fun isChatAddressSaved(longAddress: String): Boolean = withContext(Dispatchers.IO) {
        chatDetailsDao.isChatAddressSaved(PhoneNumberParser.phoneNumberParser(longAddress))
    }

    suspend fun loadChatDetailsToRoom(address: String) = withContext(Dispatchers.IO) {
        val chatsDetails = smsContentResolver.getChatDetailsByNumber(address)

        val chatId = chatDetailsDao.insertChat(chatsDetails.toMessageEntity())

        val insertedChat =
            chatDetailsDao.insertMessages(chatsDetails.chatList.map { it.toMessageEntity(chatId) })

    }

    suspend fun getAllContactsName(): List<ChatDetailsModel> = withContext(Dispatchers.IO) {
        chatDetailsDao.getAllContacts().map { it.toDomain() }
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
