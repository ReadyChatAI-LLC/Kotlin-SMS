package com.readychat.smsbase.domain.models

data class Settings(
    val readyChatActive: Boolean,
    val readyChatPrompt: String,
    val dateActive: Boolean,
    val dateRangeStart: String,
    val dateRangeEnd: String,
    val appStarted: Boolean
)
