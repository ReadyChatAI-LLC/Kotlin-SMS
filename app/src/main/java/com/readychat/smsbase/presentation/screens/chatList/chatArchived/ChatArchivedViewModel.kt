package com.readychat.smsbase.presentation.screens.chatList.chatArchived

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.repositories.LocalSmsRepository
import com.readychat.smsbase.presentation.screens.chatList.components.SmsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatArchivedViewModel @Inject constructor(
    private val localSmsRepository: LocalSmsRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<SmsUiState>(SmsUiState.Loading)
    val uiState: State<SmsUiState> get() = _uiState

    init {
        viewModelScope.launch {
            Log.d("prueba", "Cargando mensajes de ROOM")
            try {
                localSmsRepository.getArchivedChatSummaries().collect{ chatSummaries ->
                    _uiState.value = SmsUiState.Success(chatSummaries)
                }
            } catch (e: Exception) {
                Log.e("prueba", "Error al cargar los mensajes - loadMessages de ChatListViewModel")
                _uiState.value = SmsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun deleteChat(chatsToBeDeleted: List<Int>){

    }

    fun unarchiveChats(chatToBeUnarchived: List<Int>){
        viewModelScope.launch {
            try {
                localSmsRepository.updateArchivedChats(false, chatToBeUnarchived)
            }catch (e: Exception){
                Log.e("prueba", "Fallo el desarchivar chat: ${e.message}")
                _uiState.value = SmsUiState.Error("Unarchived Chats Failed: ${e.message}")
            }
        }
    }
}