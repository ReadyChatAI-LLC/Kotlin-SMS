package com.aireply.presentation.screens.chatList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.presentation.screens.chatList.components.ChatList
import com.aireply.presentation.screens.shared.ShimmerEffect
import com.aireply.presentation.screens.shared.ErrorScreen
import com.aireply.presentation.screens.chatList.components.SmsUiState

@Composable
fun MainChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    navigateToChatDetails: (String) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToStartChat: () -> Unit,
    navigateToSetDefaultScreen: () -> Unit,
    navigateToArchivedChats: () -> Unit
) {

    val uiState by viewModel.uiState

    when (val state = uiState) {
        is SmsUiState.Loading -> {
            ShimmerEffect()
        }

        is SmsUiState.Success -> ChatList(
            chatSummaries = state.messages,
            navigateToSettings = { navigateToSettings() },
            navigateToChat = { navigateToChatDetails(it) },
            navigateToStartChat = { navigateToStartChat() },
            navigateToSetDefaultScreen = { navigateToSetDefaultScreen() },
            onDeletionChat = { viewModel.deleteChat(it.toList(), state.messages) },
            onArchiveChat = { viewModel.archiveChats(it.toList()) },
            navigateToChatsArchived = { navigateToArchivedChats() })

        is SmsUiState.Error -> ErrorScreen(errorMessage = state.message, onRetry = {}, onBack = {})
    }
}