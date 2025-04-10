package com.readychat.smsbase.presentation.screens.shared

data class ConfirmationModel(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit
)
