package com.aireply.presentation.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.aireply.presentation.navigation.NavigationWrapper
import com.aireply.ui.theme.AIReplyKotlinTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        installSplashScreen()

        setContent {
            AIReplyKotlinTheme {
                NavigationWrapper()
            }
        }
    }
}