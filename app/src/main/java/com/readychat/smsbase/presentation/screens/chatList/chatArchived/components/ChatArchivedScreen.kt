package com.readychat.smsbase.presentation.screens.chatList.chatArchived.components

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.readychat.smsbase.domain.models.ChatSummaryModel
import com.readychat.smsbase.presentation.screens.chatList.components.ChatSummaryItem
import com.readychat.smsbase.presentation.screens.shared.ConfirmOperationDialog
import com.readychat.smsbase.presentation.screens.shared.ConfirmationModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatArchivedScreen(
    chatSummaries: List<ChatSummaryModel>,
    navigateToChat: (String) -> Unit,
    onDeletionChat: (Set<Int>) -> Unit,
    onUnarchiveChat: (Set<Int>) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var selectedChatIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedChatIds.isNotEmpty()
    var showDialogToConfirmDeletion by remember { mutableStateOf(false) }
    var confirmationModel by remember {
        mutableStateOf<ConfirmationModel?>(
            null
        )
    }

    if (showDialogToConfirmDeletion) {
        confirmationModel?.let { it ->
            ConfirmOperationDialog(it)
        }?: run {
            Toast.makeText(context, "No action was taken", Toast.LENGTH_SHORT).show()
            showDialogToConfirmDeletion = false
            Log.e("prueba", "ChatArchivedScreen. Se inicializo un objeto vacio que no debio inicializarse.")
        }
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSelectionMode) "${selectedChatIds.size} Selected" else "Archived") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        selectedChatIds = emptySet()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            onUnarchiveChat(selectedChatIds)
                            selectedChatIds = emptySet()
                        }) {
                            Icon(
                                Icons.Default.Archive,
                                contentDescription = "Archive chat",
                                modifier = Modifier
                                    .size(27.dp)
                                    .graphicsLayer {
                                        rotationZ = 180f
                                    }
                            )
                        }
                        IconButton(onClick = {
                            confirmationModel = ConfirmationModel(
                                title = "Block Chat",
                                description = "Are you sure you want to block this chat?",
                                onConfirm = {
                                    onDeletionChat(selectedChatIds)
                                    showDialogToConfirmDeletion = false
                                    selectedChatIds = emptySet()
                                },
                                onDismiss = {showDialogToConfirmDeletion = false }
                            )
                            showDialogToConfirmDeletion = true
                        }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete chat",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                .fillMaxSize()
        ) {
            items(chatSummaries) { chat ->
                ChatSummaryItem(
                    chatSummaryModel = chat,
                    isSelected = selectedChatIds.contains(chat.id),
                    isSelectionMode = isSelectionMode,
                    onMessageSelected = {
                        selectedChatIds = if (selectedChatIds.contains(it)) {
                            selectedChatIds - it
                        } else {
                            selectedChatIds + it
                        }
                    },
                    navigateToChat = navigateToChat
                )
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}