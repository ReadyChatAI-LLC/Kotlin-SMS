package com.readychat.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.readychat.domain.models.ChatSummaryModel
import com.readychat.util.Converters

@Entity(
    tableName = "chat_summary",
    indices = [Index(value = ["address"], unique = true)]
)
data class ChatSummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val address: String,
    val content: String,
    val timeStamp: Long,
    var status: String,
    val type: String,
    val contact: String,
    val updatedAt: Long,
    val archivedChat: Boolean,
    val accountLogoColor: Int
)

fun ChatSummaryEntity.toDomain(): ChatSummaryModel {
    return ChatSummaryModel(
        id = this.id,
        address = this.address,
        content = this.content,
        timeStamp = this.timeStamp,
        status = this.status,
        type = this.type,
        contact = this.contact,
        updatedAt = this.updatedAt,
        archivedChat = this.archivedChat,
        accountLogoColor = Converters.toColor(this.accountLogoColor)
    )
}