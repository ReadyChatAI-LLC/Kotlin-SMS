package com.readychat.smsbase.presentation.screens.chatDetails

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.SmsSendService
import com.readychat.smsbase.data.local.repositories.LocalSmsRepository
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.SmsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val localSmsRepository: LocalSmsRepository
) : ViewModel() {

    private val _address = mutableStateOf("")
    val address: State<String> get() = _address

    private val _smsState = mutableStateOf<SmsState?>(null)
    val smsState: State<SmsState?> get() = _smsState

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
                Log.e("prueba", "ChatDetailsViewModel ERROR: ${e.message}")
                _uiState.value = ChatDetailsState.Error("ChatDetailsViewModel -> ${e.message ?: "Unknown Error"}")
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
            _smsState.value = SmsState.Sending
            Log.d("ChatDetailsViewModelStatus", "Estado cambiado: ${_smsState.value}")

            try {
                localSmsRepository.addTextMessage(textMessage)
                sendSms(context, textMessage)
                _smsState.value = SmsState.Sent
                Log.d("ChatDetailsViewModelStatus", "Estado cambiado: ${_smsState.value}")
            } catch (e: Exception) {
                _smsState.value = SmsState.Error(e.message ?: "Error desconocido")
                Log.e("ChatDetailsViewModelStatus", "Estado cambiado: ${_smsState.value}")
            }
        }
    }

    fun updateMessageText(messageText: String) {
        _messageText.value = messageText
    }

    fun updateAddress(address: String){
        _address.value = address
    }


    private fun sendSms(context: Context, textMessage: TextMessageModel) {
        val intent = Intent(context, SmsSendService::class.java).apply {
            putExtra("phone", textMessage.sender)
            putExtra("message", textMessage.content)
        }
        context.startService(intent)
    }

    /*override fun onCleared() {
        super.onCleared()
        SmsReceiver.clearSmsListener()
    }*/
}