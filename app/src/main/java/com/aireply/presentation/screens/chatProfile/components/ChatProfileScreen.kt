import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Archive
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.domain.models.ChatDetailsModel
import com.aireply.presentation.screens.chatProfile.ChatProfileViewModel
import com.aireply.presentation.screens.shared.ChatDetailsState
import com.aireply.presentation.screens.shared.ErrorScreen
import com.aireply.presentation.screens.shared.ShimmerEffect
import com.aireply.util.PhoneNumberParser
import javax.annotation.meta.When

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatProfileScreen(
    viewModel: ChatProfileViewModel = hiltViewModel(),
    address: String,
    onBack: () -> Unit
) {

    val chatDetailsState by viewModel.uiState

    val context = LocalContext.current

    LaunchedEffect(Unit) {
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
                    val chatDetails = state.chatDetails
                    Column {
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
                                Text(
                                    text = chatDetails.address,
                                    fontSize = 15.sp
                                )
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
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        /*Text(
                            text = "Country: ${phoneNumberInfo.country ?: "Unknown"}",
                        )*/

                        Text(
                            text = "Archived Chat: ${chatDetails.archivedChat}",
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { viewModel.deleteConversation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                    ) {
                        Text(text = "Delete chat", color = MaterialTheme.colorScheme.surface, fontSize = 18.sp)
                    }
                }

                is ChatDetailsState.Error -> {
                    ErrorScreen(
                        titleTopBar = address,
                        errorMessage = state.message,
                        onRetry = {},
                        onBack = { onBack() })
                }
            }

        }
    }
}