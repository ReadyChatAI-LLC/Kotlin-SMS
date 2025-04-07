package com.readychat.smsbase.presentation.screens.groupChat

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.SmsSendService
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.SmsState
import kotlinx.coroutines.launch

class GroupChatViewModel : ViewModel() {

    private val _messageText = mutableStateOf("")
    val messageText: State<String> get() = _messageText

    private val _uiState = mutableStateOf<ChatDetailsState>(ChatDetailsState.Success(
        chatDetails = com.readychat.smsbase.domain.models.ChatDetailsModel(
            address = "",
            contact = "",
            accountLogoColor = androidx.compose.ui.graphics.Color.Gray,
            updatedAt = System.currentTimeMillis(),
            chatList = mutableListOf()
        )
    ))
    val uiState: State<ChatDetailsState> get() = _uiState

    private val _smsState = mutableStateOf<SmsState?>(null)
    val smsState: State<SmsState?> get() = _smsState

    fun updateMessageText(messageText: String) {
        _messageText.value = messageText
    }

    fun sendMessageToGroup(
        message: TextMessageModel,
        recipients: List<String>,
        context: Context
    ) {
        viewModelScope.launch {
            _smsState.value = SmsState.Sending
            try {
                recipients.forEach { recipient ->
                    val intent = Intent(context, SmsSendService::class.java).apply {
                        putExtra("phone", recipient)
                        putExtra("message", message.content)
                    }
                    context.startService(intent)
                }
                _smsState.value = SmsState.Sent
                Toast.makeText(context, "Message sent to group", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _smsState.value = SmsState.Error(e.message ?: "Unknown error")
                Toast.makeText(context, "Error: ${'$'}{e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun appendMessageToChat(message: MessageModel) {
        val current = _uiState.value
        if (current is ChatDetailsState.Success) {
            current.chatDetails.chatList.add(message)
        }
    }
}