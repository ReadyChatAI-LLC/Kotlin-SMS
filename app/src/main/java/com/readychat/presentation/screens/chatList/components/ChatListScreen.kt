package com.readychat.presentation.screens.chatList.components

import android.app.role.RoleManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readychat.domain.models.ChatSummaryModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatList(
    chatSummaries: List<ChatSummaryModel>,
    navigateToSettings: () -> Unit,
    navigateToChat: (String) -> Unit,
    navigateToStartChat: () -> Unit,
    navigateToSetDefaultScreen: () -> Unit,
    onDeletionChat: (Set<Int>) -> Unit,
    onArchiveChat: (Set<Int>) -> Unit,
    navigateToChatsArchived: () -> Unit
) {

    val context = LocalContext.current

    var selectedChatIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedChatIds.isNotEmpty()
    var showDialogToConfirmDeletion by remember { mutableStateOf(false) }

    if (showDialogToConfirmDeletion) {
        ConfirmDeletionDialog(
            onConfirm = {
                onDeletionChat(selectedChatIds)
                showDialogToConfirmDeletion = false
                selectedChatIds = emptySet()
            },
            onDismiss = { showDialogToConfirmDeletion = false })
    }

    LaunchedEffect(Unit) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isDefaultSmsApp = roleManager.isRoleHeld(RoleManager.ROLE_SMS)

        if (isDefaultSmsApp) {
            Log.e("prueba", "Volviendo a DefaultSmsScreen.")
            //navigateToSetDefaultScreen()
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
                            showDialogToConfirmDeletion = true
                        }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete chat",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = { navigateToSettings() }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
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
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                TextButton(
                    onClick = { navigateToChatsArchived() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        "Archived Chats",
                        modifier = Modifier,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}