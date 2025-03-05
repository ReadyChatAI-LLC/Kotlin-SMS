package com.aireply.presentation.screens.chatDetails

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.presentation.screens.chatDetails.components.ChatDetailsScreen
import com.aireply.presentation.screens.chatDetails.components.ChatDetailsState
import com.aireply.presentation.screens.shared.ShimmerEffect
import com.aireply.presentation.screens.shared.ErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatDetailsViewModel = hiltViewModel(), phoneNumber: String, onBack: () -> Unit) {

    Log.d("prueba", "Phone Number: $phoneNumber")

    val chatDetailsState by viewModel.uiState

    when (val state = chatDetailsState) {
        is ChatDetailsState.Loading -> {
            LaunchedEffect(Unit) { viewModel.getChatMessages(phoneNumber) }
            ShimmerEffect()
        }

        is ChatDetailsState.Success -> {
            val messageText by viewModel.messageText
            ChatDetailsScreen(messageText = messageText, messages = state.messages, onMessageTextChange = {viewModel.updateMessageText(it)}, onBack = onBack)
        }

        is ChatDetailsState.Error -> ErrorMessage(state.message)
    }
}