package com.aireply.domain.models

import androidx.compose.ui.graphics.Color

data class SmsChat(
    val id: Int = 0,
    val sender: String,
    val content: String,
    val timeStamp: Long,
    var status: String,
    val type: String,
    val contact: String,
    val updatedAt: Long,
    val accountLogoColor: Color
)