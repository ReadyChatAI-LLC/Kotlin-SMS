package com.readychat.smsbase.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.readychat.smsbase.domain.models.ChatSummaryModel
import com.readychat.smsbase.util.Converters

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
    val contactName: String,
    val isArchived: Boolean,
    val isPinned: Boolean = false,
    val isBlocked: Boolean = false,
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
        contactName = this.contactName,
        isArchived = this.isArchived,
        isPinned = this.isPinned,
        isBlocked = this.isBlocked,
        accountLogoColor = Converters.toColor(this.accountLogoColor)
    )
}