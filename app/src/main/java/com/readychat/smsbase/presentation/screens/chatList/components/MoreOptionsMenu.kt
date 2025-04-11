package com.readychat.smsbase.presentation.screens.chatList.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.HelpCenter
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.readychat.smsbase.domain.models.ChatSummaryModel

@Composable
fun MoreOptionsMenu(
    showDropdownMenu: Boolean,
    onShowDropdownMenu: (Boolean) -> Unit,
    isSelectionMode: Boolean,
    selectedChats: List<ChatSummaryModel>,
    onShowBlockConfirmation: (Boolean) -> Unit
) {
    Box {
        IconButton(onClick = { onShowDropdownMenu(true) }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More Options",
                modifier = Modifier.size(27.dp)
            )
        }

        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { onShowDropdownMenu(false) }
        ) {
            // Settings option
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = { onShowDropdownMenu(false) },
                leadingIcon = {
                    Icon(Icons.Outlined.Settings, contentDescription = null)
                }
            )

            // Block option
            if (isSelectionMode) {
                val chatsAreBlocked = selectedChats.all { it.isBlocked }
                DropdownMenuItem(
                    text = { Text("Block Chats") },
                    onClick = {
                        onShowBlockConfirmation(chatsAreBlocked)
                        onShowDropdownMenu(false)
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Block, contentDescription = null)
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("View blocked chats") },
                    onClick = { onShowDropdownMenu(false) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Block, contentDescription = null)
                    }
                )
            }
/*
            HorizontalDivider()

            // Help option
            DropdownMenuItem(
                text = { Text("Help & feedback") },
                onClick = { onShowDropdownMenu(false) },
                leadingIcon = {
                    Icon(Icons.Outlined.HelpCenter, contentDescription = null)
                }
            )*/
        }
    }
}