package com.aireply.presentation.screens.chatList

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireply.data.local.contentResolver.SmsContentResolver
import com.aireply.presentation.screens.chatList.components.SmsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
                viewModelScope.launch {
                    _uiState.value = SmsUiState.Success(smsContentResolver.getSmsChats())
                }
            } catch (e: Exception) {
                Log.e("prueba", "Error al cargar los mensajes - loadMessages de ChatListViewModel")
                _uiState.value = SmsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}