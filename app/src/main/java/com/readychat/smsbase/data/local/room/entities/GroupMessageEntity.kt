package com.readychat.smsbase.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.readychat.smsbase.domain.models.GroupChatSummaryModel
import com.readychat.smsbase.util.Converters

@Entity(tableName = "group_chat_summary")
data class GroupChatSummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val groupName: String,
    val members: String,
    val lastMessage: String,
    val timeStamp: Long,
    val updatedAt: Long,
    val accountLogoColor: Int
)

fun GroupChatSummaryEntity.toModel(): GroupChatSummaryModel {
    return GroupChatSummaryModel(
        id = this.id,
        groupName = this.groupName,
        members = this.members.split(","),
        lastMessage = this.lastMessage,
        timeStamp = this.timeStamp,
        updatedAt = this.updatedAt,
        accountLogoColor = Converters.toColor(this.accountLogoColor)
    )
}
