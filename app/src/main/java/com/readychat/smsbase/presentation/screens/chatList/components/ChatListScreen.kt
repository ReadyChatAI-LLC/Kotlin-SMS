package com.readychat.smsbase.presentation.screens.chatList.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HelpCenter
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
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

    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedChatIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedChatIds.isNotEmpty()
    var showDialogToConfirmOperation by remember { mutableStateOf(false) }
    var confirmationModel by remember { mutableStateOf<ConfirmationModel?>(null) }

    var showDropdownMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredChatSummaries = if (searchQuery.isBlank()) {
        chatSummaries
    } else {
        chatSummaries.filter { chat ->
            chat.address.contains(searchQuery, ignoreCase = true) || chat.content.contains(
                searchQuery,
                ignoreCase = true
            ) || chat.contactName.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showDialogToConfirmOperation) {
        confirmationModel?.let { model ->
            ConfirmOperationDialog(model)
        } ?: run {
            Toast.makeText(context, "No action was taken", Toast.LENGTH_SHORT).show()
            showDialogToConfirmOperation = false
            Log.e(
                "prueba",
                "ChatListScreen -> Se inicializo un objeto vacio que no debio inicializarse."
            )
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            kotlinx.coroutines.delay(100)
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Scaffold(topBar = {
        ChatListTopBar(
            isSelectionMode = isSelectionMode,
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedChatIds = selectedChatIds,
            onClearSelection = { selectedChatIds = emptySet() },
            onExitSearch = {
                isSearchActive = false
                searchQuery = ""
            },
            onActivateSearch = { isSearchActive = true },
            showDropdownMenu = showDropdownMenu,
            onShowDropdownMenu = { showDropdownMenu = it },
            chatSummaries = chatSummaries,
            onPinChat = onPinChat,
            onArchiveChat = onArchiveChat,
            onShowDeleteConfirmation = {
                confirmationModel = createDeleteConfirmationModel(onConfirm = {
                    showDialogToConfirmOperation = false
                    onDeletionChat(selectedChatIds)
                    selectedChatIds = emptySet()
                }, onDismiss = {
                    showDialogToConfirmOperation = false
                    selectedChatIds = emptySet()
                })
                showDialogToConfirmOperation = true
            },
            onShowBlockConfirmation = { chatsAreBlocked ->
                confirmationModel =
                    createBlockConfirmationModel(chatsAreBlocked = chatsAreBlocked, onConfirm = {
                        onBlockChat(selectedChatIds, !chatsAreBlocked)
                        showDialogToConfirmOperation = false
                        selectedChatIds = emptySet()
                    }, onDismiss = {
                        showDialogToConfirmOperation = false
                        selectedChatIds = emptySet()
                    })
                showDialogToConfirmOperation = true
            },
            searchFocusRequester = searchFocusRequester
        )
    }, floatingActionButton = {
        if (!isSearchActive) {
            FloatingActionButton(onClick = navigateToStartChat) {
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Sms, contentDescription = "Agregar"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Start chat")
                }
            }
        }
    }) { innerPadding ->
        ChatListContent(
            modifier = Modifier.padding(innerPadding),
            filteredChatSummaries = filteredChatSummaries,
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            isSelectionMode = isSelectionMode,
            selectedChatIds = selectedChatIds,
            onMessageSelected = { chatId ->
                selectedChatIds = if (selectedChatIds.contains(chatId)) {
                    selectedChatIds - chatId
                } else {
                    selectedChatIds + chatId
                }
            },
            navigateToChat = navigateToChat,
            navigateToChatsArchived = navigateToChatsArchived
        )
    }
}

private fun createDeleteConfirmationModel(
    onConfirm: () -> Unit, onDismiss: () -> Unit
): ConfirmationModel {
    return ConfirmationModel(
        title = "Delete Chats",
        description = "Are you sure you want to delete the selected chats?",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

private fun createBlockConfirmationModel(
    chatsAreBlocked: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit
): ConfirmationModel {
    return ConfirmationModel(
        title = "Block Chats",
        description = "Are you sure you want to ${if (chatsAreBlocked) "unblock" else "block"} the selected chats?",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ChatListContent(
    modifier: Modifier = Modifier,
    filteredChatSummaries: List<ChatSummaryModel>,
    isSearchActive: Boolean,
    searchQuery: String,
    isSelectionMode: Boolean,
    selectedChatIds: Set<Int>,
    onMessageSelected: (Int) -> Unit,
    navigateToChat: (String) -> Unit,
    navigateToChatsArchived: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        Log.d("prueba", "Items: ${filteredChatSummaries.joinToString { it.id.toString() }}")

        items(filteredChatSummaries) { chat ->
            ChatSummaryItem(
                chatSummaryModel = chat,
                isSelected = selectedChatIds.contains(chat.id),
                isSelectionMode = isSelectionMode,
                onMessageSelected = onMessageSelected,
                navigateToChat = navigateToChat
            )
        }

        if (isSearchActive && searchQuery.isNotBlank() && filteredChatSummaries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isSearchActive || searchQuery.isBlank()) {
            item {
                TextButton(
                    onClick = navigateToChatsArchived,
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