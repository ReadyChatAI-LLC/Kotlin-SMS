package com.aireply.presentation.screens.chatList

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.domain.models.Chat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    navigateToChat: (String) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToStartChat: () -> Unit
) {

    Log.d("prueba", "Entro a ChatListScreen")

    val uiState by viewModel.uiState

    when (val state = uiState) {
        is SmsUiState.Loading -> {
            LaunchedEffect(Unit) { viewModel.loadMessages() }
            CircularProgressIndicator()
        }

        is SmsUiState.Success -> ChatList(
            chats = state.messages,
            navigateToSettings = { navigateToSettings() },
            navigateToChat = { navigateToChat(it) },
            navigateToStartChat = {navigateToStartChat()})

        is SmsUiState.Error -> ErrorMessage(state.message)
    }
}

@Composable
fun ErrorMessage(errorMessage: String) {
    Log.e("prueba", "Error al obtener los mensajes: $errorMessage")
    Text(errorMessage, modifier = Modifier.fillMaxSize())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatList(
    chats: List<Chat>,
    navigateToSettings: () -> Unit,
    navigateToChat: (String) -> Unit,
    navigateToStartChat: () -> Unit
) {
    Log.d("prueba", "La lista de chats cargo")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { navigateToSettings() }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToStartChat() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(chats) { chat ->
                ChatItem(chat = chat, navigateToChat = navigateToChat)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 10.dp))
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, navigateToChat: (String) -> Unit) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .clickable { navigateToChat(chat.contact) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = chat.contact, fontWeight = Bold)
            Text(text = chat.lastMessage.body)
            Text(text = Date(chat.lastMessage.date).toString())
        }
    }
}