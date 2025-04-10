package com.readychat.smsbase.presentation.screens.chatDetails

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.MmsSendService
import com.readychat.smsbase.data.local.SmsSendService
import com.readychat.smsbase.domain.models.MessageModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.SmsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val chatDetailsRepository: IChatDetailsRepository,
) : ViewModel() {

    private val _address = mutableStateOf("")
    val address: State<String> get() = _address

    private val _smsState = mutableStateOf<SmsState?>(null)
    val smsState: State<SmsState?> get() = _smsState

    private val _messageText = mutableStateOf("")
    val messageText: State<String> get() = _messageText

    private val _uiState = mutableStateOf<ChatDetailsState>(ChatDetailsState.Loading)
    val uiState: State<ChatDetailsState> get() = _uiState

    private val _selectedImageUri = mutableStateOf<Uri?>(null)
    val selectedImageUri: State<Uri?> get() = _selectedImageUri

    fun getChatMessages(){
        viewModelScope.launch {
            try {
                _uiState.value = ChatDetailsState.Loading
                val chatAddress = address.value

                if (!chatDetailsRepository.isChatAddressSaved(chatAddress)) {
                    chatDetailsRepository.loadChatDetailsToRoom(chatAddress)
                }

                chatDetailsRepository.getChatDetails(chatAddress).collect { chatDetails ->
                    Log.d("prueba", "chatDetailsViewModel -> ${chatDetails.contactSaved}")
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
            chatDetailsRepository.removeMessages(selectedMessages, addressOnChatSummaryChange, newMessage)
        }
    }

    fun sendMessage(textMessage: TextMessageModel, context: Context) {
        viewModelScope.launch {
            _smsState.value = SmsState.Sending
            Log.d("prueba", "Estado cambiado: ${_smsState.value}")

            try {
                chatDetailsRepository.addTextMessage(textMessage)
                sendSms(context, textMessage)
                _smsState.value = SmsState.Sent
                Log.d("prueba", "Estado cambiado: ${_smsState.value}")
            } catch (e: Exception) {
                _smsState.value = SmsState.Error(e.message ?: "Error desconocido")
                Log.e("prueba", "ChatDetailsViewModel -> Estado cambiado: ${_smsState.value}")
            }
        }
    }

    fun sendMmsMessage(textMessage: TextMessageModel, context: Context) {

        viewModelScope.launch {
            /*_selectedImageUri.value?.let { uri ->
                mmsSender.sendMMSViaIntent(textMessage.sender, textMessage.content, uri)
            }*/


            _selectedImageUri.value?.let { uri ->
                //mmsSender.enviarMMS(textMessage.sender, textMessage.content, uri)
            }
        }
    }

    fun updateMessageText(messageText: String) {
        _messageText.value = messageText
    }

    fun updateAddress(address: String){
        _address.value = address
    }

    fun updateSelectedImageUri(imageUri: Uri?){
        _selectedImageUri.value = imageUri
    }

    fun sendSms(context: Context, textMessage: TextMessageModel) {
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