package com.readychat.smsbase.util

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
                Log.d("prueba", "PhoneNumberParser -> $phoneNumber no tiene formato")
                e.printStackTrace()
                phoneNumber
            }
    }

    fun getPhoneNumberInfo(phoneNumber: String): PhoneNumberInfo {
        val phoneUtil = PhoneNumberUtil.getInstance()
        Log.d("prueba", "PhoneNumber to be paser: $phoneNumber")
        var phoneNumberProto: Phonenumber.PhoneNumber
        return try {
            Log.d("prueba", "0")
            phoneNumberProto = phoneUtil.parse(phoneNumber, null)
            Log.d("prueba", "1")
            val country = phoneUtil.getRegionCodeForNumber(phoneNumberProto)
            val internationalFormat = phoneUtil.format(phoneNumberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            Log.d("prueba", "2")
            val regionCode = phoneUtil.getRegionCodeForNumber(phoneNumberProto)
            Log.d("prueba", "3")
            val countryCode = phoneNumberProto.countryCode
            val nationalNumber = phoneNumberProto.nationalNumber

            Log.d("prueba", "Pais: $country")
            Log.d("prueba", "Region code:: $regionCode")
            Log.d("prueba", "international number: $internationalFormat")

            PhoneNumberInfo(internationalFormat, regionCode)
        } catch (e: Exception) {
            phoneNumberProto = phoneUtil.parse(phoneNumber, "CO")
            val internationalFormat = phoneUtil.format(phoneNumberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            Log.e("prueba", "Error: ${e.message}. But phonenumber formated: $internationalFormat")
            e.printStackTrace()
            PhoneNumberInfo(internationalFormat, "CO")
        }
    }

    fun getCleanPhoneNumber(input: String): NumberAndCountryCodeObj {
        val trimmed = input.trim()

        if (trimmed.startsWith("+")) {
            val phoneUtil = PhoneNumberUtil.getInstance()
            return try {
                val numberProto = phoneUtil.parse(trimmed, null)
                val nationalNumber = numberProto.nationalNumber.toString()
                val countryCode = numberProto.countryCode.toString()
                NumberAndCountryCodeObj(nationalNumber, countryCode)
            } catch (e: Exception) {
                NumberAndCountryCodeObj(trimmed, null)
            }
        } else {
            val cleanedNumber = trimmed.replace("\\s+".toRegex(), "")
            return NumberAndCountryCodeObj(cleanedNumber, null)
        }
    }

    fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace("[()\\-]".toRegex(), "")
    }
}

data class PhoneNumberInfo(
    val internationalPhone: String,
    val region: String
)

data class NumberAndCountryCodeObj(val number: String, val countryCode: String?)