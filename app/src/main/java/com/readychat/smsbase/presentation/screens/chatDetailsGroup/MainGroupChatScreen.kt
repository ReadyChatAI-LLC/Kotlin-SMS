package com.readychat.smsbase.presentation.screens.chatDetailsGroup


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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



    LaunchedEffect(groupName, members) {

        viewModel.setGroupInfo(groupName, members)
        viewModel.loadGroupMessages() // 👈 Agrega esta línea
    }

    when (val state = uiState) {
        is ChatDetailsState.Loading -> {
            ShimmerEffect()
        }

        is ChatDetailsState.Success -> {

            GroupChatScreen(
                groupName = groupName,
                messages = state.chatDetails.chatList,
                messageText = messageText,
                onMessageTextChange = {
                    viewModel.updateMessageText(it)
                },
                onNewMessageSent = {
                    viewModel.sendMessageToGroup(
                        content = it.content,
                        message = it,
                        context = context
                    )
                },
                removeMessage = { ids ->
                    val remaining = state.chatDetails.chatList.filterNot { it.messageId in ids }
                    state.chatDetails.chatList.clear()
                    state.chatDetails.chatList.addAll(remaining)
                },
                onBack = onBack
            )
        }

        is ChatDetailsState.Error -> {
            ErrorScreen(
                titleTopBar = groupName,
                errorMessage = state.message,
                onRetry = {},
                onBack = onBack
            )
        }
    }
}
