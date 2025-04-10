package com.readychat.smsbase.presentation.screens.chatDetails.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.util.FormatDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onMessageSelected: (Int) -> Unit
) {
    val isFromUser = message.type.toInt() == 2
    val alignment = if (isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor =
        if(isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        //if (isFromUser) Color(0xFF85929e) else Color(0xFF054D67)
    val textColor = if(isFromUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val textAlign = if (isFromUser) TextAlign.End else TextAlign.Start
    val boxColor = if (isSelected) Color(0xFFC9CBCB).copy(alpha = 0.3f) else Color.Transparent

    val messageId = message.messageId

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = boxColor)
            .padding(
                top = 4.dp, bottom = 4.dp,
                start = 5.dp, end = 5.dp
            )
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onMessageSelected(messageId)
                    }
                },
                onLongClick = {
                    if (!isSelectionMode) {
                        onMessageSelected(messageId)
                    }
                }
            ), contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if(isFromUser) 18.dp else 2.dp,
                    bottomEnd = if(isFromUser) 2.dp else 18.dp
                ))
                .wrapContentWidth()
                .widthIn(max = 330.dp)
                .background(bubbleColor)
                .padding(8.dp),
        ) {
            Text(
                text = message.content.trim(),
                textAlign = textAlign,
                fontSize = 18.sp
            )
            Text(
                text = FormatDate.formatDateTime(message.timeStamp),
                color = textColor.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = textAlign
            )
        }
    }
}