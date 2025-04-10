package com.readychat.smsbase.presentation.screens.contacts

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.repositories.IContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepo: IContactRepository
) : ViewModel() {

    private val _contacts = mutableStateOf<List<ChatDetailsModel>>(emptyList())
    val contacts: State<List<ChatDetailsModel>> get() = _contacts

    private val _query = mutableStateOf("")
    val query: State<String> get() = _query

    fun getAllContacts() {
        viewModelScope.launch {
            _contacts.value = contactRepo.getAllContacts()
        }
    }

    fun updateQuery(query: String) {
        _query.value = query
    }
}
