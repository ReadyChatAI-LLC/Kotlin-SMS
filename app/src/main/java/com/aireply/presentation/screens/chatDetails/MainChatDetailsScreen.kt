package com.aireply.presentation.screens.chatDetails

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.data.local.SmsReceiver
import com.aireply.presentation.screens.chatDetails.components.ChatDetailsScreen
import com.aireply.presentation.screens.chatDetails.components.ChatDetailsState
import com.aireply.presentation.screens.shared.ErrorMessage
import com.aireply.presentation.screens.shared.ShimmerEffect
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext // Importa LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatDetailsViewModel = hiltViewModel(), phoneNumber: String, onBack: () -> Unit) {

    Log.d("prueba", "Phone Number: $phoneNumber")

    val chatDetailsState by viewModel.uiState
    val context = LocalContext.current

    // Permission Handling (Simplified - Needs more robust handling in a real app)
    var showRationale by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed.  Good place to load initial data.
            viewModel.getChatMessages(phoneNumber)
        } else {
            showRationale = true
            // Handle permission denial.  Show an error, disable features, etc.
        }
    }



    DisposableEffect(phoneNumber) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        } else {
            SmsReceiver.smsListener = { newMessage ->
                viewModel.addReceivedMessage(newMessage)
            }
            viewModel.getChatMessages(phoneNumber)
        }
        onDispose {
            SmsReceiver.clearSmsListener()
        }
    }



    when (val state = chatDetailsState) {
        is ChatDetailsState.Loading -> {
            ShimmerEffect()
        }
        is ChatDetailsState.Success -> {
            val messageText by viewModel.messageText
            ChatDetailsScreen(
                viewModel = viewModel,
                messageText = messageText,
                messages = state.messages,
                onMessageTextChange = { viewModel.updateMessageText(it) },
                onBack = onBack
            )
        }
        is ChatDetailsState.Error -> ErrorMessage(state.message)
    }


    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Permission Needed") },
            text = { Text("This app needs permission to receive SMS messages to display them in the chat.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                }) {
                    Text("Request Again")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}