package com.aireply.presentation.screens.chatList.components

import android.app.role.RoleManager
import android.content.Context
import android.provider.Telephony
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aireply.domain.models.SmsChat
import com.aireply.util.FormatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatList(
    smsChats: List<SmsChat>,
    navigateToSettings: () -> Unit,
    navigateToChat: (String) -> Unit,
    navigateToStartChat: () -> Unit,
    navigateToSetDefaultScreen: () -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isDefaultSmsApp = roleManager.isRoleHeld(RoleManager.ROLE_SMS)

        if (isDefaultSmsApp) {
            Log.e("prueba", "Volviendo a DefaultSmsScreen.")
            //navigateToSetDefaultScreen()
        }
    }

    Log.d("prueba", "La lista de chats cargo")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    IconButton(onClick = { navigateToSettings() }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                navigationIcon = {
                    Icon(imageVector = Icons.Default.Textsms, modifier = Modifier.padding(horizontal = 8.dp), contentDescription = "SmsLogo")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToStartChat() }
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Agregar"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start chat")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(smsChats) { chat ->
                ChatItem(smsChat = chat, navigateToChat = navigateToChat)
            }
        }
    }
}

@Composable
fun ChatItem(smsChat: SmsChat, navigateToChat: (String) -> Unit) {

    Row(
        modifier = Modifier
            .clickable { navigateToChat(smsChat.sender) }
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "AccountRepresentation",
            modifier = Modifier
                .size(50.dp),
            tint = smsChat.accountLogoColor
        )
        Column(
            modifier = Modifier
                .padding(start = 5.dp)
                .fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = smsChat.contact, fontWeight = Bold)
            val messageBody =
                if (smsChat.content.length > 80) smsChat.content.take(80) + " ..." else smsChat.content
            Row {
                if (smsChat.type.toInt() == 2) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Sms Sent",
                        modifier = Modifier
                            .size(23.dp)
                            .padding(3.dp)
                    )
                }
                Text(text = messageBody, style = TextStyle())
            }
        }
        Text(
            text = FormatDate.formatDate(smsChat.updatedAt),
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 11.sp, textAlign = TextAlign.End)
        )
    }
}