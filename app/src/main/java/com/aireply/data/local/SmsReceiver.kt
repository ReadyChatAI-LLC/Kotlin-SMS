package com.aireply.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            val pdus = bundle["pdus"] as? Array<*>
            pdus?.forEach { pdu ->
                val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                val sender = sms.originatingAddress
                val messageBody = sms.messageBody
                Log.d("pruebas", "SMS de $sender: $messageBody")
            }
        }
    }
}