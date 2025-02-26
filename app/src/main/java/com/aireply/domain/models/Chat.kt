package com.aireply.domain.models

data class Chat(
    val contact: String,
    val lastMessage: SmsMessage,
    val messages: List<SmsMessage>
)
