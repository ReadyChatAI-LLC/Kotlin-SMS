package com.readychat.smsbase.presentation.screens.chatProfile.components

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.presentation.screens.shared.ConfirmOperationDialog
import com.readychat.smsbase.presentation.screens.shared.ConfirmationModel
import com.readychat.smsbase.util.PhoneNumberInfo
import com.readychat.smsbase.util.PhoneNumberParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatProfileScreen(
    modifier: Modifier = Modifier,
    chatDetails: ChatDetailsModel,
    onDeleteChat: (String) -> Unit,
    onBlockChat: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val addressInfo = PhoneNumberParser.getPhoneNumberInfo(chatDetails.address)
    var showDialogToConfirmOperation by remember { mutableStateOf(false) }
    var confirmationModel by remember {
        mutableStateOf<ConfirmationModel?>(
            null
        )
    }

    if(showDialogToConfirmOperation){
        confirmationModel?.let { it ->
            ConfirmOperationDialog(it)
        }?: run {
            Toast.makeText(context, "No action was taken", Toast.LENGTH_SHORT).show()
            showDialogToConfirmOperation = false
            Log.e("prueba", "ChatListScreen. Se inicializo un objeto vacio que no debio inicializarse.")
        }
    }

    Column(modifier =modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "AccountRepresentation",
                modifier = Modifier
                    .size(65.dp),
                tint = chatDetails.accountLogoColor
            )
            Spacer(modifier = Modifier.width(13.dp))
            Column {
                Text(
                    text = chatDetails.contact,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    Text(
                        text = "(${addressInfo.region}) ",
                        fontSize = 15.sp
                    )

                    Text(
                        //text = contactInfo.internationalPhone,
                        text = PhoneNumberParser.getPhoneNumberInfo(chatDetails.address).internationalPhone,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${chatDetails.address}")
                }
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
            IconButton(onClick = {
                onBack()
            }) {
                Icon(Icons.Default.Message, contentDescription = "Message")
            }
            IconButton(onClick = {
                if(chatDetails.contactId.isNotEmpty()){
                    Log.i("prueba", "Debe ir a contactos para editar: ${chatDetails.address}")
                    val contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, chatDetails.contactId)
                    val editIntent = Intent(Intent.ACTION_EDIT).apply {
                        data = contactUri
                        putExtra("finishActivityOnSaveCompleted", true)
                    }
                    context.startActivity(editIntent)
                }else{
                    Log.i("prueba", "Debe ir a contactos para insertar: ${chatDetails.address}")
                    val insertIntent = Intent(Intent.ACTION_INSERT).apply {
                        data = ContactsContract.Contacts.CONTENT_URI
                        putExtra(ContactsContract.Intents.Insert.PHONE, chatDetails.address)
                    }
                    context.startActivity(insertIntent)
                }

            }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Archived Chat: ${chatDetails.isArchived}",
            color = Color.Gray
        )
    }

    DangerousButtons(if(chatDetails.isBlocked) "Unblock chat" else "Block Chat", Icons.Default.Block) {
        confirmationModel = ConfirmationModel(
            title = "Block Chat",
            description = "Are you sure you want to block this chat?",
            onConfirm = {onBlockChat(chatDetails.address)
                showDialogToConfirmOperation = false},
            onDismiss = {showDialogToConfirmOperation = false }
        )
        showDialogToConfirmOperation = true
    }

    Spacer(modifier = Modifier.height(10.dp))

    DangerousButtons("Delete Chat", Icons.Default.Delete) {
        confirmationModel = ConfirmationModel(
            title = "Delete Chat",
            description = "Are you sure you want to delete this chat?",
            onConfirm = {onDeleteChat(chatDetails.address)
                showDialogToConfirmOperation = false},
            onDismiss = {showDialogToConfirmOperation = false }
        )
        showDialogToConfirmOperation = true
    }
}

@Composable
fun DangerousButtons(buttonText: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
            Icon(icon, contentDescription = buttonText, modifier = Modifier.size(25.dp))
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = buttonText, fontSize = 18.sp)
        }
    }
}