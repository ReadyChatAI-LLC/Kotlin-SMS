package com.readychat.smsbase.presentation.screens.chatList.chatArchived

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.presentation.screens.chatList.chatArchived.components.ChatArchivedScreen
import com.readychat.smsbase.presentation.screens.chatList.components.SmsUiState
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect

@Composable
fun MainChatArchivedScreen(viewModel: ChatArchivedViewModel = hiltViewModel(),
     navigateToChatDetails: (String) -> Unit,
     onBack: () -> Unit
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.loadArchivedChats()
    }

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

        is SmsUiState.Error -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            ErrorScreen(errorMessage = state.message, onRetry = {}, onBack = {
                viewModel.loadArchivedChats()
            })
        }
    }
}