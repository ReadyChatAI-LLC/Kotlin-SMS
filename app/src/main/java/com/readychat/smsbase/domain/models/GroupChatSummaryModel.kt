package com.readychat.smsbase.domain.models

import androidx.compose.ui.graphics.Color
import com.readychat.smsbase.data.local.room.entities.GroupChatSummaryEntity
import com.readychat.smsbase.util.Converters

data class GroupChatSummaryModel(
    val id: Int = 0,
    val groupName: String,
    val members: List<String>,
    val lastMessage: String,
    val timeStamp: Long,
    val updatedAt: Long,
    val accountLogoColor: Color
)

fun GroupChatSummaryModel.toEntity(): GroupChatSummaryEntity {
    return GroupChatSummaryEntity(
        id = this.id,
        groupName = this.groupName,
        members = this.members.joinToString(","),
        lastMessage = this.lastMessage,
        timeStamp = this.timeStamp,
        updatedAt = this.updatedAt,
        accountLogoColor = Converters.fromColor(this.accountLogoColor)
    )
}
