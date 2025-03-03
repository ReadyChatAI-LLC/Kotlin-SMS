package com.aireply.presentation.screens.chatDetails.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aireply.domain.models.ChatDetailsModel
import com.aireply.domain.models.SmsChat
import com.aireply.util.FormatDate
import java.time.LocalTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsScreen(messageText: String, messages: ChatDetailsModel, onMessageTextChange: (String) -> Unit, onBack: () -> Unit) {

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(messages.contact) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MessagesList(modifier = Modifier.weight(1f), messages.chatList)

            MessageInput(
                messageText = messageText,
                onMessageChange = { onMessageTextChange(it) },
                onSend = {
                    if (messageText.isNotBlank()) {
                        messages.chatList.add(
                            SmsChat(
                                id = messages.chatList.size + 1,
                                sender = "ME",
                                content = messageText.trim(),
                                timeStamp = System.currentTimeMillis(),
                                status = "IDK",
                                type = "2",
                                contact = "ME",
                                updatedAt = System.currentTimeMillis(),
                                accountLogoColor = Color.LightGray
                            )
                        )
                        onMessageTextChange("")
                    }
                }
            )
        }
    }
}

@Composable
fun MessagesList(modifier: Modifier = Modifier, messages: List<SmsChat>) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        reverseLayout = true
    ) {
        items(messages.reversed()) { message ->
            MessageBubble(message = message)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MessageBubble(message: SmsChat) {
    val isFromMe = message.type.toInt() == 2
    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isFromMe) Color(0xFF85929e) else MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (isFromMe) Color.Black else Color.White
    val textAlign = if(isFromMe) TextAlign.End else TextAlign.Start

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .wrapContentWidth()
                .background(bubbleColor)
                .padding(8.dp),
        ) {
            Text(
                text = message.content.trim(),
                color = textColor,
                textAlign = textAlign
            )
            Text(
                text = FormatDate.formatDateTime(message.timeStamp),
                color = textColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                textAlign = textAlign
            )
        }
    }
}

@Composable
fun MessageInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Write a sms message...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        IconButton(onClick = {
            onSend()
        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}