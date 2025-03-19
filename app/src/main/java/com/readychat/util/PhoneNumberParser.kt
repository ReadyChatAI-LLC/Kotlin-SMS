package com.readychat.util

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

object PhoneNumberParser {
    fun phoneNumberParser(phoneNumber: String): String{
            val phoneUtil = PhoneNumberUtil.getInstance()
            return try {
                val phoneNumberProto: Phonenumber.PhoneNumber = phoneUtil.parse(phoneNumber, null)

                val country = phoneUtil.getRegionCodeForNumber(phoneNumberProto)
                val countryCode = phoneNumberProto.countryCode
                val nationalNumber = phoneNumberProto.nationalNumber

                Log.d("prueba", "País: $country")
                Log.d("prueba", "Código de país: $countryCode")
                Log.d("prueba", "Número nacional: $nationalNumber")

                nationalNumber.toString()
            } catch (e: Exception) {
                Log.e("prueba", "Error: ${e.message}")
                e.printStackTrace()
                phoneNumber
            }
    }

    fun getPhoneNumberInfo(phoneNumber: String): PhoneNumberInfo{
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val phoneNumberProto: Phonenumber.PhoneNumber = phoneUtil.parse(phoneNumber, null)

            val country = phoneUtil.getRegionCodeForNumber(phoneNumberProto)
            val countryCode = phoneNumberProto.countryCode
            val nationalNumber = phoneNumberProto.nationalNumber

            Log.d("prueba", "País: $country")
            Log.d("prueba", "Código de país: $countryCode")
            Log.d("prueba", "Número nacional: $nationalNumber")

            PhoneNumberInfo(nationalNumber.toString(), country, countryCode.toString())
        } catch (e: Exception) {
            Log.e("prueba", "Error: ${e.message}")
            e.printStackTrace()
            PhoneNumberInfo(phoneNumber)
        }
    }

    fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace("[()\\-]".toRegex(), "")
    }
}

data class PhoneNumberInfo(
    val phoneNumber: String,
    val country: String? = null,
    val countryCode: String? = null
)