package com.readychat.smsbase.domain.models

import com.readychat.smsbase.data.local.room.entities.MessageEntity

data class TextMessageModel(
    val sender: String,
    val content: String,
    val timeStamp: Long,
    val status: String,
    val type: String
)

fun TextMessageModel.toMessageEntity(chatId: Long): MessageEntity{
    return MessageEntity(
        chatId = chatId,
        content = content,
        timeStamp = timeStamp,
        status = status,
        type = type
    )
}