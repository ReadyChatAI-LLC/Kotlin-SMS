package com.readychat.smsbase.data.local.room.entities

import androidx.compose.ui.graphics.Color
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.models.MessageModel


data class ChatWithMessages(
    @Embedded val chat: ChatDetailsEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chatId"
    )
    val messages: List<MessageEntity>
)


@Entity(
    tableName = "chat_details",
    indices = [Index(value = ["address"], unique = true)]
)
data class ChatDetailsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String,
    val contact: String,
    val accountLogoColor: Int, // ARGB guardado
    val archivedChat: Boolean,
    val updatedAt: Long
)

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = ChatDetailsEntity::class,
        parentColumns = ["id"],
        childColumns = ["chatId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["chatId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0,
    val chatId: Long,
    val content: String,
    val timeStamp: Long,
    val status: String,
    val type: String
)

fun ChatWithMessages.toDomain(): ChatDetailsModel {
    return ChatDetailsModel(
        id = chat.id,
        address = chat.address,
        contact = chat.contact,
        accountLogoColor = Color(chat.accountLogoColor),
        updatedAt = chat.updatedAt,
        archivedChat = chat.archivedChat,
        chatList = messages.map { it.toDomain() }.toMutableList()
    )
}

fun MessageEntity.toDomain(): MessageModel {
    return MessageModel(
        messageId = messageId,
        chatId = chatId,
        content = content,
        timeStamp = timeStamp,
        status = status,
        type = type
    )
}

fun ChatDetailsEntity.toDomain(): ChatDetailsModel{
    return ChatDetailsModel(
        id = id,
        address = address,
        contact = contact,
        accountLogoColor = Color(accountLogoColor),
        updatedAt = updatedAt,
        archivedChat = archivedChat,
        chatList = emptyList<MessageModel>().toMutableList()
    )
}