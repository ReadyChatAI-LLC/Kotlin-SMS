package com.readychat.smsbase.presentation.screens.contacts

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readychat.smsbase.data.local.repositories.LocalSmsRepository
import com.readychat.smsbase.domain.models.ChatDetailsModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val localSmsRepository: LocalSmsRepository
): ViewModel() {
    private val _contacts = mutableStateOf<List<ChatDetailsModel>>(emptyList())
    val contacts: State<List<ChatDetailsModel>> get() = _contacts

    private val _query = mutableStateOf("")
    val query: State<String> get() = _query

    fun getAllContacts(){
        viewModelScope.launch {
            _contacts.value = localSmsRepository.getAllContactsName()
        }
    }

    fun updateQuery(query: String){
        _query.value = query
    }
}