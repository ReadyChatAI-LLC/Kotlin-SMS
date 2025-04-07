package com.readychat.smsbase.presentation.screens.contacts

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    onBack: () -> Unit,
    onCreateGroup: (String, List<String>) -> Unit
) {
    var isGroupMode by remember { mutableStateOf(false) }
    val selectedContacts = remember { mutableStateMapOf<String, Boolean>() }
    var showGroupNameDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }

    val contacts by viewModel.contacts
    val query by viewModel.query
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

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
        },
        floatingActionButton = {
            val selectedCount = selectedContacts.count { it.value }
            if (isGroupMode && selectedCount >= 2) {
                FloatingActionButton(
                    onClick = { showGroupNameDialog = true }
                ) {
                    Text(
                        text = "Next",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
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
                placeholder = { Text("Search by name or number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { isGroupMode = !isGroupMode }
                    .padding(vertical = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = "Create group",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Create group",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                )
            }

            val filteredContacts = if (query.isBlank()) {
                contacts
            } else {
                contacts.filter {
                    it.contact.contains(query, ignoreCase = true) ||
                            it.address.contains(query, ignoreCase = true)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredContacts) { contact ->
                    ContactItem(
                        contact = contact,
                        onContactClick = onContactClick,
                        showCheckbox = isGroupMode,
                        isChecked = selectedContacts[contact.address] ?: false,
                        onCheckedChange = { isChecked ->
                            selectedContacts[contact.address] = isChecked
                        }
                    )
                }
            }

            if (showGroupNameDialog) {
                AlertDialog(
                    onDismissRequest = { showGroupNameDialog = false },
                    confirmButton = {
                        Text(
                            text = "Create",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    if (groupName.isNotBlank()) {
                                        val selectedAddresses = contacts
                                            .filter { selectedContacts[it.address] == true }
                                            .map { it.address }

                                        showGroupNameDialog = false
                                        onCreateGroup(groupName, selectedAddresses)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please enter a group name",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        )
                    },
                    dismissButton = {
                        Text(
                            text = "Cancel",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { showGroupNameDialog = false }
                        )
                    },
                    title = { Text("Group name") },
                    text = {
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            placeholder = { Text("Enter group name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: ChatDetailsModel,
    onContactClick: (String) -> Unit,
    showCheckbox: Boolean = false,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val rowModifier = if (!showCheckbox) {
        Modifier
            .clickable { onContactClick(contact.address) }
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (showCheckbox) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Contact",
            modifier = Modifier.size(50.dp),
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
