package com.readychat.smsbase.presentation.screens.chatProfile

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.presentation.screens.chatProfile.ChatProfileViewModel
import com.readychat.smsbase.presentation.screens.chatProfile.components.ChatProfileScreen
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect
import com.readychat.smsbase.util.PhoneNumberParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatProfileScreen(
    viewModel: ChatProfileViewModel = hiltViewModel(),
    address: String,
    onBack: () -> Unit
) {

    val chatDetailsState by viewModel.uiState

    LaunchedEffect(Unit) {
        Log.i("prueba", "Address que llega a MainChatProfileScreen: $address")
        viewModel.updateAddress(address)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                navigationIcon = {
                    IconButton(onClick = {onBack()}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when(val state = chatDetailsState){
                is ChatDetailsState.Loading -> {
                    LaunchedEffect(Unit) {
                        viewModel.getChatMessages()
                    }
                    ShimmerEffect()
                }

                is ChatDetailsState.Success -> {
                    ChatProfileScreen(chatDetails = state.chatDetails,
                        onBack = { onBack() },
                        onDeleteConversation = {})
                }
                is ChatDetailsState.Error -> {
                    ErrorScreen(
                        titleTopBar = address,
                        errorMessage = "MainChatDetailsScreen -> ${state.message}",
                        onRetry = {},
                        onBack = { onBack() })
                }
            }
        }
    }
}