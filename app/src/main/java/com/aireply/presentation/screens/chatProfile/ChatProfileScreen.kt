import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aireply.domain.models.ChatDetailsModel
import com.aireply.util.PhoneNumberParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatProfileScreen(
    chatDetails: ChatDetailsModel,
    onBack: () -> Unit,
    onDeleteConversation: () -> Unit
) {

    val phoneNumberInfo = remember { PhoneNumberParser.getPhoneNumberInfo(chatDetails.address) }

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
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(chatDetails.accountLogoColor)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = chatDetails.contact
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Country: ${phoneNumberInfo.country ?: "Unknown"}",
                )
                Text(
                    text = "Phone Number: ${phoneNumberInfo.phoneNumber}",
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (chatDetails.archivedChat) {
                    Text(
                        text = "Estado del chat: Archivado",
                        color = Color.Gray
                    )
                }
            }

            Button(
                onClick = { onDeleteConversation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
            ) {
                Text(text = "Delete chat", color = MaterialTheme.colorScheme.surface, fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatProfileScreenPreview() {
    ChatProfileScreen(
        chatDetails = ChatDetailsModel(
            id = 0,
            address = "Unknown",
            contact = "Unknown",
            accountLogoColor = Color.Gray,
            archivedChat = false,
            updatedAt = System.currentTimeMillis(),
            chatList = mutableListOf()
        ),
        onBack = {},
        onDeleteConversation = { /* Acción de eliminar */ }
    )
}
