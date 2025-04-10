package com.readychat.smsbase.presentation.screens.chatDetails.components

import android.net.Uri
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.emoji2.emojipicker.EmojiPickerView
import coil.compose.rememberAsyncImagePainter
import com.readychat.smsbase.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageInput(
    messageText: String,
    showEmojiPicker: Boolean,
    updateShowEmojiPicker: (Boolean) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    selectedImageUri: Uri? = null,
    onSelectImageUri: (Uri) -> Unit,
    onDeleteImage: () -> Unit
) {
    val context = LocalContext.current
    var showImageOptions by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(messageText, TextRange(messageText.length))) }

    LaunchedEffect(messageText) {
        if (messageText != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                messageText,
                TextRange(messageText.length)
            )
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { onSelectImageUri(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onSelectImageUri(it) }
    }

    fun createTempImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir("Pictures")
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (selectedImageUri != null) {
            SelectedImagePreview(selectedImageUri =  selectedImageUri, onDeleteImage = onDeleteImage)
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 7.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = { showImageOptions = !showImageOptions },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp)
                    )
                }

                DropdownMenu(
                    expanded = showImageOptions,
                    onDismissRequest = { showImageOptions = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tomar foto") },
                        onClick = {
                            val file = createTempImageFile()
                            tempUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            cameraLauncher.launch(tempUri!!)
                            showImageOptions = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = "Camera"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Elegir de galería") },
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showImageOptions = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = "Gallery"
                            )
                        }
                    )
                }
            }

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onMessageChange(newValue.text)
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "Text message",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                },
                singleLine = true
            )

            IconButton(
                onClick = {
                    val newEmojiPickerState = !showEmojiPicker
                    updateShowEmojiPicker(newEmojiPickerState)

                    if (newEmojiPickerState) {
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEmotions,
                    contentDescription = "Show emojis",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    if (textFieldValue.text.trim().isNotEmpty() || selectedImageUri != null) {
                        onSendMessage()
                        textFieldValue = TextFieldValue("")
                        onMessageChange("")
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send message",
                    tint = if (textFieldValue.text.trim().isNotEmpty() || selectedImageUri != null)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = showEmojiPicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            EmojiPickerView(
                onEmojiSelected = { emoji ->
                    val cursorPosition = textFieldValue.selection.start
                    val newText = textFieldValue.text.substring(0, cursorPosition) + emoji +
                            textFieldValue.text.substring(cursorPosition)

                    val newCursorPosition = cursorPosition + emoji.length
                    textFieldValue = TextFieldValue(
                        text = newText,
                        selection = TextRange(newCursorPosition)
                    )

                    onMessageChange(newText)
                    Log.d("prueba", "Emoji inserted at pos $cursorPosition: $emoji, New text: $newText")
                }
            )
        }
    }
}

@Composable
fun EmojiPickerView(
    modifier: Modifier = Modifier,
    onEmojiSelected: (String) -> Unit
) {
    AndroidView(
        modifier = modifier.height(250.dp),
        factory = { ctx ->
            val emojiView = EmojiPickerView(ctx).apply {
                setOnEmojiPickedListener { emojiViewItem ->
                    Log.d("prueba", "Emoji Seleccionado: ${emojiViewItem.emoji}")
                    onEmojiSelected(emojiViewItem.emoji)
                }
            }
            emojiView
        }
    )
}