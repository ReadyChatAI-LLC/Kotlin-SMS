package com.readychat.smsbase.presentation.screens.chatDetailsGroup.components


import android.net.Uri
import androidx.compose.runtime.Composable
import com.readychat.smsbase.presentation.screens.chatDetails.components.MessageInput

@Composable
fun GroupMessageInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: (Uri?) -> Unit,
    onEmojiClicked: () -> Unit,
    selectedImageUri: Uri? = null,
    onImageSelected: (Uri) -> Unit,
    onDeleteImage: () -> Unit
) {
    MessageInput(
        messageText = messageText,
        onMessageChange = onMessageChange,
        onSendMessage = onSendMessage,
        onEmojiClicked = onEmojiClicked,
        selectedImageUri = selectedImageUri,
        onImageSelected = onImageSelected,
        onDeleteImage = onDeleteImage
    )
}
