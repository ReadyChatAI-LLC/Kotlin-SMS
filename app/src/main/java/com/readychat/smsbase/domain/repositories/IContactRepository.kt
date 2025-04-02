package com.readychat.smsbase.domain.repositories

import com.readychat.smsbase.domain.models.ChatDetailsModel

interface IContactRepository {
    suspend fun getContactByAddress(address: String): String
    suspend fun getAllContacts(): List<ChatDetailsModel>
    suspend fun searchContact(searchText: String): List<ChatDetailsModel>
    suspend fun loadContactsToRoom()
}