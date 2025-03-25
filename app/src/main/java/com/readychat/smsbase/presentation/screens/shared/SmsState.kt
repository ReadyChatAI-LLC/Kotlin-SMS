package com.readychat.smsbase.presentation.screens.shared


sealed class SmsState {
    object Sending: SmsState()
    object Sent: SmsState()
    data class Error(val errorMessage: String): SmsState()

    override fun toString(): String {
        return when (this) {
            is Sending -> "Sending"
            is Sent -> "Sent"
            is Error -> "Error: $errorMessage"
        }
    }
}