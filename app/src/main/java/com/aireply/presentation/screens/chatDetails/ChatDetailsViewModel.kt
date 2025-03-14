package com.aireply.presentation.screens.chatDetails

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireply.data.local.SmsSendService
import com.aireply.data.local.repositories.LocalSmsRepository
import com.aireply.domain.models.MessageModel
import com.aireply.domain.models.TextMessageModel
import com.aireply.presentation.screens.shared.ChatDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val localSmsRepository: LocalSmsRepository
) : ViewModel() {

    private val _address = mutableStateOf("")
    val address: State<String> get() = _address

    private val _messageText = mutableStateOf("")
    val messageText: State<String> get() = _messageText

    private val _uiState = mutableStateOf<ChatDetailsState>(ChatDetailsState.Loading)
    val uiState: State<ChatDetailsState> get() = _uiState

    fun getChatMessages(){
        viewModelScope.launch {
            try {
                _uiState.value = ChatDetailsState.Loading
                val chatAddress = address.value

                if (!localSmsRepository.isChatAddressSaved(chatAddress)) {
                    localSmsRepository.loadChatDetailsToRoom(chatAddress)
                }

                localSmsRepository.getChatDetails(chatAddress).collect { chatDetails ->
                    _uiState.value = ChatDetailsState.Success(chatDetails)
                }
            } catch (e: Exception) {
                Log.e("prueba", "ChatViewModel ERROR: ${e.message}")
                _uiState.value = ChatDetailsState.Error(e.message ?: "Unknown Error")
            }
        }
    }


    fun removeMessages(selectedMessages: List<MessageModel>, addressOnChatSummaryChange: String?, newMessage: MessageModel?){
        viewModelScope.launch {
            localSmsRepository.removeMessages(selectedMessages, addressOnChatSummaryChange, newMessage)
        }
    }

    fun sendMessage(textMessage: TextMessageModel, context: Context) {
        viewModelScope.launch {
            localSmsRepository.addTextMessage(textMessage)
        }
        sendSms(context, textMessage)
    }

    fun updateMessageText(messageText: String) {
        _messageText.value = messageText
    }

    fun updateAddress(address: String){
        _address.value = address
    }


    private fun sendSms(context: Context, textMessage: TextMessageModel) {
        val intent = Intent(context, SmsSendService::class.java).apply {
            putExtra("phoneNumber", textMessage.sender)
            putExtra("message", textMessage.content)
        }
        context.startService(intent)
    }

    /*override fun onCleared() {
        super.onCleared()
        SmsReceiver.clearSmsListener()
    }*/
}