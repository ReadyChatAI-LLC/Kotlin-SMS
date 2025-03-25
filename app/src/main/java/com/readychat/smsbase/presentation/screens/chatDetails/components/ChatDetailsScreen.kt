package com.readychat.smsbase.presentation.screens.chatDetails.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.util.FormatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsScreen(
    messageText: String,
    messages: ChatDetailsModel,
    onMessageTextChange: (String) -> Unit,
    onBack: () -> Unit,
    onNewMessageSent: (TextMessageModel) -> Unit,
    removeMessage: (Set<Int>) -> Unit,
    onTopBarClick: () -> Unit
) {

    var selectedMessageIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedMessageIds.isNotEmpty()

    BackHandler {
        onBack()
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                TextButton(onClick = { onTopBarClick() }) {
                    Text(if (isSelectionMode) "${selectedMessageIds.size} Selected" else messages.contact,
                        fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (isSelectionMode)
                        selectedMessageIds = emptySet()
                    else
                        onBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            actions = {
                if (isSelectionMode) {
                    IconButton(onClick = {
                        removeMessage(selectedMessageIds)
                        selectedMessageIds = emptySet()
                    }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete message",
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
            }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MessagesList(
                modifier = Modifier.weight(1f),
                messages.chatList,
                selectedList = selectedMessageIds,
                isSelectionMode = isSelectionMode,
                onMessageSelected = {
                    selectedMessageIds = if (selectedMessageIds.contains(it)) {
                        selectedMessageIds - it
                    } else {
                        selectedMessageIds + it
                    }
                }
            )

            var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

            MessageInput(
                messageText = messageText,
                onMessageChange = { onMessageTextChange(it) },
                onSendMessage = { Uri->

                    if (messageText.isNotBlank()) {

                        onNewMessageSent(
                            TextMessageModel(
                                sender = messages.address,
                                content = messageText.trim(),
                                timeStamp = System.currentTimeMillis(),
                                status = "0",
                                type = "2"
                            )
                        )
                        onMessageTextChange("")
                    }
                },
                onEmojiClicked = {
                    // Muestra tu selector de emojis
                },
                selectedImageUri = selectedImageUri,
                onImageSelected = { uri ->
                    selectedImageUri = uri
                },
                onDeleteImage = {
                    selectedImageUri = null
                })
        }
    }
}

@Composable
fun MessagesList(
    modifier: Modifier = Modifier, messages: List<MessageModel>,
    selectedList: Set<Int>,
    isSelectionMode: Boolean,
    onMessageSelected: (Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(), reverseLayout = true
    ) {
        items(messages.reversed()) { message ->
            MessageBubble(
                message = message,
                isSelected = selectedList.contains(message.messageId),
                isSelectionMode = isSelectionMode,
                onMessageSelected = { onMessageSelected(it) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onMessageSelected: (Int) -> Unit
) {
    val isFromMe = message.type.toInt() == 2
    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor =
        if (isFromMe) Color(0xFF85929e) else MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (isFromMe) Color.Black else Color.White
    val textAlign = if (isFromMe) TextAlign.End else TextAlign.Start
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
                .clip(MaterialTheme.shapes.medium)
                .wrapContentWidth()
                .widthIn(max = 330.dp)
                .background(bubbleColor)
                .padding(8.dp),
        ) {
            Text(
                text = message.content.trim(),
                color = textColor,
                textAlign = textAlign,
                fontSize = 18.sp
            )
            Text(
                text = FormatDate.formatDateTime(message.timeStamp),
                color = textColor.copy(alpha = 0.7f),
                //modifier = Modifier.fillMaxWidth(),
                fontSize = 11.sp,
                textAlign = textAlign
            )
        }
    }
}