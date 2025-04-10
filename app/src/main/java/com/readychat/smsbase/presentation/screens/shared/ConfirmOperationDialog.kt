package com.readychat.smsbase.presentation.screens.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmOperationDialog(confirmationModel: ConfirmationModel) {
    AlertDialog(
        onDismissRequest = confirmationModel.onDismiss,
        title = { Text(confirmationModel.title) },
        text = { Text(confirmationModel.description) },
        confirmButton = {
            TextButton(onClick = confirmationModel.onConfirm) {
                Text("Continue", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = confirmationModel.onDismiss) {
                Text("Cancel")
            }
        }
    )
}