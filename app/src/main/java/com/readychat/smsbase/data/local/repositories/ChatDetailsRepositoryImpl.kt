package com.readychat.smsbase.data.local.repositories

import android.util.Log
import com.readychat.smsbase.data.local.contentResolver.SmsContentResolver
import com.readychat.smsbase.data.local.room.dao.ChatDetailsDao
import com.readychat.smsbase.data.local.room.dao.ChatSummaryDao
import com.readychat.smsbase.data.local.room.entities.ChatDetailsEntity
import com.readychat.smsbase.data.local.room.entities.ChatSummaryEntity
import com.readychat.smsbase.data.local.room.entities.ChatWithMessages
import com.readychat.smsbase.data.local.room.entities.toDomain
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.domain.models.toMessageEntity
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import com.readychat.smsbase.util.Converters
import com.readychat.smsbase.util.PhoneNumberParser
import com.readychat.smsbase.util.RandomColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatDetailsRepositoryImpl @Inject constructor(
    private val smsContentResolver: SmsContentResolver,
    private val chatDetailsDao: ChatDetailsDao,
    private val chatSummaryDao: ChatSummaryDao
) : IChatDetailsRepository {

    private fun mapChatWithMessagesToDomain(chatWithMessages: ChatWithMessages): ChatDetailsModel {
        return chatWithMessages.toDomain()
    }

    override fun getChatDetails(longAddress: String): Flow<ChatDetailsModel> {
        val address = PhoneNumberParser.phoneNumberParser(longAddress)
        return chatDetailsDao.getChatWithMessages(address)
            .map(::mapChatWithMessagesToDomain)
    }

    override suspend fun isChatAddressSaved(longAddress: String): Boolean =
        withContext(Dispatchers.IO) {
            chatDetailsDao.isChatAddressSaved(PhoneNumberParser.phoneNumberParser(longAddress))
        }

    override suspend fun loadChatDetailsToRoom(address: String) {
        withContext(Dispatchers.IO) {
            val chatsDetails = smsContentResolver.getChatDetailsByNumber(address)
            val chatId = chatDetailsDao.insertChat(chatsDetails.toMessageEntity())
            chatDetailsDao.insertMessages(chatsDetails.chatList.map { it.toMessageEntity(chatId) })
        }
    }

    override suspend fun addTextMessage(textMessage: TextMessageModel) {
        addTextMessage(textMessage, customContactName = null)
    }

    private suspend fun ensureChatExists(address: String): Long {
        val existingId = chatDetailsDao.getChatIdByAddress(address)
        return existingId ?: chatDetailsDao.insertChat(
            ChatDetailsEntity(
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

    override suspend fun addTextMessage(textMessage: TextMessageModel, customContactName: String?) {
        withContext(Dispatchers.IO) {
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
                contact = customContactName ?: smsContentResolver.getContactName(textMessage.sender) ?: textMessage.sender,
                updatedAt = textMessage.timeStamp,
                archivedChat = false,
                accountLogoColor = Converters.fromColor(RandomColor.randomColor())
            )

            chatSummaryDao.insertChatSummary(chatSummary)
        }
    }


    override suspend fun createEmptyChatIfNotExists(address: String, contactName: String) {
        withContext(Dispatchers.IO) {
            val exists = chatDetailsDao.isChatAddressSaved(address)
            if (!exists) {
                Log.d("manupruebas", "Chat grupal no existe, creando vacío en Room")

                chatDetailsDao.insertChat(
                    ChatDetailsEntity(
                        address = address,
                        contact = contactName,
                        accountLogoColor = Converters.fromColor(RandomColor.randomColor()),
                        archivedChat = false,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                chatSummaryDao.insertChatSummary(
                    ChatSummaryEntity(
                        address = address,
                        contact = contactName,
                        content = "",
                        timeStamp = System.currentTimeMillis(),
                        status = "",
                        type = "",
                        updatedAt = System.currentTimeMillis(),
                        archivedChat = false,
                        accountLogoColor = Converters.fromColor(RandomColor.randomColor())
                    )
                )
            } else {
                Log.d("manupruebas", "Chat grupal ya existe, cargando desde Room")
            }
        }
    }

    override suspend fun removeMessages(
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

    override suspend fun deleteChats(chatsToBeDeleted: List<String>) {
        withContext(Dispatchers.IO) {
            chatSummaryDao.deleteChatSummariesByAddresses(chatsToBeDeleted)
            val chatIds = chatDetailsDao.getChatIdsByAddresses(chatsToBeDeleted)
            chatDetailsDao.deleteMessagesByChatIds(chatIds)
        }
    }
}