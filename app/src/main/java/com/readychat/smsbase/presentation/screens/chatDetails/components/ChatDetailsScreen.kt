package com.readychat.smsbase.presentation.screens.chatDetails.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsScreen(
    messageText: String,
    chatDetails: ChatDetailsModel,
    onSelectImageUri: (Uri?) -> Unit,
    selectedImageUri: Uri?,
    onMessageChange: (String) -> Unit,
    onBack: () -> Unit,
    onSendMessage: (TextMessageModel) -> Unit,
    removeMessage: (Set<Int>) -> Unit,
    onProfileClick: () -> Unit
) {

    var selectedMessageIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedMessageIds.isNotEmpty()

    var showEmojiPicker by remember { mutableStateOf(false) }

    BackHandler {
        if(isSelectionMode){
            selectedMessageIds = emptySet()
        }else{
            onBack()
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                TextButton(onClick = {
                    onProfileClick()
                }) {
                    Text(
                        if (isSelectionMode) "${selectedMessageIds.size} Selected" else chatDetails.contact,
                        fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface
                    )
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
                .imePadding()
        ) {
            MessagesList(
                modifier = Modifier
                    .weight(1f),
                chatDetails.chatList,
                selectedList = selectedMessageIds,
                isSelectionMode = isSelectionMode,
                onScroll = {showEmojiPicker = false},
                onMessageSelected = {
                    selectedMessageIds = if (selectedMessageIds.contains(it)) {
                        selectedMessageIds - it
                    } else {
                        selectedMessageIds + it
                    }
                }
            )

            if(chatDetails.isBlocked){
                BlockedMessageInput()
            }else{
                MessageInput(
                    messageText = messageText,
                    showEmojiPicker = showEmojiPicker,
                    updateShowEmojiPicker = {showEmojiPicker = it},
                    onMessageChange = { onMessageChange(it) },
                    onSendMessage = {
                        if (messageText.isNotBlank() || selectedImageUri != null) {
                            onSendMessage(
                                TextMessageModel(
                                    sender = chatDetails.address,
                                    content = messageText.trim(),
                                    timeStamp = System.currentTimeMillis(),
                                    status = "0",
                                    type = "2"
                                )
                            )
                            onMessageChange("")
                        }
                    },
                    selectedImageUri = selectedImageUri,
                    onSelectImageUri = { uri ->
                        onSelectImageUri(uri)
                    },
                    onDeleteImage = {
                        onSelectImageUri(null)
                    })
            }

        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessagesList(
    modifier: Modifier = Modifier, messages: List<MessageModel>,
    selectedList: Set<Int>,
    onScroll: () -> Unit,
    isSelectionMode: Boolean,
    onMessageSelected: (Int) -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
            keyboardController?.hide()
            onScroll()
            return androidx.compose.ui.geometry.Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            keyboardController?.hide()
            onScroll()
            return super.onPreFling(available)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .nestedScroll(nestedScrollConnection),
        reverseLayout = true
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