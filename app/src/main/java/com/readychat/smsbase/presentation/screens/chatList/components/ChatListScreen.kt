package com.readychat.smsbase.presentation.screens.chatList.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readychat.smsbase.domain.models.ChatSummaryModel
import com.readychat.smsbase.presentation.screens.shared.ConfirmOperationDialog
import com.readychat.smsbase.presentation.screens.shared.ConfirmationModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatSummaries: List<ChatSummaryModel>,
    navigateToChat: (String) -> Unit,
    navigateToStartChat: () -> Unit,
    onDeletionChat: (Set<Int>) -> Unit,
    onPinChat: (Set<Int>, Boolean) -> Unit,
    onBlockChat: (Set<Int>, Boolean) -> Unit,
    onArchiveChat: (Set<Int>) -> Unit,
    navigateToChatsArchived: () -> Unit
) {

    val context = LocalContext.current

    var selectedChatIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedChatIds.isNotEmpty()
    var showDialogToConfirmOperation by remember { mutableStateOf(false) }
    var confirmationModel by remember {
        mutableStateOf<ConfirmationModel?>(
            null
        )
    }

    if (showDialogToConfirmOperation) {
        confirmationModel?.let { it ->
            ConfirmOperationDialog(it)
        } ?: run {
            Toast.makeText(context, "No action was taken", Toast.LENGTH_SHORT).show()
            showDialogToConfirmOperation = false
            Log.e(
                "prueba",
                "ChatListScreen. Se inicializo un objeto vacio que no debio inicializarse."
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSelectionMode) "${selectedChatIds.size} Selected" else "Chats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    if (isSelectionMode) {
                        val selectedChats = chatSummaries.filter { it.id in selectedChatIds }
                        val chatsArePinned = selectedChats.all { it.isPinned }
                        IconButton(onClick = {
                            onPinChat(selectedChatIds, !chatsArePinned)
                            selectedChatIds = emptySet()
                        }) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Pin chat",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                        IconButton(onClick = {
                            onArchiveChat(selectedChatIds)
                            selectedChatIds = emptySet()
                        }) {
                            Icon(
                                Icons.Default.Archive,
                                contentDescription = "Archive chat",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                        IconButton(onClick = {
                            confirmationModel = ConfirmationModel(
                                title = "Delete Chats",
                                description = "Are you sure you want to delete the selected chats?",
                                onConfirm = {
                                    showDialogToConfirmOperation = false
                                    onDeletionChat(selectedChatIds)
                                    selectedChatIds = emptySet()
                                },
                                onDismiss = {
                                    showDialogToConfirmOperation = false
                                    selectedChatIds = emptySet()
                                }
                            )
                            showDialogToConfirmOperation = true
                        }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete chat",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                        IconButton(onClick = {
                            val selectedChats = chatSummaries.filter { it.id in selectedChatIds }
                            Log.d("prueba", "Chats seleccionados para bloquear: $selectedChats")
                            val chatsAreBlocked = selectedChats.all { it.isBlocked }
                            Log.d("prueba", "Chats seleccionados estan bloqueados?: $chatsAreBlocked")
                            confirmationModel = ConfirmationModel(
                                title = "Block Chats",
                                description = "Are you sure you want to ${if(chatsAreBlocked) "unblock" else "block"} the selected chats?",
                                onConfirm = {
                                    onBlockChat(selectedChatIds, !chatsAreBlocked)
                                    showDialogToConfirmOperation = false
                                    selectedChatIds = emptySet()
                                },
                                onDismiss = {
                                    showDialogToConfirmOperation = false
                                    selectedChatIds = emptySet()
                                }
                            )
                            showDialogToConfirmOperation = true
                        }) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = "Block chat",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            selectedChatIds = emptySet()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Message,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(27.dp),
                            contentDescription = "SmsLogo"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToStartChat() }
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Agregar"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Start chat")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
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
            }
            item {
                TextButton(
                    onClick = { navigateToChatsArchived() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        "Archived Chats",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}