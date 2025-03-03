package com.aireply.data.local.contentResolver

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import com.aireply.domain.models.ChatDetailsModel
import com.aireply.domain.models.SmsChat
import com.aireply.domain.models.Contact
import com.aireply.util.PhoneNumberParser
import com.aireply.util.RandomColor
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

    private val contactsCache = mutableListOf<String>()

    private fun getContactName(phoneNumber: String): String? {
        if (contactCache.containsKey(phoneNumber)) {
            return contactCache[phoneNumber]
        }

        Log.d("prueba", "getContactName - Consultando contacto para: $phoneNumber")
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)
        )

        context.contentResolver.query(
            uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
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

    suspend fun getSmsChats(): List<SmsChat> = withContext(Dispatchers.IO) {
        val smsChats = mutableListOf<SmsChat>()

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.STATUS,
            Telephony.Sms.TYPE,
            Telephony.Sms.THREAD_ID
        )

        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI, projection, null, null, Telephony.Sms.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
            val statusIndex = it.getColumnIndex(Telephony.Sms.STATUS)

            while (it.moveToNext()) {
                if (!contactsCache.contains(it.getString(addressIndex))) {
                    val address = it.getString(addressIndex).takeIf { address -> !address.isNullOrEmpty() } ?: "Unknown"
                    val body = it.getString(bodyIndex)
                    val date = it.getString(dateIndex)
                    val type = it.getString(typeIndex)
                    val status = it.getString(statusIndex)

                    Log.d(
                        "prueba",
                        "address: $address, body: $body, date: $date, type: $type, status: $status"
                    )

                    contactsCache.add(address)

                    // address = MethodChannelProvider.formatPhoneNumberWithCountryCode(address)
                    // Fetch contact name if the address is a valid phone number
                    // val contactName = if (isPhoneNumber(address)) getContactName(address)!! else address

                    val message = SmsChat(
                        id = 0,
                        sender = address,
                        content = body,
                        timeStamp = date.toLong(),
                        status = status.toString(),
                        type = type,
                        contact = getContactName(address)?: address,
                        updatedAt = date.toLong(),
                        accountLogoColor = RandomColor.randomColor()
                    )
                    smsChats.add(message)
                }
            }
        }
        Log.d("prueba", "5")

        smsChats
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
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, sortOrder
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
                    contactsMap[id] = Contact(id, name, number, RandomColor.randomColor())
                }
            }
        }
        return contactsMap.values.toList().sortedBy { it.name }
    }

    suspend fun getSmsChatByNumber(phoneNumberLong: String): ChatDetailsModel = withContext(Dispatchers.IO) {
        val phoneNumber = PhoneNumberParser.phoneNumberParser(phoneNumberLong)

        val smsChats = mutableListOf<SmsChat>()

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.STATUS,
            Telephony.Sms.TYPE,
            Telephony.Sms.THREAD_ID
        )

        val selection = "${Telephony.Sms.ADDRESS} = ?"
        val selectionArgs = arrayOf(phoneNumber)

        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            Telephony.Sms.DEFAULT_SORT_ORDER
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)
            val statusIndex = it.getColumnIndex(Telephony.Sms.STATUS)

            while (it.moveToNext()) {
                val address = it.getString(addressIndex).takeIf {address -> !address.isNullOrEmpty() } ?: "Unknown"
                val body = it.getString(bodyIndex)
                val date = it.getString(dateIndex)
                val type = it.getString(typeIndex)
                val status = it.getString(statusIndex)

                val contactName = getContactName(address) ?: address

                val message = SmsChat(
                    id = 0,
                    sender = address,
                    content = body,
                    timeStamp = date.toLong(),
                    status = status.toString(),
                    type = type,
                    contact = contactName,
                    updatedAt = date.toLong(),
                    accountLogoColor = RandomColor.randomColor()
                )
                smsChats.add(message)
            }
        }
        val sortedSmsChats = smsChats.sortedBy { it.timeStamp }
        ChatDetailsModel(phoneNumber, getContactName(phoneNumber)?:phoneNumber, sortedSmsChats.toMutableList())
    }


}