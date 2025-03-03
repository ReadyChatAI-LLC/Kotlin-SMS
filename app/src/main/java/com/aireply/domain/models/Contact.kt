package com.aireply.domain.models

import androidx.compose.ui.graphics.Color

data class Contact(
    val id: String,
    val name: String,
    val number: String,
    val accountLogoColor: Color
)
