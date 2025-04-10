package com.readychat.smsbase.presentation.screens.chatList

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.SmsReceiver
import com.readychat.smsbase.data.local.dataStore.SettingsDataStore
import com.readychat.smsbase.domain.models.ChatSummaryModel
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import com.readychat.smsbase.domain.repositories.IChatSummaryRepository
import com.readychat.smsbase.domain.repositories.IContactRepository
import com.readychat.smsbase.presentation.screens.chatList.components.SmsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatDetailsRepo: IChatDetailsRepository,
    private val chatSummaryRepo: IChatSummaryRepository,
    private val contactRepo: IContactRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = mutableStateOf<SmsUiState>(SmsUiState.Loading)
    val uiState: State<SmsUiState> get() = _uiState

    init {
        viewModelScope.launch {
            settingsDataStore.appStartedFlow.collect { value ->
                if (!value) {
                    chatSummaryRepo.loadChatSummariesToRoom()
                    contactRepo.loadContactsToRoom()

                    settingsDataStore.setAppStarted(true)
                } else {
                    Log.d("prueba", "App had already been initialized")
                }
                loadMessages()
            }
        }

        SmsReceiver.smsListener = { textMessage ->
            Log.i("prueba", "Mensaje recibido!!!: $textMessage")
            viewModelScope.launch {
                chatDetailsRepo.addTextMessage(textMessage)
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            Log.d("prueba", "Cargando mensajes de ROOM")
            try {
                chatSummaryRepo.getChatSummaries().collect { chatSummaries ->
                    _uiState.value = SmsUiState.Success(chatSummaries)
                }
            } catch (e: Exception) {
                Log.e("prueba", "Error al cargar los mensajes - loadMessages de ChatListViewModel")
                _uiState.value = SmsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun deleteChat(chatsToBeDeleted: List<Int>, summaries: List<ChatSummaryModel>) {
        val addresses: List<String> = summaries.filter { it.id in chatsToBeDeleted }
            .map { it.address }

        viewModelScope.launch {
            try {
                chatDetailsRepo.deleteChats(addresses)
            } catch (e: Exception) {
                Log.e("prueba", "Fallo el eliminar chats: ${e.message}")
                _uiState.value = SmsUiState.Error("Deletion Chats Failed: ${e.message}")
            }
        }
    }

    fun archiveChats(chatToBeArchived: List<Int>) {
        viewModelScope.launch {
            try {
                chatSummaryRepo.updateArchivedChats(true, chatToBeArchived)
            } catch (e: Exception) {
                Log.e("prueba", "Fallo el archivar chat: ${e.message}")
                _uiState.value = SmsUiState.Error("Archived Chats Failed: ${e.message}")
            }
        }
    }
}
