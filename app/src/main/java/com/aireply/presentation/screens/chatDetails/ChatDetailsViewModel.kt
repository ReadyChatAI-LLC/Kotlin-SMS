package com.aireply.presentation.screens.chatDetails

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireply.data.local.SmsSendService
import com.aireply.data.local.contentResolver.SmsContentResolver
import com.aireply.domain.models.ChatDetailsModel
import com.aireply.domain.models.SmsChat
import com.aireply.presentation.screens.chatDetails.components.ChatDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val smsContentResolver: SmsContentResolver
): ViewModel() {

    private val _messageText = mutableStateOf("")
    val messageText: State<String> get() = _messageText

    private val _uiState = mutableStateOf<ChatDetailsState>(ChatDetailsState.Loading)
    val uiState: State<ChatDetailsState> get() = _uiState

    private var currentPhoneNumber: String? = null

    fun getChatMessages(phoneNumber: String){
        currentPhoneNumber = phoneNumber
        viewModelScope.launch {
            try {
                _uiState.value = ChatDetailsState.Success(smsContentResolver.getSmsChatByNumber(phoneNumber))
            }catch (e: Exception){
                Log.e("prueba", "ChatViewModel ERROR: ${e.message}")
                _uiState.value = ChatDetailsState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun updateMessageText(messageText: String){
        _messageText.value = messageText
    }


    fun sendSms(context: Context, phoneNumber: String, message: String) {
        val intent = Intent(context, SmsSendService::class.java).apply {
            putExtra("phoneNumber", phoneNumber)
            putExtra("message", message)
        }
        context.startService(intent)
    }

    fun addReceivedMessage(message: SmsChat) {
        val currentState = _uiState.value
        if (currentState is ChatDetailsState.Success) {
            if (message.sender == currentPhoneNumber || message.contact == currentPhoneNumber) {
                val updatedChatList = currentState.messages.chatList.toMutableList().apply {
                    add(message)
                }
                val updatedChatDetails = currentState.messages.copy(chatList = updatedChatList)
                _uiState.value = ChatDetailsState.Success(updatedChatDetails)
            }
        }
    }
}