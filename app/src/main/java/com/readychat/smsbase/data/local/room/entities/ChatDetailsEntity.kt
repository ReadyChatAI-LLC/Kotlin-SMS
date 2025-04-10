package com.readychat.smsbase.data.local.room.entities

import android.util.Log
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
    val contactId: String = "",
    val address: String,
    val contact: String,
    val accountLogoColor: Int, // ARGB guardado
    val isArchived: Boolean,
    val isPinned: Boolean = false,
    val isBlocked: Boolean = false,
    val updatedAt: Long,
    var contactSaved: Boolean = true,
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
    return try {
        ChatDetailsModel(
            id = chat.id,
            address = chat.address,
            contact = chat.contact,
            accountLogoColor = Color(chat.accountLogoColor),
            updatedAt = chat.updatedAt,
            contactSaved = chat.contactSaved,
            isArchived = chat.isArchived,
            isPinned = chat.isPinned,
            isBlocked = chat.isBlocked,
            chatList = messages.map { it.toDomain() }.toMutableList()
        )
    } catch (e: Exception) {
        Log.e("prueba", "ChatDetailsEntity -> Error mapping ChatWithMessages to domain: ${e.message}")
        throw e
    }
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
        isArchived = isArchived,
        isPinned = isPinned,
        isBlocked = isBlocked,
        contactSaved = contactSaved,
        chatList = emptyList<MessageModel>().toMutableList()
    )
}