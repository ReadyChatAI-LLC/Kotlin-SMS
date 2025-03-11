package com.aireply.presentation.screens.chatList

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireply.data.local.SmsReceiver
import com.aireply.data.local.dataStore.SettingsDataStore
import com.aireply.data.local.repositories.LocalSmsRepository
import com.aireply.presentation.screens.chatList.components.SmsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val localSmsRepository: LocalSmsRepository,
    private val settingsDataStore: SettingsDataStore
): ViewModel() {

    private val _uiState = mutableStateOf<SmsUiState>(SmsUiState.Loading)
    val uiState: State<SmsUiState> get() = _uiState

    init {
        viewModelScope.launch {
            settingsDataStore.appStartedFlow.collect{ value ->
                if(!value){
                    localSmsRepository.loadChatSummariesToRoom()
                    localSmsRepository.loadContactsToRoom()

                    settingsDataStore.setAppStarted(true)
                }else{
                    Log.d("prueba", "App had already been initialized")
                }
                loadMessages()
            }
        }

        SmsReceiver.smsListener = { textMessage ->
            Log.i("prueba", "Mensaje recibido!!!: $textMessage")
            viewModelScope.launch {
                localSmsRepository.addTextMessage(textMessage)
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            Log.d("prueba", "Cargando mensajes de ROOM")
            try {
                localSmsRepository.getChatSummaries().collect{ chatSummaries ->
                    _uiState.value = SmsUiState.Success(chatSummaries)
                }
            } catch (e: Exception) {
                Log.e("prueba", "Error al cargar los mensajes - loadMessages de ChatListViewModel")
                _uiState.value = SmsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}