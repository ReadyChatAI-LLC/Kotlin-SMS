package com.aireply.data.local.contentResolver

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.aireply.domain.models.Chat
import com.aireply.domain.models.Contact
import com.aireply.domain.models.SmsMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsContentResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val contactCache = mutableMapOf<String, String?>()

    private suspend fun getSmsMessages(): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()
        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("address", "body", "date", "type"),
            null,
            null,
            "date DESC"
        )

        Log.d("prueba", "getSmsMessages - SmsContentResolver")

        cursor?.use {
            while (it.moveToNext()) {
                val address = it.getString(0)
                val body = it.getString(1)
                val date = it.getLong(2)

                val senderName = getContactName(address) ?: address

                messages.add(SmsMessage(address, body, date, senderName))
            }
        }
        messages
    }

    private fun getContactName(phoneNumber: String): String? {
        if (contactCache.containsKey(phoneNumber)) {
            return contactCache[phoneNumber]
        }

        Log.d("prueba", "getContactName - Consultando contacto para: $phoneNumber")
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0)
                contactCache[phoneNumber] = name
                return name
            }
        }
        contactCache[phoneNumber] = null
        return null
    }

    suspend fun groupMessagesByContact(): List<Chat> = withContext(Dispatchers.IO) {
        Log.d("prueba", "groupMessagesByContact - SmsContentResolver")
        getSmsMessages()
            .groupBy { it.senderName }
            .map { (contact, smsList) ->
                val latestMessage = smsList.maxByOrNull { it.date }!!
                Chat(
                    contact = contact,
                    lastMessage = latestMessage,
                    messages = smsList
                )
            }
            .sortedByDescending { it.lastMessage.date }
    }

    fun getAllContacts(): List<Contact> {
        val contactsMap = mutableMapOf<String, Contact>()
        val contentResolver = context.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Sin nombre"
                val number = it.getString(numberIndex) ?: ""
                if (!contactsMap.containsKey(id)) {
                    contactsMap[id] = Contact(id, name, number)
                }
            }
        }
        return contactsMap.values.toList().sortedBy { it.name }
    }

    fun getSmsByPhoneNumber(phoneNumber: String): List<SmsMessage> {
        val smsList = mutableListOf<SmsMessage>()
        val uriSms = Uri.parse("content://sms")
        val projection = arrayOf("address", "date", "body", "type")
        val selection = "address = ?"
        val selectionArgs = arrayOf(phoneNumber)

        val cursor = context.contentResolver.query(uriSms, projection, selection, selectionArgs, "date DESC")
        cursor?.use {
            val addressIndex = it.getColumnIndex("address")
            val dateIndex = it.getColumnIndex("date")
            val bodyIndex = it.getColumnIndex("body")
            val typeIndex = it.getColumnIndex("type")

            while (it.moveToNext()) {
                val address = it.getString(addressIndex)
                val date = it.getLong(dateIndex)
                val body = it.getString(bodyIndex)
                val type = it.getInt(typeIndex)

                //smsList.add(SmsMessage(address, date, body, type))
            }
        }

        return smsList
    }

}