package com.readychat.smsbase.presentation.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.readychat.smsbase.presentation.screens.chatList.MainChatListScreen
import com.readychat.smsbase.presentation.screens.chatDetails.MainChatDetailsScreen
import com.readychat.smsbase.presentation.screens.chatList.chatArchived.MainChatArchivedScreen
import com.readychat.smsbase.presentation.screens.chatProfile.MainChatProfileScreen
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

    val slideEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(200)
        )
    }

    val slideExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(100)
        )
    }

    NavHost(
        navController = navController, startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(200)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(100)
            )
        },
    ) {
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

        composable<ChatDetailsRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) { backStackEntry ->
            val phoneNumber: ChatDetailsRoute = backStackEntry.toRoute()
            MainChatDetailsScreen(
                address = phoneNumber.phoneNumber,
                onBack = {
                    navController.navigate(ChatListRoute) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onProfileClick = { address -> navController.navigate(ChatProfileRoute(address)) })
        }

        composable<SettingsRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) {
            SettingsScreen { navController.popBackStack() }
        }

        composable<DefaultSmsRoute>(
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(100)) }
        ) {
            DefaultSmsScreen(onSetDefaultApp = {
                navController.navigate(ChatListRoute)
            })
        }

        composable<ContactsRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) {
            ContactsScreen(onContactClick = { phoneNumber ->
                navController.navigate(ChatDetailsRoute(phoneNumber))
            }, onBack = { navController.popBackStack() })
        }

        composable<ChatProfileRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) { backStackEntry ->
            val phoneNumber: ChatDetailsRoute = backStackEntry.toRoute()
            MainChatProfileScreen(
                address = phoneNumber.phoneNumber,
                onBack = { navController.popBackStack() })
        }

        composable<ArchivedChatsRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) {
            MainChatArchivedScreen(
                navigateToChatDetails = { address -> navController.navigate(ChatDetailsRoute(address)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}