package com.readychat.domain.models

import androidx.compose.ui.graphics.Color
import com.readychat.data.local.room.entity.ChatSummaryEntity
import com.readychat.util.Converters

data class ChatSummaryModel(
    val id: Int = 0,
    val address: String,
    val content: String,
    val timeStamp: Long,
    var status: String,
    val type: String,
    val contact: String,
    val updatedAt: Long,
    val archivedChat: Boolean,
    val accountLogoColor: Color
)

fun ChatSummaryModel.toMessageEntity(): ChatSummaryEntity {
    return ChatSummaryEntity(
        id = this.id,
        address = this.address,
        content = this.content,
        timeStamp = this.timeStamp,
        status = this.status,
        type = this.type,
        contact = this.contact,
        updatedAt = this.updatedAt,
        archivedChat = this.archivedChat,
        accountLogoColor = Converters.fromColor(this.accountLogoColor)
    )
}