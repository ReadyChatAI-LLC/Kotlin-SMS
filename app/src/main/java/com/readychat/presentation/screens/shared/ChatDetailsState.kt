package com.readychat.presentation.screens.shared

import com.readychat.domain.models.ChatDetailsModel

sealed class ChatDetailsState {
    object Loading : ChatDetailsState()
    data class Success(val chatDetails: ChatDetailsModel) : ChatDetailsState()
    data class Error(val message: String) : ChatDetailsState()
}