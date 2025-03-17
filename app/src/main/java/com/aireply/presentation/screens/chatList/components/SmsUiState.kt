package com.aireply.presentation.screens.chatList.components

import com.aireply.domain.models.ChatSummaryModel

sealed class SmsUiState {
    object Loading : SmsUiState()
    data class Success(val messages: List<ChatSummaryModel>) : SmsUiState()
    data class Error(val message: String) : SmsUiState()
}