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

class LocalSmsRepositorys @Inject constructor(
    private val smsContentResolver: SmsContentResolver,
    private val chatSummaryDao: ChatSummaryDao,
    private val chatDetailsDao: ChatDetailsDao
) {

    // DONE
    suspend fun getContactByAddress(address: String): String = withContext(Dispatchers.IO) {
        smsContentResolver.getContactName(address) ?: address
    }

    private fun mapChatSummaryEntityToDomain(chatSummaryEntity: List<ChatSummaryEntity>): List<ChatSummaryModel> {
        return chatSummaryEntity.map { it.toDomain() }
    }

    // DONE
    fun getChatSummaries(): Flow<List<ChatSummaryModel>> {
        return chatSummaryDao.getChatSummaries().map(::mapChatSummaryEntityToDomain)
    }

    // DONE
    fun getArchivedChatSummaries(): Flow<List<ChatSummaryModel>> {
        return chatSummaryDao.getArchivedChatSummaries().map(::mapChatSummaryEntityToDomain)
    }

    // DONE
    suspend fun updateArchivedChats(archivedChat: Boolean, id: List<Int>) =
        withContext(Dispatchers.IO) {
            chatSummaryDao.updateArchivedChats(id, archivedChat)
        }

    // DONE
    suspend fun loadChatSummariesToRoom() = withContext(Dispatchers.IO) {
        val smsChats: List<ChatSummaryEntity> =
            smsContentResolver.getChatsSummaryContentResolver().map { it.toMessageEntity() }

        chatSummaryDao.insertChatSummaries(smsChats)
    }

    // DONE
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

    // DONE
    suspend fun updateChatSummary(summaryEntity: ChatSummaryEntity) = withContext(Dispatchers.IO) {
        chatSummaryDao.updateChatSummary(summaryEntity)
    }

    private fun mapChatWithMessagesToDomain(chatWithMessages: ChatWithMessages): ChatDetailsModel {
        return chatWithMessages.toDomain()
    }

    // DONE
    fun getChatDetails(longAddress: String): Flow<ChatDetailsModel> {
        val address = PhoneNumberParser.phoneNumberParser(longAddress)
        return chatDetailsDao.getChatWithMessages(address)
            .map(::mapChatWithMessagesToDomain)
    }

    // DONE
    suspend fun isChatAddressSaved(longAddress: String): Boolean = withContext(Dispatchers.IO) {
        Log.i("prueba", "Checking initial number: 1")
        chatDetailsDao.isChatAddressSaved(PhoneNumberParser.phoneNumberParser(longAddress))
    }

    // DONE
    suspend fun loadChatDetailsToRoom(address: String) = withContext(Dispatchers.IO) {
        Log.i("prueba", "Checking initial number: 2")
        Log.d("prueba", "0")
        val chatsDetails = smsContentResolver.getChatDetailsByNumber(address)
        Log.d("prueba", "3")

        val chatId = chatDetailsDao.insertChat(chatsDetails.toMessageEntity())

        Log.d("prueba", "4")

        val insertedChat =
            chatDetailsDao.insertMessages(chatsDetails.chatList.map { it.toMessageEntity(chatId) })

        Log.d("prueba", "Resultado de importando detalles de un chat a Room: $insertedChat")
    }

    // DONE
    suspend fun getAllContactsName(): List<ChatDetailsModel> = withContext(Dispatchers.IO) {
        chatDetailsDao.getAllContacts().map { it.toDomain() }
    }

    // DONE
    suspend fun searchContact(searchText: String): List<ChatDetailsModel> =
        withContext(Dispatchers.IO) {
            chatDetailsDao.searchContacts(searchText).map { it.toDomain() }
        }

    // DONE
    suspend fun loadContactsToRoom() = withContext(Dispatchers.IO) {
        val contacts = smsContentResolver.getAllContacts()
        chatDetailsDao.insertContacts(contacts)
    }

    // DONE
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

    // DONE
    suspend fun deleteChats(chatsToBeDeleted: List<String>) = withContext(Dispatchers.IO) {
        chatSummaryDao.deleteChatSummariesByAddresses(chatsToBeDeleted)

        val chatIds = chatDetailsDao.getChatIdsByAddresses(chatsToBeDeleted)
        chatDetailsDao.deleteMessagesByChatIds(chatIds)
    }
}