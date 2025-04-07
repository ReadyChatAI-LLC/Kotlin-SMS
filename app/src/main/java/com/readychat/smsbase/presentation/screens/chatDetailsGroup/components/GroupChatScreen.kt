package com.readychat.smsbase.presentation.screens.groupChat

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.presentation.screens.chatDetails.components.MessagesList
import com.readychat.smsbase.presentation.screens.chatDetails.components.MessageInput



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    groupName: String,
    messages: List<MessageModel>,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onNewMessageSent: (TextMessageModel) -> Unit,
    removeMessage: (Set<Int>) -> Unit,
    onBack: () -> Unit,
) {
    var selectedMessageIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedMessageIds.isNotEmpty()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    BackHandler { onBack() }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    if (isSelectionMode) "${'$'}{selectedMessageIds.size} Selected" else groupName,
                    fontSize = 18.sp
                )
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
            actions = {
                if (isSelectionMode) {
                    IconButton(onClick = {
                        removeMessage(selectedMessageIds)
                        selectedMessageIds = emptySet()
                    }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete message",
                            modifier = Modifier.padding(end = 8.dp)
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
                messages = messages,
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

            MessageInput(
                messageText = messageText,
                onMessageChange = onMessageTextChange,
                onSendMessage = { uri ->
                    if (messageText.isNotBlank()) {
                        val newMessage = TextMessageModel(
                            sender = groupName,
                            content = messageText.trim(),
                            timeStamp = System.currentTimeMillis(),
                            status = "0",
                            type = "2"
                        )
                        selectedImageUri = null
                        onNewMessageSent(newMessage)
                    }
                },
                onEmojiClicked = {},
                selectedImageUri = selectedImageUri,
                onImageSelected = { selectedImageUri = it },
                onDeleteImage = { selectedImageUri = null }
            )
        }
    }
}
