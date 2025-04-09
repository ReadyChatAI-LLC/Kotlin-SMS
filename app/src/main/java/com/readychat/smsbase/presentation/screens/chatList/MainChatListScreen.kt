package com.readychat.smsbase.presentation.screens.chatList

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.presentation.screens.chatList.components.ChatList
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen
import com.readychat.smsbase.presentation.screens.chatList.components.SmsUiState
import com.readychat.smsbase.domain.models.ChatSummaryModel

@Composable
fun MainChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    navigateToChatDetails: (String) -> Unit,
    navigateToGroupChat: (String, List<String>) -> Unit, // ✅ agregado para grupos
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
            navigateToChat = { address ->
                if (address.contains("_")) {
                    val summary = state.messages.find { it.address == address }
                    if (summary != null) {
                        val members = address.split("_")
                        navigateToGroupChat(summary.contact, members)
                    }
                } else {
                    navigateToChatDetails(address)
                }
            },
            navigateToStartChat = { navigateToStartChat() },
            navigateToSetDefaultScreen = { navigateToSetDefaultScreen() },
            onDeletionChat = { viewModel.deleteChat(it.toList(), state.messages) },
            onArchiveChat = { viewModel.archiveChats(it.toList()) },
            navigateToChatsArchived = { navigateToArchivedChats() }
        )

        is SmsUiState.Error -> ErrorScreen(errorMessage = state.message, onRetry = {}, onBack = {})
    }
}
