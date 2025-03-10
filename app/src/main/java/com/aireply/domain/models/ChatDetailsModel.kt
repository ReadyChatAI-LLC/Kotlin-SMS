package com.aireply.domain.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.aireply.data.local.room.entity.ChatDetailsEntity
import com.aireply.data.local.room.entity.ChatWithMessages
import com.aireply.data.local.room.entity.MessageEntity

data class ChatDetailsModel(
    val id: Long = 0,
    var address: String,
    var contact: String,
    var accountLogoColor: Color,
    var archivedChat: Boolean = false,
    var updatedAt: Long,
    var chatList: MutableList<MessageModel>
)

data class MessageModel(
    val messageId: Int = 0,
    val chatId: Long,
    val content: String,
    val timeStamp: Long,
    val status: String,
    val type: String
)

fun ChatDetailsModel.toMessageEntity(): ChatDetailsEntity {
    return ChatDetailsEntity(
        id = id,
        address = address,
        contact = contact,
        accountLogoColor = accountLogoColor.toArgb(),
        archivedChat = archivedChat,
        updatedAt = updatedAt
    )
}

fun MessageModel.toMessageEntity(chatId: Long = this.chatId): MessageEntity {
    return MessageEntity(
        messageId = messageId,
        chatId = chatId,
        content = content,
        timeStamp = timeStamp,
        status = status,
        type = type
    )
}