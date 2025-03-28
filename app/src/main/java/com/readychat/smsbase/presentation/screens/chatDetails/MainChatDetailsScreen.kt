package com.readychat.smsbase.presentation.screens.chatDetails

import android.util.Log
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.presentation.screens.chatDetails.components.ChatDetailsScreen
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatDetailsScreen(
    viewModel: ChatDetailsViewModel = hiltViewModel(),
    address: String,
    onBack: () -> Unit,
    onTopBarClick: (String) -> Unit
) {

    Log.d("prueba", "Address: $address")

    val chatDetailsState by viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.updateAddress(address)
    }

    when (val state = chatDetailsState) {
        is ChatDetailsState.Loading -> {
            LaunchedEffect(Unit) {
                viewModel.getChatMessages()
            }
            ShimmerEffect()
        }

        is ChatDetailsState.Success -> {
            val messageText by viewModel.messageText
            ChatDetailsScreen(
                messageText = messageText,
                messages = state.chatDetails,
                onMessageTextChange = { viewModel.updateMessageText(it) },
                onBack = onBack,
                onNewMessageSent = {textMessage ->
                    viewModel.sendMessage(textMessage, context)
                },
                removeMessage = {
                    val selectedMessages =
                        state.chatDetails.chatList.filter { obj -> obj.messageId in it.toList() }
                    if (selectedMessages.contains(state.chatDetails.chatList.last())) {
                        val newMessage: MessageModel? =
                            state.chatDetails.chatList.filter { messages -> messages.messageId !in it }
                                .maxByOrNull { it.messageId }
                        viewModel.removeMessages(
                            selectedMessages,
                            state.chatDetails.address,
                            newMessage
                        )
                    } else {
                        viewModel.removeMessages(selectedMessages, null, null)
                    }
                },
                onTopBarClick = {onTopBarClick(state.chatDetails.address)})
        }

        is ChatDetailsState.Error -> ErrorScreen(
            titleTopBar = address,
            errorMessage = state.message,
            onRetry = {},
            onBack = { onBack() })
    }
}