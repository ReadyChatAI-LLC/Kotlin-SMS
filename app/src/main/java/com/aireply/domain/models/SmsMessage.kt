package com.aireply.domain.models

data class SmsMessage(
    val address: String,
    val body: String,
    val date: Long,
    val senderName: String
)