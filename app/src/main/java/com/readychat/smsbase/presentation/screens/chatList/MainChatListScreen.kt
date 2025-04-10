package com.readychat.smsbase.presentation.screens.chatList

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.presentation.screens.chatList.components.ChatListScreen
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen
import com.readychat.smsbase.presentation.screens.chatList.components.SmsUiState

@Composable
fun MainChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    navigateToChatDetails: (String) -> Unit,
    navigateToStartChat: () -> Unit,
    navigateToArchivedChats: () -> Unit
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState

    when (val state = uiState) {
        is SmsUiState.Loading -> {
            ShimmerEffect()
        }

        is SmsUiState.Success -> ChatListScreen(
            chatSummaries = state.messages,
            navigateToChat = { navigateToChatDetails(it) },
            navigateToStartChat = { navigateToStartChat() },
            onDeletionChat = { viewModel.deleteChats(it.toList()) },
            onPinChat = {selectedChatIds, toBePinned ->
                viewModel.pinChats(selectedChatIds.toList(), toBePinned)
                        },
            onArchiveChat = { viewModel.archiveChats(it.toList()) },
            onBlockChat = {selectedChatIds, toBeBlocked ->
                viewModel.blockChats(selectedChatIds.toList(), toBeBlocked)
                          },
            navigateToChatsArchived = { navigateToArchivedChats() })

        is SmsUiState.Error -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            ErrorScreen(errorMessage = state.message, onRetry = {}, onBack = {
                viewModel.loadChats()
            })
        }
    }
}