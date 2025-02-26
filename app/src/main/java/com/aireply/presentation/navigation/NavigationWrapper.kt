package com.aireply.presentation.navigation

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aireply.presentation.screens.chatList.ChatListScreen
import com.aireply.presentation.screens.chatDetails.ChatScreen
import com.aireply.presentation.screens.defaultSms.DefaultSmsScreen
import com.aireply.presentation.screens.settings.SettingsScreen
import com.aireply.presentation.screens.startChat.StartChatScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val packageName = context.packageName
    val isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context) == packageName
    val contactPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    val startDestination = if (isDefaultSmsApp && contactPermissionGranted) ChatListRoute else DefaultSmsRoute

    NavHost(navController = navController, startDestination = startDestination) {
        composable<ChatListRoute> {
            Log.d("prueba", "Navegando a ChatListScreen")
            ChatListScreen(
                navigateToChat = { chatId -> navController.navigate(ChatRoute(chatId)) },
                navigateToSettings = {
                    navController.navigate(SettingsRoute)
                },
                navigateToStartChat = {
                    navController.navigate(StartChatRoute)
                })
        }

        composable<ChatRoute> { backStackEntry ->
            val chatId: ChatRoute = backStackEntry.toRoute()
            ChatScreen(chatId.chatId) {
                navController.popBackStack()
            }
        }

        composable<SettingsRoute> {
            SettingsScreen { navController.popBackStack() }
        }

        composable<DefaultSmsRoute> {
            DefaultSmsScreen {
                navController.navigate(ChatListRoute)
            }
        }

        composable<StartChatRoute> {
            StartChatScreen{ chatId ->
                navController.navigate(navController.navigate(ChatRoute(chatId)))
            }
        }
    }
}