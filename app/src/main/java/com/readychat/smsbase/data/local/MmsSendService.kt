package com.readychat.smsbase.data.local

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import java.io.File
import android.content.ContentValues
import java.util.concurrent.atomic.AtomicInteger

class MmsSendService : Service() {

    companion object {
        private const val TAG = "prueba"
        const val ACTION_MMS_SENT = "com.readychat.smsbase.MMS_SENT"
        const val ACTION_MMS_DELIVERED = "com.readychat.smsbase.MMS_DELIVERED"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_MESSAGE_ID = "message_id"
    }

    private val messageIdCounter = AtomicInteger(0)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phoneNumber")
        val message = intent?.getStringExtra("message")
        val imageUriString = intent?.getStringExtra("imageUri")

        if (!phoneNumber.isNullOrEmpty()) {
            try {
                val messageId = messageIdCounter.incrementAndGet()

                registerResultReceivers(messageId)

                val imageUri = if (!imageUriString.isNullOrEmpty()) {
                    Uri.parse(imageUriString)
                } else {
                    null
                }

                if (imageUri != null) {
                    sendMms(phoneNumber, message ?: "", imageUri, messageId)
                }

                saveSentMessage(phoneNumber, message, imageUriString)

            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar mensaje: ${e.message}")
                sendBroadcast(Intent(ACTION_MMS_SENT).apply {
                    putExtra(EXTRA_RESULT_CODE, SmsManager.RESULT_ERROR_GENERIC_FAILURE)
                })
            }
        }

        return START_NOT_STICKY
    }

    private fun registerResultReceivers(messageId: Int) {
        val sentIntent = PendingIntent.getBroadcast(
            this,
            messageId,
            Intent(ACTION_MMS_SENT).putExtra(EXTRA_MESSAGE_ID, messageId),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            this,
            messageId,
            Intent(ACTION_MMS_DELIVERED).putExtra(EXTRA_MESSAGE_ID, messageId),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        /*registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
                Log.d(TAG, "MMS enviado. Resultado: $resultCode")
                unregisterReceiver(this)
            }
        }, IntentFilter(ACTION_MMS_SENT))

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "MMS entregado")
                unregisterReceiver(this)
                stopSelf()
            }
        }, IntentFilter(ACTION_MMS_DELIVERED))*/
    }

    private fun sendSms(phoneNumber: String, message: String, messageId: Int) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        val sentIntent = PendingIntent.getBroadcast(
            this,
            messageId,
            Intent(ACTION_MMS_SENT).putExtra(EXTRA_MESSAGE_ID, messageId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            this,
            messageId,
            Intent(ACTION_MMS_DELIVERED).putExtra(EXTRA_MESSAGE_ID, messageId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        smsManager.sendTextMessage(
            phoneNumber,
            null,
            message,
            sentIntent,
            deliveredIntent
        )
    }

    private fun sendMms(phoneNumber: String, message: String, imageUri: Uri, messageId: Int) {
        Log.d(TAG, "Enviando MMS no está completamente implementado")

        val smsText = if (message.isNotEmpty()) {
            "$message\n[Imagen adjunta no mostrada]"
        } else {
            "[Imagen adjunta no mostrada]"
        }

        sendSms(phoneNumber, smsText, messageId)
    }

    private fun saveSentMessage(phoneNumber: String, message: String?, imageUri: String?) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, phoneNumber)
                put(Telephony.Sms.BODY, message)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            }
            contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar mensaje: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}