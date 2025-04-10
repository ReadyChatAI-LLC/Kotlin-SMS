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
        if (phoneNumber.any { it.isDigit() }) {
            Log.w("PhoneNumberParser", "Input contains letters. Returning as Unknown.")
            return PhoneNumberInfo(phoneNumber, "Unknown")
        }

        val phoneUtil = PhoneNumberUtil.getInstance()
        Log.d("PhoneNumberParser", "PhoneNumber to be parsed: $phoneNumber")

        return try {
            val phoneNumberProto = phoneUtil.parse(phoneNumber, null)
            val internationalFormat = phoneUtil.format(phoneNumberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            val regionCode = phoneUtil.getRegionCodeForNumber(phoneNumberProto)

            Log.d("PhoneNumberParser", "Region code: $regionCode, International format: $internationalFormat")

            PhoneNumberInfo(internationalFormat, regionCode)
        } catch (e: Exception) {
            // Intentamos con un país por defecto, en este caso Colombia ("CO")
            return try {
                val fallbackProto = phoneUtil.parse(phoneNumber, "CO")
                val fallbackFormat = phoneUtil.format(fallbackProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)

                Log.e("PhoneNumberParser", "Fallback success with CO. Formatted: $fallbackFormat")
                PhoneNumberInfo(fallbackFormat, "Unknown")
            } catch (fallbackEx: Exception) {
                Log.e("PhoneNumberParser", "Fallback failed too: ${fallbackEx.message}")
                PhoneNumberInfo(phoneNumber, "Unknown")
            }
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