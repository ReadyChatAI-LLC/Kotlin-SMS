package com.readychat.smsbase.presentation.screens.chatList.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readychat.smsbase.domain.models.ChatSummaryModel
import com.readychat.smsbase.util.FormatDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatSummaryItem(
    chatSummaryModel: ChatSummaryModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onMessageSelected: (Int) -> Unit,
    navigateToChat: (String) -> Unit
) {


    val boxColor =
        if (isSelected) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp, start = 5.dp, end = 5.dp)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onMessageSelected(chatSummaryModel.id)
                    } else {
                        navigateToChat(chatSummaryModel.address)
                    }
                },
                onLongClick = {
                    if (!isSelectionMode) {
                        onMessageSelected(chatSummaryModel.id)
                    }
                }
            )
            .background(color = boxColor, shape = RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "AccountRepresentation",
                modifier = Modifier
                    .size(50.dp),
                tint = chatSummaryModel.accountLogoColor
            )
            Column(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = chatSummaryModel.contact, fontWeight = Bold)
                val messageBody =
                    if (chatSummaryModel.content.length > 80) chatSummaryModel.content.take(80) + " ..." else chatSummaryModel.content
                Row {
                    if (chatSummaryModel.type.toInt() == 2) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Sms Sent",
                            modifier = Modifier
                                .size(23.dp)
                                .padding(3.dp)
                        )
                    }
                    Text(text = messageBody, style = TextStyle())
                }
            }
            Text(
                text = FormatDate.formatDate(chatSummaryModel.updatedAt),
                modifier = Modifier.weight(1f),
                style = TextStyle(fontSize = 11.sp, textAlign = TextAlign.End)
            )
        }
    }
}