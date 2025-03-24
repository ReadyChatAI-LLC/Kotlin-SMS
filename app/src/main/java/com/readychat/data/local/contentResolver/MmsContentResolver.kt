package com.readychat.data.local.contentResolver

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.Telephony
import com.readychat.data.local.room.dao.MmsMessageDao
import com.readychat.data.local.room.dao.MmsPartDao
import com.readychat.data.local.room.database.AppDatabase
import com.readychat.data.local.room.entities.ChatDetailsEntity
import com.readychat.data.local.room.entities.MmsMessageEntity
import com.readychat.data.local.room.entities.MmsPartEntity
import com.readychat.util.Converters
import com.readychat.util.RandomColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class MmsContentResolver @Inject constructor(
    private val context: Context,
    private val mmsMessageDao: MmsMessageDao,
    private val mmsPartDao: MmsPartDao
) {

    suspend fun syncMmsMessages(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val mmsMessages = getMmsMessagesFromProvider()
                var insertedCount = 0

                mmsMessages.forEach { (mmsMessage, mmsParts) ->
                    val existingMessage = mmsMessageDao.getMmsMessageByMmsId(mmsMessage.mmsId)
                    if (existingMessage == null) {
                        val mmsMessageId = mmsMessageDao.insertMmsMessage(mmsMessage).toInt()

                        mmsParts.forEach { part ->
                            val updatedPart = part.copy(mmsMessageId = mmsMessageId)
                            mmsPartDao.insertMmsPart(updatedPart)
                        }

                        insertedCount++
                    }
                }

                Result.success(insertedCount)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private fun getMmsMessagesFromProvider(): List<Pair<MmsMessageEntity, List<MmsPartEntity>>> {
        val result = mutableListOf<Pair<MmsMessageEntity, List<MmsPartEntity>>>()

        val uri = Telephony.Mms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Mms._ID,
            Telephony.Mms.DATE,
            Telephony.Mms.READ,
            Telephony.Mms.SUBJECT,
            Telephony.Mms.MESSAGE_BOX,
            Telephony.Mms.THREAD_ID
        )

        context.contentResolver.query(
            uri, projection, null, null, Telephony.Mms.DATE + " DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val mmsId = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Mms._ID))
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)) * 1000L
                val read = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Mms.READ)) == 1
                val subject = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.SUBJECT))
                val messageBox = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_BOX))
                val threadId = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID))

                val address = getMmsAddress(mmsId)

                val chatId = 1L

                if (chatId != -1L) {
                    val mmsMessage = MmsMessageEntity(
                        mmsId = mmsId,
                        chatId = chatId,
                        subject = subject,
                        timeStamp = date,
                        status = getStatusFromMessageBox(messageBox),
                        read = read,
                        messageBox = messageBox
                    )

                    val mmsParts = getMmsParts(mmsId)

                    result.add(Pair(mmsMessage, mmsParts))
                }
            }
        }

        return result
    }

    private fun getMmsAddress(mmsId: Long): String {
        var address = ""
        val uri = Uri.parse("content://mms/$mmsId/addr")

        context.contentResolver.query(
            uri,
            arrayOf("address", "type"),
            "type=137 OR type=151", null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                address = cursor.getString(0) ?: ""
            }
        }

        return address
    }

    private fun getMmsParts(mmsId: Long): List<MmsPartEntity> {
        val parts = mutableListOf<MmsPartEntity>()
        val uri = Uri.parse("content://mms/part")

        context.contentResolver.query(
            uri,
            arrayOf("_id", "ct", "_data", "text"),
            "mid=$mmsId", null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val partId = cursor.getLong(0)
                val contentType = cursor.getString(1) ?: ""
                val data = cursor.getString(2)
                var text = cursor.getString(3)

                if (text == null && data != null) {
                    text = getTextFromPartFile(partId)
                }

                var filePath: String? = null
                if (contentType.startsWith("image/") || contentType.startsWith("video/") || contentType.startsWith("audio/")) {
                    filePath = saveMmsMediaToFile(partId, contentType)
                }

                parts.add(MmsPartEntity(
                    partId = partId,
                    mmsMessageId = 0,
                    contentType = contentType,
                    text = text,
                    filePath = filePath
                ))
            }
        }

        return parts
    }

    private fun getTextFromPartFile(partId: Long): String? {
        val partUri = Uri.parse("content://mms/part/$partId")
        return try {
            context.contentResolver.openInputStream(partUri)?.use { input ->
                input.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveMmsMediaToFile(partId: Long, contentType: String): String? {
        val partUri = Uri.parse("content://mms/part/$partId")
        return try {
            context.contentResolver.openInputStream(partUri)?.use { input ->
                val extension = when {
                    contentType.contains("jpeg") || contentType.contains("jpg") -> ".jpg"
                    contentType.contains("png") -> ".png"
                    contentType.contains("gif") -> ".gif"
                    contentType.contains("video") -> ".mp4"
                    contentType.contains("audio") -> ".mp3"
                    else -> ".bin"
                }

                val fileName = "mms_${partId}_${System.currentTimeMillis()}$extension"
                val file = File(context.getExternalFilesDir(null), fileName)

                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }

                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getContactNameFromNumber(phoneNumber: String): String? {
        return null
    }

    private fun getStatusFromMessageBox(messageBox: Int): String {
        return when (messageBox) {
            Telephony.Mms.MESSAGE_BOX_INBOX -> "received"
            Telephony.Mms.MESSAGE_BOX_SENT -> "sent"
            Telephony.Mms.MESSAGE_BOX_DRAFTS -> "draft"
            Telephony.Mms.MESSAGE_BOX_OUTBOX -> "sending"
            else -> "unknown"
        }
    }

    suspend fun sendMms(chatId: Long, subject: String?, text: String?, imageUri: Uri?): Result<Boolean> {
        return Result.success(true)
    }
}