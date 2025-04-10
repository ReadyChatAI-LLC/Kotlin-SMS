package com.readychat.smsbase.data.local.repositories

import android.util.Log
import com.readychat.smsbase.data.local.contentResolver.SmsContentResolver
import com.readychat.smsbase.data.local.room.dao.ChatDetailsDao
import com.readychat.smsbase.data.local.room.dao.ChatSummaryDao
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

    override fun getChatDetails(longAddress: String): Flow<ChatDetailsModel> {
        val address = PhoneNumberParser.phoneNumberParser(longAddress)
        return chatDetailsDao.getChatWithMessages(address)
            .map { chatWithMessages ->
                val domainModel = chatWithMessages.toDomain()
                domainModel
            }
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

    override suspend fun updateBlockedChats(
        blockedChat: Boolean,
        ids: List<Int>,
        address: String?
    ) {
        withContext(Dispatchers.IO) {
            if (ids.isEmpty() && address.isNullOrBlank()) {
                throw IllegalArgumentException("You must provide at least one valid id or address")
            }
            if (ids.isNotEmpty() && !address.isNullOrBlank()) {
                throw IllegalArgumentException("Only one of the parameters must be provided: chatIds or address")
            }

            val addressesToUpdate = if (ids.isNotEmpty()) {
                chatSummaryDao.getAddressesByIds(ids)
            } else {
                listOf(address!!)
            }

            chatSummaryDao.updateBlockedChats(blockedChat, ids)

            Log.d("prueba", "Observando a: $addressesToUpdate")

            val exist = chatDetailsDao.getChatIdByAddress(addressesToUpdate[0])

            Log.d("prueba", "Exist?: $exist")

            val updated = chatDetailsDao.updateBlockedChats(blockedChat, addressesToUpdate)

            Log.d("prueba", "Chats Actualizados a $blockedChat: $updated")

            if(updated == 0){
                throw IllegalStateException("Could not be saved in ChatDetails database")
            }

        }
    }

    override suspend fun addTextMessage(textMessage: TextMessageModel) {
        withContext(Dispatchers.IO) {
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
                            isArchived = obj.isArchived
                        )
                    } ?: ChatSummaryEntity(
                        address = textMessage.sender,
                        content = textMessage.content,
                        timeStamp = textMessage.timeStamp,
                        status = textMessage.status,
                        type = textMessage.type,
                        contactName = smsContentResolver.getContactName(textMessage.sender) ?: textMessage.sender,
                        isArchived = false,
                        accountLogoColor = Converters.fromColor(RandomColor.randomColor())
                    )

                val rowId = chatSummaryDao.insertChatSummary(chatSummary)

                if (rowId != -1L) {
                    Log.d("prueba", "Insercion exitosa: $textMessage")
                } else {
                    Log.e("prueba", "Insercion fallo: $textMessage")
                }
            }
        }
    }

    override suspend fun updateArchivedChats(
        archivedChat: Boolean,
        ids: List<Int>,
        address: String?
    ) = withContext(Dispatchers.IO) {
        if (ids.isEmpty() && address.isNullOrBlank()) {
            throw IllegalArgumentException("You must provide at least one valid id or address")
        }
        if (ids.isNotEmpty() && !address.isNullOrBlank()) {
            throw IllegalArgumentException("Only one of the parameters must be provided: chatIds or address")
        }

        val addressesToUpdate = if (ids.isNotEmpty()) {
            chatSummaryDao.getAddressesByIds(ids)
        } else {
            listOf(address!!)
        }

        chatDetailsDao.updateArchivedChats(archivedChat, addressesToUpdate)
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
                    type = it.type
                )
            }
        }
        chatDetailsDao.deleteMessages(messages.map { it.toMessageEntity() })
    }

    override suspend fun deleteChats(ids: List<Int>, address: String?) {
        withContext(Dispatchers.IO) {
            if (ids.isEmpty() && address.isNullOrBlank()) {
                throw IllegalArgumentException("You must provide at least one valid id or address")
            }
            if (ids.isNotEmpty() && !address.isNullOrBlank()) {
                throw IllegalArgumentException("Only one of the parameters must be provided: chatIds or address")
            }

            val addressesToDeleted = if (ids.isNotEmpty()) {
                chatSummaryDao.getAddressesByIds(ids)
            } else {
                listOf(address!!)
            }

            chatSummaryDao.deleteChatSummariesByAddresses(addressesToDeleted)

            val chatIds = chatDetailsDao.getChatIdsByAddresses(addressesToDeleted)
            chatDetailsDao.deleteMessagesByChatIds(chatIds)
        }
    }
}