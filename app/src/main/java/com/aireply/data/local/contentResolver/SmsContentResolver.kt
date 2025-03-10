package com.aireply.data.local.contentResolver

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import com.aireply.data.local.room.entity.ChatDetailsEntity
import com.aireply.domain.models.ChatDetailsModel
import com.aireply.domain.models.ChatSummaryModel
import com.aireply.domain.models.MessageModel
import com.aireply.domain.models.toMessageEntity
import com.aireply.util.Converters
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

    private inline fun <T> queryContentResolver(
        uri: Uri,
        projection: Array<String>,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null,
        block: (Cursor) -> T
    ): T? {
        return context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            ?.use(block)
    }

    private fun getContactName(phoneNumber: String): String? {
        contactCache[phoneNumber]?.let { return it }
        Log.d("prueba", "getContactName - Consultando nombre de contacto para: $phoneNumber")

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)
        )

        return queryContentResolver(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        ) { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0)
                contactCache[phoneNumber] = name
                name
            } else {
                contactCache[phoneNumber] = null
                null
            }
        }
    }

    suspend fun getChatsSummaryContentResolver(): List<ChatSummaryModel> {
        val list = querySmsChats(isFetchingSummaryChats = true)
        val chats = list.filterIsInstance<ChatSummaryModel>()

        return chats
    }

    suspend fun getChatDetailsByNumber(longAddress: String): ChatDetailsModel =
        withContext(Dispatchers.IO) {
            val address = PhoneNumberParser.phoneNumberParser(longAddress)
            Log.d("prueba", "Buscando mensajes de $longAddress -> $address. getChatDetailsByNumber de SmsContentResolver")
            val list = querySmsChats(
                selection = "${Telephony.Sms.ADDRESS} = ?",
                selectionArgs = arrayOf(address),
                isFetchingSummaryChats = false
            )

            val chats = list.filterIsInstance<MessageModel>()

            val sortedSmsChats = chats.sortedBy { it.timeStamp }

            ChatDetailsModel(
                address = address,
                contact = getContactName(address)?: address,
                accountLogoColor = RandomColor.randomColor(),
                updatedAt = System.currentTimeMillis(),
                archivedChat = false,
                chatList = sortedSmsChats.toMutableList()
            )
        }

    private suspend fun querySmsChats(
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        isFetchingSummaryChats: Boolean
    ): List<Any> = withContext(Dispatchers.IO) {
        val chatDetails = mutableListOf<MessageModel>()
        val chatSummaries = mutableListOf<ChatSummaryModel>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.STATUS,
            Telephony.Sms.TYPE,
            Telephony.Sms.THREAD_ID
        )

        queryContentResolver(
            uri = Telephony.Sms.CONTENT_URI,
            projection = projection,
            selection = selection,
            selectionArgs = selectionArgs,
            sortOrder = Telephony.Sms.DEFAULT_SORT_ORDER
        ) { cursor ->
            val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE)
            val statusIndex = cursor.getColumnIndex(Telephony.Sms.STATUS)

            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex).takeIf { it.isNotEmpty() } ?: "Unknown"
                val body = cursor.getString(bodyIndex)
                val date = cursor.getString(dateIndex)
                val type = cursor.getString(typeIndex)
                val status = cursor.getString(statusIndex)

                if (isFetchingSummaryChats) {
                    if (!contactsCache.contains(cursor.getString(addressIndex))) {
                        contactsCache.add(address)

                        chatSummaries.add(
                            ChatSummaryModel(
                                id = 0,
                                address = PhoneNumberParser.normalizePhoneNumber(address),
                                content = body,
                                timeStamp = date.toLong(),
                                status = status,
                                type = type,
                                contact = getContactName(address)?: PhoneNumberParser.normalizePhoneNumber(address),
                                updatedAt = date.toLong(),
                                archivedChat = false,
                                accountLogoColor = RandomColor.randomColor()
                            )
                        )
                    }
                } else {
                    chatDetails.add(
                        MessageModel(
                            messageId = 0,
                            chatId = 0,
                            content = body,
                            timeStamp = date.toLong(),
                            status = status,
                            type = type
                        )
                    )
                }
            }

        }
        if (isFetchingSummaryChats) chatSummaries else chatDetails
    }

    fun getAllContacts(): List<ChatDetailsEntity> {
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
}