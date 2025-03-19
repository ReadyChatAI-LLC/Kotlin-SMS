package com.readychat.domain.models

import com.readychat.data.local.room.entity.MessageEntity

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