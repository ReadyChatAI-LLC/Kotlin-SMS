package com.aireply.presentation.screens.shared

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ErrorMessage(errorMessage: String) {
    Log.e("prueba", "Error: $errorMessage")
    Text(errorMessage, modifier = Modifier.fillMaxSize())
}