package com.aireply.domain.models

data class ChatDetailsModel(
    val address: String,
    val contact: String,
    val chatList: MutableList<SmsChat>
)