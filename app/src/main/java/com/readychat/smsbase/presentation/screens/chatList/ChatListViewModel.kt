package com.readychat.smsbase.presentation.screens.chatList

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.SmsReceiver
import com.readychat.smsbase.data.local.dataStore.SettingsDataStore
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import com.readychat.smsbase.domain.repositories.IChatSummaryRepository
import com.readychat.smsbase.domain.repositories.IContactRepository
import com.readychat.smsbase.presentation.screens.chatList.components.SmsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatSummaryRepository: IChatSummaryRepository,
    private val contactRepository: IContactRepository,
    private val chatDetailsRepository: IChatDetailsRepository,
    private val settingsDataStore: SettingsDataStore
): ViewModel() {

    private val _uiState = mutableStateOf<SmsUiState>(SmsUiState.Loading)
    val uiState: State<SmsUiState> get() = _uiState

    // REVISAR FUNCION INIT

    init {
        viewModelScope.launch {
            settingsDataStore.appStartedFlow.collect{ value ->
                if(!value){
                    chatSummaryRepository.loadChatSummariesToRoom()
                    contactRepository.loadContactsToRoom()

                    settingsDataStore.setAppStarted(true)
                }else{
                    Log.d("prueba", "App had already been initialized")
                }
                loadChats()
            }
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            Log.d("prueba", "Cargando mensajes de ROOM")
            try {
                chatSummaryRepository.getChatSummaries().collect{ chatSummaries ->
                    _uiState.value = SmsUiState.Success(chatSummaries)
                }
            } catch (e: Exception) {
                Log.e("prueba", "Error al cargar los mensajes - loadMessages de ChatListViewModel")
                _uiState.value = SmsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun deleteChats(chatsToBeDeleted: List<Int>){
        viewModelScope.launch {
            try {
                Log.d("prueba", "Eliminando chats: $chatsToBeDeleted")
                chatDetailsRepository.deleteChats(chatsToBeDeleted)
            }catch (e: Exception){
                Log.e("prueba", "Fallo el eliminar chats: ${e.message}")
                _uiState.value = SmsUiState.Error("Deletion Chats Failed: ${e.message}")
            }
        }
    }

    fun pinChats(chatsToBePinned: List<Int>, toBePinned: Boolean){
        viewModelScope.launch {
            try {
                Log.d("prueba", "Pineando chats: $chatsToBePinned")
                chatSummaryRepository.updatePinnedChats(toBePinned, chatsToBePinned)
            }catch (e: Exception){
                Log.e("prueba", "Fallo el pinnear chat: ${e.message}")
                _uiState.value = SmsUiState.Error("Pinned Chats Failed: ${e.message}")
            }
        }
    }

    fun archiveChats(chatToBeArchived: List<Int>){
        viewModelScope.launch {
            try {
                Log.d("prueba", "Archivando chats: $chatToBeArchived")
                chatSummaryRepository.updateArchivedChats(true, chatToBeArchived)
            }catch (e: Exception){
                Log.e("prueba", "Fallo el archivar chat: ${e.message}")
                _uiState.value = SmsUiState.Error("Archived Chats Failed: ${e.message}")
            }
        }
    }

    fun blockChats(chatToBeBlocked: List<Int>, toBeBlocked: Boolean){
        viewModelScope.launch {
            try {
                Log.d("prueba", "Bloqueando chats: $chatToBeBlocked")
                chatDetailsRepository.updateBlockedChats(toBeBlocked, chatToBeBlocked)
            }catch (e: Exception){
                Log.e("prueba", "Fallo el blockear chat: ${e.message}")
                _uiState.value = SmsUiState.Error("Blocked Chats Failed: ${e.message}")
            }
        }
    }
}