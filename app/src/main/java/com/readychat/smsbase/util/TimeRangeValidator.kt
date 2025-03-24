package com.readychat.smsbase.util

import android.util.Log
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TimeRangeValidator {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun isValidTimeRange(startTime: String, endTime: String): Boolean {
        return try {
            val start = LocalTime.parse(startTime, timeFormatter)
            val end = LocalTime.parse(endTime, timeFormatter)
            Log.e("prueba", "Fechas validas. Inicio: $startTime - Final: $endTime")
            start.isBefore(end)
        } catch (e: DateTimeParseException) {
            Log.e("prueba", "Fechas NO validas. Inicio: $startTime - Final: $endTime")
            false
        }
    }
}
