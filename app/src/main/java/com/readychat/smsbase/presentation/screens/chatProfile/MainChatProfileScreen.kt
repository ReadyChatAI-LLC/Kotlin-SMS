package com.readychat.smsbase.presentation.screens.chatProfile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.presentation.screens.chatProfile.components.ChatProfileScreen
import com.readychat.smsbase.presentation.screens.shared.ChatDetailsState
import com.readychat.smsbase.presentation.screens.shared.ErrorScreen
import com.readychat.smsbase.presentation.screens.shared.ShimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatProfileScreen(
    viewModel: ChatProfileViewModel = hiltViewModel(),
    address: String,
    onBack: () -> Unit
) {

    val context = LocalContext.current
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
                    IconButton(onClick = { onBack() }) {
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
            when (val state = chatDetailsState) {
                is ChatDetailsState.Loading -> {
                    LaunchedEffect(Unit) {
                        viewModel.getChatDetails()
                    }
                    ShimmerEffect()
                }

                is ChatDetailsState.Success -> {
                    ChatProfileScreen(
                        modifier = Modifier.weight(1f),
                        chatDetails = state.chatDetails,
                        onBack = { onBack() },
                        onDeleteChat = {
                            if (it.isNotBlank()) {
                                viewModel.deleteChat(it)
                                onBack()
                            } else
                                Log.e(
                                    "prueba",
                                    "MainChatProfileScreen. Address es null y no se puede eliminar"
                                )
                        },
                        onBlockChat = {
                            if (it.isNotBlank()) {
                                viewModel.blockChat(it)
                                onBack()
                            } else
                                Log.e(
                                    "prueba",
                                    "MainChatProfileScreen. Address es null y no se puede bloquear"
                                )
                        }
                    )
                }

                is ChatDetailsState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    ErrorScreen(
                        titleTopBar = address,
                        errorMessage = "MainChatDetailsScreen -> ${state.message}",
                        onRetry = {viewModel.getChatDetails()},
                        onBack = { onBack() })
                }
            }
        }
    }
}