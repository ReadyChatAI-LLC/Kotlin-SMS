package com.readychat.smsbase.presentation.screens.chatDetailsGroup


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect
import com.readychat.smsbase.presentation.screens.groupChat.GroupChatScreen
import com.readychat.smsbase.presentation.screens.groupChat.GroupChatViewModel


@Composable
fun MainGroupChatScreen(
    groupName: String,
    members: List<String>,
    viewModel: GroupChatViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val messageText by viewModel.messageText

    when (val state = uiState) {
        is ChatDetailsState.Loading -> {
            ShimmerEffect()
        }
        is ChatDetailsState.Success -> {
            GroupChatScreen(
                groupName = groupName,
                messages = state.chatDetails.chatList,
                messageText = messageText,
                onMessageTextChange = { viewModel.updateMessageText(it) },
                onNewMessageSent = {
                    val newMessage = MessageModel(
                        chatId = 0,
                        content = it.content,
                        timeStamp = System.currentTimeMillis(),
                        status = "0",
                        type = "2"
                    )
                    viewModel.appendMessageToChat(newMessage)
                    viewModel.sendMessageToGroup(it, members, context)
                },
                removeMessage = { ids ->
                    val remaining = state.chatDetails.chatList.filterNot { it.messageId in ids }
                    state.chatDetails.chatList.clear()
                    state.chatDetails.chatList.addAll(remaining)
                },
                onBack = onBack
            )
        }
        is ChatDetailsState.Error -> ErrorScreen(
            titleTopBar = groupName,
            errorMessage = state.message,
            onRetry = {},
            onBack = { onBack() }
        )
    }
}
