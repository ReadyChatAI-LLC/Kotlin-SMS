package com.aireply.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatDate {
    fun formatDate(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }

}