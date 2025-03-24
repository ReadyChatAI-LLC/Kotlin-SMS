package com.readychat.smsbase.presentation.screens.chatList.components

import com.readychat.smsbase.domain.models.ChatSummaryModel

sealed class SmsUiState {
    object Loading : SmsUiState()
    data class Success(val messages: List<ChatSummaryModel>) : SmsUiState()
    data class Error(val message: String) : SmsUiState()
}