package com.aireply.domain.models

data class Message(
    val id: Int,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean
)
