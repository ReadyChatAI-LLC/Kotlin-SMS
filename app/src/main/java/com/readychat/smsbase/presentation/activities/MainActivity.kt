package com.readychat.smsbase.presentation.activities

import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.readychat.smsbase.presentation.navigation.NavigationWrapper
import com.readychat.smsbase.theme.AIReplyKotlinTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        installSplashScreen()

        setContent {
            Log.d("prueba", "SetContent inicializado")

            val navHostController = rememberNavController()
            navController = navHostController

            AIReplyKotlinTheme {
                NavigationWrapper()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        /*
        Log.d("prueba", "OnResume() inicializado")

        if (::navController.isInitialized) {
            if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {
                Log.d("prueba", "No es la app predeterminada")
                navController.navigate(DefaultSmsRoute)
            }
        }*/
    }
}