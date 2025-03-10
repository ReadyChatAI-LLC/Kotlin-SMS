package com.aireply.presentation.screens.contacts

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aireply.data.local.contentResolver.SmsContentResolver
import com.aireply.data.local.repositories.LocalSmsRepository
import com.aireply.domain.models.ChatDetailsModel
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