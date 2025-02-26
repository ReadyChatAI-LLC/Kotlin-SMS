package com.aireply.presentation.screens.startChat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.aireply.data.local.contentResolver.SmsContentResolver
import com.aireply.domain.models.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartChatViewModel @Inject constructor(
    private val smsContentResolver: SmsContentResolver
): ViewModel() {
    private val _contacts = mutableStateOf<List<Contact>>(emptyList())
    val contacts: State<List<Contact>> get() = _contacts

    private val _query = mutableStateOf("")
    val query: State<String> get() = _query

    fun getAllContacts(){
        _contacts.value = smsContentResolver.getAllContacts()
    }

    fun updateQuery(query: String){
        _query.value = query
    }
}