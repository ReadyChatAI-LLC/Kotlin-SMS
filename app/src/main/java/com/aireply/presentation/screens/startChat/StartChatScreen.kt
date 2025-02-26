package com.aireply.presentation.screens.startChat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aireply.data.local.contentResolver.SmsContentResolver
import com.aireply.domain.models.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartChatScreen (viewModel: StartChatViewModel = hiltViewModel(), onContactClick: (String) -> Unit) {

    val contacts by viewModel.contacts
    val query by viewModel.query
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.getAllContacts()
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Contactos") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Buscar contacto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            val filteredContacts = if (query.isBlank()) {
                contacts
            } else {
                contacts.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.number.contains(query, ignoreCase = true)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredContacts) { contact ->
                    ListItemContact(contact, onContactClick)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ListItemContact(contact: Contact, onContactClick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(5.dp).clickable { onContactClick(contact.number) }, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(contact.name)
        Text(contact.number)
    }
}