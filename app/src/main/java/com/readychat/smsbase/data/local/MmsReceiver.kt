package com.readychat.smsbase.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.readychat.smsbase.domain.models.MmsMessageModel
import com.readychat.smsbase.domain.repositories.IChatDetailsRepository
import dagger.hilt.android.AndroidEntryPoint
import android.net.Uri
import com.readychat.smsbase.domain.models.MmsPartModel
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val CHANNEL_ID = "mms_channel"
private const val KEY_TEXT_REPLY = "key_text_reply"
private const val NOTIFICATION_ID = 1

@AndroidEntryPoint
class MmsReceiver : BroadcastReceiver() {

    companion object {
        var mmsListener: ((MmsMessageModel) -> Unit)? = null

        fun clearMmsListener() {
            mmsListener = null
        }
    }

    @Inject
    lateinit var chatDetailsRepository: IChatDetailsRepository

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("prueba", "=== INICIO DE RECEPCION DE MMS ===")
        Log.d("prueba", "Accion recibida: ${intent.action}")

        if (intent.action == Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION) {
            val mmsData = intent.getByteArrayExtra("data")
            if (mmsData != null) {
                try {
                    val mmsMessage = processMmsMessage(context, mmsData)
                    if (mmsMessage != null) {
                        Log.d("prueba", "=== MMS RECIBIDO EXITOSAMENTE ===")
                        Log.d("prueba", "ID del MMS: ${mmsMessage.mmsId}")
                        Log.d("prueba", "Asunto: ${mmsMessage.subject ?: "Sin asunto"}")
                        Log.d("prueba", "Fecha de recepcion: ${mmsMessage.timeStamp}")
                        
                        // Notificar al listener si existe
                        mmsListener?.invoke(mmsMessage)
                    }
                } catch (e: Exception) {
                    Log.e("prueba", "ERROR al procesar MMS: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                Log.e("prueba", "ERROR: No se encontraron datos en el MMS")
            }
        } else {
            Log.d("prueba", "Accion no reconocida: ${intent.action}")
        }
        Log.d("prueba", "=== FIN DE RECEPCIÓN DE MMS ===")
    }

    private fun processMmsMessage(context: Context, mmsData: ByteArray): MmsMessageModel? {
        try {
            // Obtener información del MMS
            val mmsUri = Uri.parse("content://mms")
            val mmsCursor = context.contentResolver.query(
                mmsUri,
                arrayOf(
                    Telephony.Mms._ID,
                    Telephony.Mms.DATE,
                    Telephony.Mms.READ,
                    Telephony.Mms.SUBJECT,
                    Telephony.Mms.MESSAGE_BOX,
                    Telephony.Mms.THREAD_ID
                ),
                null,
                null,
                Telephony.Mms.DATE + " DESC"
            )

            if (mmsCursor?.moveToFirst() == true) {
                val mmsId = mmsCursor.getLong(mmsCursor.getColumnIndexOrThrow(Telephony.Mms._ID))
                val date = mmsCursor.getLong(mmsCursor.getColumnIndexOrThrow(Telephony.Mms.DATE)) * 1000L
                val read = mmsCursor.getInt(mmsCursor.getColumnIndexOrThrow(Telephony.Mms.READ)) == 1
                val subject = mmsCursor.getString(mmsCursor.getColumnIndexOrThrow(Telephony.Mms.SUBJECT))
                val threadId = mmsCursor.getLong(mmsCursor.getColumnIndexOrThrow(Telephony.Mms.THREAD_ID))

                // Obtener información del remitente
                val sender = getMmsSender(context, mmsId)
                Log.d("prueba", "Remitente: $sender")

                // Obtener información de las partes del MMS
                val parts = getMmsParts(context, mmsId)
                Log.d("prueba", "Numero de partes: ${parts.size}")
                parts.forEach { part ->
                    when {
                        part.contentType.contains("text") -> {
                            Log.d("prueba", "Texto recibido: ${part.text}")
                        }
                        part.contentType.contains("image") -> {
                            Log.d("prueba", "Imagen recibida: ${part.filePath}")
                        }
                        part.contentType.contains("video") -> {
                            Log.d("prueba", "Video recibido: ${part.filePath}")
                        }
                        part.contentType.contains("audio") -> {
                            Log.d("prueba", "Audio recibido: ${part.filePath}")
                        }
                        else -> {
                            Log.d("prueba", "Otro tipo de contenido: ${part.contentType}")
                        }
                    }
                }

                return MmsMessageModel(
                    mmsId = mmsId,
                    chatId = threadId,
                    subject = subject,
                    timeStamp = date,
                    status = "received",
                    read = read,
                    messageBox = Telephony.Mms.MESSAGE_BOX_INBOX,
                    parts = parts
                )
            }
        } catch (e: Exception) {
            Log.e("prueba", "Error al procesar MMS: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    private fun getMmsSender(context: Context, mmsId: Long): String {
        var sender = "Unknown"
        val uri = Uri.parse("content://mms/$mmsId/addr")
        
        context.contentResolver.query(
            uri,
            arrayOf("address", "type"),
            "type=137 OR type=151",
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                sender = cursor.getString(0) ?: "Unknown"
            }
        }
        return sender
    }

    private fun getMmsParts(context: Context, mmsId: Long): List<MmsPartModel> {
        val parts = mutableListOf<MmsPartModel>()
        val uri = Uri.parse("content://mms/part")

        context.contentResolver.query(
            uri,
            arrayOf("_id", "ct", "_data", "text"),
            "mid=$mmsId",
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val partId = cursor.getLong(0)
                val contentType = cursor.getString(1) ?: ""
                val data = cursor.getString(2)
                var text = cursor.getString(3)

                if (text == null && data != null) {
                    text = getTextFromPartFile(context, partId)
                }

                var filePath: String? = null
                if (contentType.startsWith("image/") || contentType.startsWith("video/") || contentType.startsWith("audio/")) {
                    filePath = saveMmsMediaToFile(context, partId, contentType)
                }

                parts.add(MmsPartModel(
                    partId = partId,
                    mmsMessageId = mmsId.toInt(),
                    contentType = contentType,
                    text = text,
                    filePath = filePath
                ))
            }
        }
        return parts
    }

    private fun getTextFromPartFile(context: Context, partId: Long): String? {
        val partUri = Uri.parse("content://mms/part/$partId")
        return try {
            context.contentResolver.openInputStream(partUri)?.use { input ->
                input.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            Log.e("prueba", "Error al leer texto del MMS: ${e.message}")
            null
        }
    }

    private fun saveMmsMediaToFile(context: Context, partId: Long, contentType: String): String? {
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

                Log.d("prueba", "Archivo multimedia guardado: ${file.absolutePath}")
                file.absolutePath
            }
        } catch (e: Exception) {
            Log.e("prueba", "Error al guardar archivo multimedia: ${e.message}")
            null
        }
    }
}
