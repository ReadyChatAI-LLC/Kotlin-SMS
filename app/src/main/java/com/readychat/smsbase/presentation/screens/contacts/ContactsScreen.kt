package com.readychat.smsbase.presentation.screens.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.readychat.smsbase.domain.models.ChatDetailsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    onContactClick: (String) -> Unit,
    onBack: () -> Unit
) {

    val contacts by viewModel.contacts
    val query by viewModel.query
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.getAllContacts()
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
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
                    .focusRequester(focusRequester),
                placeholder = { Text("Type names or phone numbers") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            val filteredContacts = if (query.isBlank()) {
                contacts
            } else {
                contacts.filter {
                    it.contact.contains(query, ignoreCase = true) ||
                            it.address.contains(query, ignoreCase = true)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (query.isNotEmpty() && query.all { it.isDigit() }) {
                    item {
                        StartConversationWithUnknown(query) {
                            onContactClick(query)
                        }
                    }
                }
                items(filteredContacts) { contact ->
                    ContactItem(contact, onContactClick)
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: ChatDetailsModel, onContactClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onContactClick(contact.address) }
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "AccountRepresentation",
            modifier = Modifier
                .size(50.dp),
            tint = contact.accountLogoColor
        )
        Column(
            modifier = Modifier
                .padding(start = 5.dp)
                .fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = contact.contact, fontWeight = Bold)
            Text(text = contact.address, style = TextStyle())
        }
    }
}

@Composable
fun StartConversationWithUnknown(unknownNumber: String, onStartConversationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartConversationClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "AccountRepresentation",
            modifier = Modifier
                .size(50.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 5.dp)
                .fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Send to $unknownNumber", fontWeight = Bold)
            Text(text = unknownNumber, style = TextStyle())
        }
    }
}