package com.readychat.smsbase.presentation.screens.chatDetails

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.presentation.screens.chatDetails.components.ChatDetailsScreen
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen
import com.readychat.smsbase.util.PhoneNumberParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatDetailsScreen(
    viewModel: ChatDetailsViewModel = hiltViewModel(),
    address: String,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit
) {

    val chatDetailsState by viewModel.uiState
    val context = LocalContext.current

    val selectedImageUri by viewModel.selectedImageUri

    LaunchedEffect(Unit) {
        viewModel.updateAddress(PhoneNumberParser.getCleanPhoneNumber(address).number)
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
                chatDetails = state.chatDetails,
                onSelectImageUri = { viewModel.updateSelectedImageUri(it) },
                selectedImageUri = selectedImageUri,
                onMessageChange = { viewModel.updateMessageText(it) },
                onBack = onBack,
                onSendMessage = { textMessage ->
                    if (selectedImageUri != null) {
                        Log.d("prueba", "MMS")
                        viewModel.sendMmsMessage(textMessage, context)
                    } else {
                        Log.d("prueba", "SMS")
                        viewModel.sendMessage(textMessage, context)
                    }
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
                onProfileClick = {
                    Log.i("prueba", "MainChatDetialsScreen en OnProfileClick, address es: $address")
                    onProfileClick(address)
                })
        }

        is ChatDetailsState.Error -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            ErrorScreen(
                titleTopBar = address,
                errorMessage = "ERROR: MainChatDetailsScreen -> ${state.message}",
                onRetry = {},
                onBack = { onBack() })
        }
    }
}