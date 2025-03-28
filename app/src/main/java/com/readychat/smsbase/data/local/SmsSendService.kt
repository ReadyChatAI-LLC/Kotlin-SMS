package com.readychat.smsbase.data.local

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log

class SmsSendService : Service() {

    private fun saveSentSms(phoneNumber: String, message: String){
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, phoneNumber)
            put(Telephony.Sms.BODY, message)
            put(Telephony.Sms.DATE, System.currentTimeMillis())
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            Log.d("prueba", "Mensajffffffffff")
        }

        try {
            contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
            Log.d("prueba", "Mensaje guardado en la base de datos")
        }catch (e: Exception){
            Log.e("prueba", "Error al guardar SMS en la base de datos: ${e.message}")
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phone")
        val message = intent?.getStringExtra("message")
        if (!phoneNumber.isNullOrEmpty() && !message.isNullOrEmpty()) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("prueba", "Mensaje enviado a $phoneNumber")


                saveSentSms(phoneNumber, message)

            } catch (e: Exception) {
                Log.e("prueba", "Error al enviar SMS: ${e.message}")
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }
}
