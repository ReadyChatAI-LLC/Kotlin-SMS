package com.readychat.smsbase.presentation.screens.chatList.chatArchived

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import com.readychat.smsbase.domain.repositories.IChatSummaryRepository
import com.readychat.smsbase.presentation.screens.chatList.components.SmsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatArchivedViewModel @Inject constructor(
    private val chatSummaryRepo: IChatSummaryRepository,
    private val chatDetailsRepo: IChatDetailsRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<SmsUiState>(SmsUiState.Loading)
    val uiState: State<SmsUiState> get() = _uiState

    init {
        viewModelScope.launch {
            Log.d("prueba", "Cargando mensajes archivados desde ROOM")
            try {
                chatSummaryRepo.getArchivedChatSummaries().collect { chatSummaries ->
                    _uiState.value = SmsUiState.Success(chatSummaries)
                }
            } catch (e: Exception) {
                Log.e("prueba", "Error al cargar mensajes archivados")
                _uiState.value = SmsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun deleteChat(addresses: List<String>) {
        viewModelScope.launch {
            try {
                chatDetailsRepo.deleteChats(addresses)
            } catch (e: Exception) {
                Log.e("prueba", "Fallo al eliminar chats: ${e.message}")
                _uiState.value = SmsUiState.Error("Delete Chats Failed: ${e.message}")
            }
        }
    }

    fun unarchiveChats(chatToBeUnarchived: List<Int>) {
        viewModelScope.launch {
            try {
                chatSummaryRepo.updateArchivedChats(false, chatToBeUnarchived)
            } catch (e: Exception) {
                Log.e("prueba", "Fallo el desarchivar chat: ${e.message}")
                _uiState.value = SmsUiState.Error("Unarchive Failed: ${e.message}")
            }
        }
    }
}
