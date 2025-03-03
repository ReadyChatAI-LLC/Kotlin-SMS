package com.aireply.presentation.screens.chatList.components

import com.aireply.domain.models.SmsChat

sealed class SmsUiState {
    object Loading : SmsUiState()
    data class Success(val messages: List<SmsChat>) : SmsUiState()
    data class Error(val message: String) : SmsUiState()
}