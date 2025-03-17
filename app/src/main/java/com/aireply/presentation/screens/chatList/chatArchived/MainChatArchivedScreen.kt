package com.aireply.presentation.screens.chatList.chatArchived

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.presentation.screens.chatList.chatArchived.components.ChatArchivedScreen
import com.aireply.presentation.screens.chatList.components.SmsUiState
import com.aireply.presentation.screens.shared.ErrorScreen
import com.aireply.presentation.screens.shared.ShimmerEffect

@Composable
fun MainChatArchivedScreen(viewModel: ChatArchivedViewModel = hiltViewModel(),
     navigateToChatDetails: (String) -> Unit,
     onBack: () -> Unit
) {

    val uiState by viewModel.uiState

    when (val state = uiState) {
        is SmsUiState.Loading -> {
            ShimmerEffect()
        }

        is SmsUiState.Success -> ChatArchivedScreen(
            chatSummaries = state.messages,
            navigateToChat = { navigateToChatDetails(it) },
            onDeletionChat = { viewModel.deleteChat(it.toList()) },
            onUnarchiveChat = { viewModel.unarchiveChats(it.toList()) },
            onBack = { onBack() })

        is SmsUiState.Error -> ErrorScreen(errorMessage = state.message, onRetry = {}, onBack = {})
    }
}