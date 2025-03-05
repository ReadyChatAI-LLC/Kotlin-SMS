package com.aireply.data.local

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log

class SmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phone")
        val message = intent?.getStringExtra("message")
        if (!phoneNumber.isNullOrEmpty() && !message.isNullOrEmpty()) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("pruebas", "Mensaje enviado a $phoneNumber")
            } catch (e: Exception) {
                Log.e("pruebas", "Error al enviar SMS: ${e.message}")
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }
}
