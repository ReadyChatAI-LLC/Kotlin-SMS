package com.readychat.smsbase.presentation.screens.chatList.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.readychat.smsbase.domain.models.ChatSummaryModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBar(
    isSelectionMode: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedChatIds: Set<Int>,
    onClearSelection: () -> Unit,
    onExitSearch: () -> Unit,
    onActivateSearch: () -> Unit,
    showDropdownMenu: Boolean,
    onShowDropdownMenu: (Boolean) -> Unit,
    chatSummaries: List<ChatSummaryModel>,
    onPinChat: (Set<Int>, Boolean) -> Unit,
    onArchiveChat: (Set<Int>) -> Unit,
    onShowDeleteConfirmation: () -> Unit,
    onShowBlockConfirmation: (Boolean) -> Unit,
    searchFocusRequester: FocusRequester
) {
    TopAppBar(
        title = {
            if (isSearchActive) {
                SearchField(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    searchFocusRequester = searchFocusRequester
                )
            } else {
                Text(if (isSelectionMode) "${selectedChatIds.size}" else "Messages")
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ), actions = {
            if (isSelectionMode) {
                SelectionModeActions(
                    selectedChatIds = selectedChatIds,
                    chatSummaries = chatSummaries,
                    onPinChat = onPinChat,
                    onArchiveChat = onArchiveChat,
                    onShowDeleteConfirmation = onShowDeleteConfirmation,
                    showDropdownMenu = showDropdownMenu,
                    onShowDropdownMenu = onShowDropdownMenu,
                    onShowBlockConfirmation = onShowBlockConfirmation
                )
            } else if (!isSearchActive) {
                NormalModeActions(
                    onActivateSearch = onActivateSearch,
                    showDropdownMenu = showDropdownMenu,
                    onShowDropdownMenu = onShowDropdownMenu
                )
            }
        }, navigationIcon = {
            TopBarNavigationIcon(
                isSelectionMode = isSelectionMode,
                isSearchActive = isSearchActive,
                onClearSelection = onClearSelection,
                onExitSearch = onExitSearch
            )
        })
}

@Composable
fun SelectionModeActions(
    selectedChatIds: Set<Int>,
    chatSummaries: List<ChatSummaryModel>,
    onPinChat: (Set<Int>, Boolean) -> Unit,
    onArchiveChat: (Set<Int>) -> Unit,
    onShowDeleteConfirmation: () -> Unit,
    showDropdownMenu: Boolean,
    onShowDropdownMenu: (Boolean) -> Unit,
    onShowBlockConfirmation: (Boolean) -> Unit
) {
    val selectedChats = chatSummaries.filter { it.id in selectedChatIds }
    val chatsArePinned = selectedChats.all { it.isPinned }

    IconButton(onClick = {
        onPinChat(selectedChatIds, !chatsArePinned)
    }) {
        Icon(
            Icons.Outlined.PushPin, contentDescription = "Pin chat", modifier = Modifier.size(25.dp)
        )
    }

    IconButton(onClick = {
        onArchiveChat(selectedChatIds)
    }) {
        Icon(
            Icons.Outlined.Archive,
            contentDescription = "Archive chat",
            modifier = Modifier.size(25.dp)
        )
    }

    IconButton(onClick = onShowDeleteConfirmation) {
        Icon(
            Icons.Outlined.Delete,
            contentDescription = "Delete chat",
            modifier = Modifier.size(25.dp)
        )
    }

    MoreOptionsMenu(
        showDropdownMenu = showDropdownMenu,
        onShowDropdownMenu = onShowDropdownMenu,
        isSelectionMode = true,
        selectedChats = selectedChats,
        onShowBlockConfirmation = onShowBlockConfirmation
    )
}

@Composable
fun NormalModeActions(
    onActivateSearch: () -> Unit, showDropdownMenu: Boolean, onShowDropdownMenu: (Boolean) -> Unit
) {
    IconButton(onClick = onActivateSearch) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = "Search messages",
            modifier = Modifier.size(25.dp)
        )
    }

    MoreOptionsMenu(
        showDropdownMenu = showDropdownMenu,
        onShowDropdownMenu = onShowDropdownMenu,
        isSelectionMode = false,
        selectedChats = emptyList(),
        onShowBlockConfirmation = {

        })
}

@Composable
fun TopBarNavigationIcon(
    isSelectionMode: Boolean,
    isSearchActive: Boolean,
    onClearSelection: () -> Unit,
    onExitSearch: () -> Unit
) {
    if (isSelectionMode || isSearchActive) {
        IconButton(onClick = {
            if (isSelectionMode) {
                onClearSelection()
            } else {
                onExitSearch()
            }
        }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
    } else {
        Icon(
            imageVector = Icons.Outlined.Sms,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(25.dp),
            contentDescription = "Sms Logo"
        )
    }
}

@Composable
fun SearchField(
    searchQuery: String, onSearchQueryChange: (String) -> Unit, searchFocusRequester: FocusRequester
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFocusRequester),
            placeholder = { Text("Search messages") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
            )
        )
    }
}