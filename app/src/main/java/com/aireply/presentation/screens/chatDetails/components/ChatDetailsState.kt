package com.aireply.presentation.screens.chatDetails.components

import com.aireply.domain.models.ChatDetailsModel
import com.aireply.domain.models.SmsChat

sealed class ChatDetailsState {
    object Loading : ChatDetailsState()
    data class Success(val messages: ChatDetailsModel) : ChatDetailsState()
    data class Error(val message: String) : ChatDetailsState()
}