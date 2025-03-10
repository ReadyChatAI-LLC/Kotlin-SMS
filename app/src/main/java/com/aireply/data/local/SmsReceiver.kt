package com.aireply.data.local

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.aireply.domain.models.TextMessageModel

class SmsReceiver : BroadcastReceiver() {

    companion object {
        var smsListener: ((TextMessageModel) -> Unit)? = null

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


                    val values = ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, sender)
                        put(Telephony.Sms.BODY, messageBody)
                        put(Telephony.Sms.DATE, timestamp)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                    }
                    context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

                    val receivedSms = TextMessageModel(
                        sender = sender,
                        content = messageBody,
                        timeStamp = timestamp,
                        status = "0",
                        type = "1"
                    )

                    Log.d("messentrante", "SMS de $sender: $receivedSms")
                    smsListener?.invoke(receivedSms)
                }
            }

    }
}