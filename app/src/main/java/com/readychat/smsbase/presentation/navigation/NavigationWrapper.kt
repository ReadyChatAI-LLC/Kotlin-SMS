package com.readychat.smsbase.presentation.navigation

import ChatProfileScreen
import android.Manifest
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.readychat.smsbase.presentation.screens.chatList.MainChatListScreen
import com.readychat.smsbase.presentation.screens.chatDetails.MainChatDetailsScreen
import com.readychat.smsbase.presentation.screens.chatList.chatArchived.MainChatArchivedScreen
import com.readychat.smsbase.presentation.screens.defaultSms.DefaultSmsScreen
import com.readychat.smsbase.presentation.screens.settings.SettingsScreen
import com.readychat.smsbase.presentation.screens.contacts.ContactsScreen

@Composable
fun NavigationWrapper() {

    val navController = rememberNavController()

    Log.d("prueba", "NavigationWrapper Iniciada y ejecutando")

    val context = LocalContext.current

    val packageName = context.packageName

    val isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context) == packageName
    val contactPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    val startDestination =
        if (isDefaultSmsApp && contactPermissionGranted) ChatListRoute else DefaultSmsRoute

    NavHost(navController = navController, startDestination = startDestination) {
        composable<ChatListRoute> {
            Log.d("prueba", "Navegando a ChatListScreen")
            MainChatListScreen(
                navigateToChatDetails = { address -> navController.navigate(ChatDetailsRoute(address)) },
                navigateToSettings = {
                    navController.navigate(SettingsRoute)
                },
                navigateToStartChat = {
                    navController.navigate(ContactsRoute)
                },
                navigateToSetDefaultScreen = {
                    navController.navigate(DefaultSmsRoute)
                },
                navigateToArchivedChats = {
                    navController.navigate(ArchivedChatsRoute)
                })
        }

        composable<ChatDetailsRoute> { backStackEntry ->
            val phoneNumber: ChatDetailsRoute = backStackEntry.toRoute()
            MainChatDetailsScreen(
                address = phoneNumber.phoneNumber,
                onBack = { navController.popBackStack() },
                onTopBarClick = { address -> navController.navigate(ChatProfileRoute(address)) })
        }

        composable<SettingsRoute> {
            SettingsScreen { navController.popBackStack() }
        }

        composable<DefaultSmsRoute> {
            DefaultSmsScreen(onSetDefaultApp = {
                navController.navigate(ChatListRoute)
            })
        }

        composable<ContactsRoute> {
            ContactsScreen(onContactClick = { phoneNumber ->
                navController.navigate(ChatDetailsRoute(phoneNumber))
            }, onBack = { navController.popBackStack() })
        }

        composable<ChatProfileRoute> { backStackEntry ->
            val phoneNumber: ChatDetailsRoute = backStackEntry.toRoute()
            ChatProfileScreen(
                address = phoneNumber.phoneNumber,
                onBack = { navController.popBackStack() })
        }

        composable<ArchivedChatsRoute> {
            MainChatArchivedScreen(
                navigateToChatDetails = { address -> navController.navigate(ChatDetailsRoute(address)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}