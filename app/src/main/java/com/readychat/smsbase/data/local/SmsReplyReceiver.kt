package com.readychat.smsbase.data.local

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log

private const val KEY_TEXT_REPLY = "key_text_reply"

class SmsReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val sender = intent.getStringExtra("sender")
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val replyText = remoteInput?.getCharSequence(KEY_TEXT_REPLY)?.toString()

        if (!replyText.isNullOrEmpty() && sender != null) {
            SmsManager.getDefault().sendTextMessage(sender, null, replyText, null, null)
            Log.d("SmsReplyReceiverPrueba", "Respuesta enviada a $sender: $replyText")

            saveSentSms(context, sender, replyText)
        }
    }

    private fun saveSentSms(context: Context, phoneNumber: String, message: String) {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, phoneNumber)
            put(Telephony.Sms.BODY, message)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
        }

        try {
            context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
            Log.d("SmsReplyReceiver", "Mensaje guardado en la base de datos SMS")
        } catch (e: Exception) {
            Log.e("SmsReplyReceiver", "Error al guardar SMS en la base de datos: ${e.message}")
        }
    }
}
