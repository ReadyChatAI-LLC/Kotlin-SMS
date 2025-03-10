package com.aireply.presentation.screens.chatList

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.presentation.screens.chatList.components.ChatList
import com.aireply.presentation.screens.shared.ShimmerEffect
import com.aireply.presentation.screens.shared.ErrorScreen
import com.aireply.presentation.screens.chatList.components.SmsUiState

@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    navigateToChat: (String) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToStartChat: () -> Unit,
    navigateToSetDefaultScreen: () -> Unit
) {

    Log.d("prueba", "Entro a ChatListScreen")

    val uiState by viewModel.uiState

    when (val state = uiState) {
        is SmsUiState.Loading -> {
            ShimmerEffect()
        }

        is SmsUiState.Success -> ChatList(
            chatSummaries = state.messages,
            navigateToSettings = { navigateToSettings() },
            navigateToChat = { navigateToChat(it) },
            navigateToStartChat = {navigateToStartChat()},
            navigateToSetDefaultScreen = {navigateToSetDefaultScreen()})

        is SmsUiState.Error -> ErrorScreen(errorMessage = state.message, onRetry = {}, onBack = {})
    }
}