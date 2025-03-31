package com.readychat.smsbase.presentation.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.readychat.smsbase.presentation.viewmodel.MainViewModel
import com.readychat.smsbase.presentation.screens.chatList.MainChatListScreen
import com.readychat.smsbase.presentation.screens.chatDetails.MainChatDetailsScreen
import com.readychat.smsbase.presentation.screens.chatList.chatArchived.MainChatArchivedScreen
import com.readychat.smsbase.presentation.screens.chatProfile.MainChatProfileScreen
import com.readychat.smsbase.presentation.screens.defaultSms.DefaultSmsScreen
import com.readychat.smsbase.presentation.screens.settings.SettingsScreen
import com.readychat.smsbase.presentation.screens.contacts.ContactsScreen
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHost
import androidx.navigation.NavHostController

@Composable
fun NavigationWrapper(
    viewModel: MainViewModel = hiltViewModel()
) {

    val navController = rememberNavController()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val shouldShowPermissionScreen by viewModel.shouldShowPermissionScreen.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(shouldShowPermissionScreen) {
        if (shouldShowPermissionScreen) {
            val currentRoute = navController.currentDestination?: ChatListRoute
            if (currentRoute != DefaultSmsRoute) {
                navController.navigate(DefaultSmsRoute) {
                    navController.currentDestination?.route?.let { current ->
                        popUpTo(current) { inclusive = true }
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController, startDestination = ChatListRoute,
        enterTransition = NavTransitions.enterTransition,
        exitTransition = NavTransitions.exitTransition,
        popEnterTransition = NavTransitions.popEnterTransition,
        popExitTransition = NavTransitions.popExitTransition
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

        composable<ChatDetailsRoute> { backStackEntry ->
            val phoneNumber: ChatDetailsRoute = backStackEntry.toRoute()
            MainChatDetailsScreen(
                address = phoneNumber.phoneNumber,
                onBack = {
                    navController.popBackStack()
                },
                onProfileClick = { address -> navController.navigate(ChatProfileRoute(address)) })
        }

        composable<SettingsRoute> {
            SettingsScreen { navController.popBackStack() }
        }

        composable<DefaultSmsRoute> {
            DefaultSmsScreen(onSetDefaultApp = {
                navController.navigate(ChatListRoute)
            })
        }

        composable<ContactsRoute>{
            ContactsScreen(onContactClick = { phoneNumber ->
                navController.navigate(ChatDetailsRoute(phoneNumber))
            }, onBack = { navController.popBackStack() })
        }

        composable<ChatProfileRoute> { backStackEntry ->
            val phoneNumber: ChatDetailsRoute = backStackEntry.toRoute()
            MainChatProfileScreen(
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