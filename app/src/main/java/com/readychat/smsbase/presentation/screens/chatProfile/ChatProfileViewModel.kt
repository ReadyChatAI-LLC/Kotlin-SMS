package com.readychat.smsbase.presentation.screens.chatProfile

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatProfileViewModel @Inject constructor(
    private val chatDetailsRepository: IChatDetailsRepository
): ViewModel() {
    private val _address = mutableStateOf("")
    val address: State<String> get() = _address

    private val _uiState = mutableStateOf<ChatDetailsState>(ChatDetailsState.Loading)
    val uiState: State<ChatDetailsState> get() = _uiState

    fun getChatMessages(){
        viewModelScope.launch {
            try {
                val chatAddress = address.value

                chatDetailsRepository.getChatDetails(chatAddress).collect { chatDetails ->
                    _uiState.value = ChatDetailsState.Success(chatDetails)
                }
            } catch (e: Exception) {
                Log.e("prueba", "ChatProfileViewModel ERROR: ${e.message}")
                _uiState.value = ChatDetailsState.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteConversation(){

    }

    fun updateAddress(address: String){
        _address.value = address
    }
}