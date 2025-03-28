package com.readychat.smsbase.data.local.repositories

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import com.readychat.smsbase.data.local.room.dao.ChatDetailsDao
import com.readychat.smsbase.data.local.room.entities.ChatDetailsEntity
import com.readychat.smsbase.util.Converters
import com.readychat.smsbase.util.PhoneNumberParser
import com.readychat.smsbase.util.RandomColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val context: Context,
    private val chatDetailsDao: ChatDetailsDao
) {
    private val _contactsChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val contactsChanges: SharedFlow<Unit> = _contactsChanges

    private val contactsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            _contactsChanges.tryEmit(Unit)
        }
    }

    init {
        registerObserver()
    }

    private fun registerObserver() {
        context.contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            contactsObserver
        )
    }

    /*suspend fun syncContacts() {
        val currentContacts = getAllDeviceContacts()
        val currentIds = currentContacts.map { it.contactId }

        withContext(Dispatchers.IO) {
            chatDetailsDao.insertContacts(currentContacts)
            chatDetailsDao.deleteMissingContacts(currentIds)
        }
    }*/

    fun getAllDeviceContacts(): List<ChatDetailsEntity> {
        val contactsMap = mutableMapOf<String, ChatDetailsEntity>()
        val contentResolver = context.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val contact = it.getString(nameIndex) ?: "Unknown"
                val address = it.getString(numberIndex) ?: "Unknown"
                if (!contactsMap.containsKey(id)) {
                    contactsMap[id] = ChatDetailsEntity(
                        address = PhoneNumberParser.normalizePhoneNumber(address),
                        contact = contact,
                        accountLogoColor = Converters.fromColor(RandomColor.randomColor()),
                        archivedChat = false,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            }
        }
        return contactsMap.values.toList().sortedBy { it.contact }
    }

    /*fun getContactsFlow(): Flow<List<ChatDetailsEntity>> {
        return chatDetailsDao.getAllContactsFlow()
    }*/
}