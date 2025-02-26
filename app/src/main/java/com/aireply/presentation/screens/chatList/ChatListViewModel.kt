package com.aireply.presentation.screens.chatList

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireply.data.local.contentResolver.SmsContentResolver
import com.aireply.data.local.dataStore.SettingsDataStore
import com.aireply.domain.models.Chat
import com.aireply.domain.models.SmsMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val smsContentResolver: SmsContentResolver
): ViewModel() {

    private val _uiState = mutableStateOf<SmsUiState>(SmsUiState.Loading)
    val uiState: State<SmsUiState> get() = _uiState

    fun loadMessages() {
        viewModelScope.launch {
            Log.d("prueba", "1. Cargando mensajes - loadMessages de ChatListViewModel")
            try {
                val messages = groupMessagesByContact()
                Log.d("prueba", "2. Cargando mensajes - loadMessages de ChatListViewModel")
                _uiState.value = SmsUiState.Success(messages)
                Log.d("prueba", "3. Mensajes cargados - loadMessages de ChatListViewModel")
            } catch (e: Exception) {
                Log.e("prueba", "Error al cargar los mensajes - loadMessages de ChatListViewModel")
                _uiState.value = SmsUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun groupMessagesByContact(): List<Chat> {
        var list = emptyList<Chat>()
        Log.d("prueba", "groupMessagesByContact - ChatListViewModel")
        viewModelScope.launch {
            list = smsContentResolver.groupMessagesByContact()
        }
        return list
    }
}

sealed class SmsUiState {
    object Loading : SmsUiState()
    data class Success(val messages: List<Chat>) : SmsUiState()
    data class Error(val message: String) : SmsUiState()
}