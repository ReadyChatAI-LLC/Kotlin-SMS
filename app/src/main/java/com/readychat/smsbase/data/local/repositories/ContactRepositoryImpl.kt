package com.readychat.smsbase.data.local.repositories

import com.readychat.smsbase.data.local.contentResolver.SmsContentResolver
import com.readychat.smsbase.data.local.room.dao.ChatDetailsDao
import com.readychat.smsbase.data.local.room.entities.toDomain
import com.readychat.smsbase.domain.models.ChatDetailsModel
import com.readychat.smsbase.domain.repositories.IContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val smsContentResolver: SmsContentResolver,
    private val chatDetailsDao: ChatDetailsDao
) : IContactRepository {

    override suspend fun getContactByAddress(address: String): String =
        withContext(Dispatchers.IO) {
            smsContentResolver.getContactName(address) ?: address
        }

    override suspend fun getAllContacts(): List<ChatDetailsModel> {
        return withContext(Dispatchers.IO) {
            chatDetailsDao.getAllContacts().map { it.toDomain() }
        }
    }

    override suspend fun searchContact(searchText: String): List<ChatDetailsModel> {
        return withContext(Dispatchers.IO) {
            chatDetailsDao.searchContacts(searchText).map { it.toDomain() }
        }
    }

    override suspend fun loadContactsToRoom() {
        withContext(Dispatchers.IO) {
            val contacts = smsContentResolver.getAllContacts()
            chatDetailsDao.insertContacts(contacts)
        }
    }
}
