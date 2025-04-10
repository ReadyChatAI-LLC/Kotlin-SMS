package com.readychat.smsbase.presentation.screens.groupChat

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.SmsSendService
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.models.TextMessageModel
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.SmsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.compose.ui.graphics.Color

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val chatDetailsRepo: IChatDetailsRepository
) : ViewModel() {

    private var currentGroupName: String = ""
    private var currentRecipients: List<String> = emptyList()

    private val _messageText = mutableStateOf("")
    val messageText: State<String> get() = _messageText

    private val _uiState = mutableStateOf<ChatDetailsState>(
        ChatDetailsState.Success(
            chatDetails = ChatDetailsModel(
                address = "",
                contact = "",
                accountLogoColor = Color.Gray,
                updatedAt = System.currentTimeMillis(),
                chatList = mutableListOf()
            )
        )
    )
    val uiState: State<ChatDetailsState> get() = _uiState

    private val _smsState = mutableStateOf<SmsState?>(null)
    val smsState: State<SmsState?> get() = _smsState

    fun setGroupInfo(groupName: String, recipients: List<String>) {
        currentGroupName = groupName
        currentRecipients = recipients
        Log.d("manupruebas", "setGroupInfo ejecutado con $groupName y $recipients")
    }

    fun updateMessageText(messageText: String) {
        _messageText.value = messageText
    }

    fun sendMessageToGroup(
        content: String,
        message: TextMessageModel,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _smsState.value = SmsState.Sending

            try {
                val fakeGroupAddress = createGroupAddress(currentRecipients)

                val visualMessage = TextMessageModel(
                    sender = fakeGroupAddress,
                    content = content,
                    timeStamp = System.currentTimeMillis(),
                    status = "0",
                    type = "2"
                )
                chatDetailsRepo.addTextMessage(visualMessage, customContactName = currentGroupName)

                currentRecipients.forEach { recipient ->
                    val intent = Intent(context, SmsSendService::class.java).apply {
                        putExtra("phone", recipient)
                        putExtra("message", message.content)
                    }
                    context.startService(intent)
                }

                _smsState.value = SmsState.Sent

            } catch (e: Exception) {
                _smsState.value = SmsState.Error(e.message ?: "Unknown error")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createGroupAddress(members: List<String>): String {
        return members.sorted().joinToString(separator = "_")
    }

    fun loadGroupMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val address = createGroupAddress(currentRecipients)
            Log.d("manupruebas", "loadGroupMessages ejecutado para address: $address")

            try {
                chatDetailsRepo.createEmptyChatIfNotExists(address, currentGroupName)

                chatDetailsRepo.getChatDetails(address).collect { details ->
                    _uiState.value = ChatDetailsState.Success(details)
                }

            } catch (e: Exception) {
                Log.e("manupruebas", "Error al cargar mensajes del grupo: ${e.message}")
                _uiState.value = ChatDetailsState.Error("No se pudieron cargar los mensajes del grupo")
            }
        }
    }
}
