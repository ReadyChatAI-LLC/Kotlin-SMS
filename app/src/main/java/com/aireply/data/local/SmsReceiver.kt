package com.aireply.data.local

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
import android.telephony.SmsMessage
import android.util.Log
import com.aireply.domain.models.SmsChat
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import com.aireply.data.local.dataStore.SmsReplyReceiver


private const val CHANNEL_ID = "sms_channel"
private const val KEY_TEXT_REPLY = "key_text_reply"
private const val NOTIFICATION_ID = 1

class SmsReceiver : BroadcastReceiver() {

    companion object {
        var smsListener: ((SmsChat) -> Unit)? = null

        fun clearSmsListener() {
            smsListener = null
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("messentrante", "onReceive() llamado con acción: ${intent.action}")

            Log.d("messentrante", "el dispositvo ha recibido un sms")
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as? Array<*>
                pdus?.forEach { pdu ->
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray, bundle.getString("format"))
                    val sender = sms.originatingAddress ?: "Unknown"
                    val messageBody = sms.messageBody
                    val timestamp = sms.timestampMillis

                    Log.d("messentrante", "SMS de $sender: $messageBody")
                    showNotification(context!!, sender, messageBody)

                    val values = ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, sender)
                        put(Telephony.Sms.BODY, messageBody)
                        put(Telephony.Sms.DATE, timestamp)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                    }
                    context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)



                    val receivedSms = SmsChat(
                        sender = sender,
                        content = messageBody,
                        timeStamp = timestamp,
                        status = "0",
                        type = "1",
                        contact = sender,
                        updatedAt = timestamp,
                        accountLogoColor = Color(0xFF87CEEB)
                    )
                    Log.d("messentrante", "SMS de $sender: $receivedSms")
                    smsListener?.invoke(receivedSms)
                }
            }

    }

    private fun showNotification(context: Context, sender: String?, content: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mensajes SMS",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }


        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Escribe tu respuesta...")
            .build()


        val replyIntent = Intent(context, SmsReplyReceiver::class.java).apply {
            putExtra("sender", sender)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )


        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_dialog_email,
            "Responder",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()


        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nuevo SMS de: $sender")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(replyAction)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}