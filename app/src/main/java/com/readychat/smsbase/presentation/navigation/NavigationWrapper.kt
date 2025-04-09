package com.readychat.smsbase.presentation.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.readychat.smsbase.presentation.screens.chatDetailsGroup.MainGroupChatScreen

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
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start)
    }

    val slideExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition
    ) {
        composable<ChatListRoute> {
            MainChatListScreen(
                navigateToChatDetails = { address -> navController.navigate(ChatDetailsRoute(address)) },
                navigateToGroupChat = { groupName, members ->
                    navController.navigate(GroupChatRoute(groupName, members))
                },
                navigateToSettings = { navController.navigate(SettingsRoute) },
                navigateToStartChat = { navController.navigate(ContactsRoute) },
                navigateToSetDefaultScreen = { navController.navigate(DefaultSmsRoute) },
                navigateToArchivedChats = { navController.navigate(ArchivedChatsRoute) }
            )
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
                onProfileClick = { address -> navController.navigate(ChatProfileRoute(address)) }
            )
        }

        composable<SettingsRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) {
            SettingsScreen { navController.popBackStack() }
        }

        composable<DefaultSmsRoute>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            DefaultSmsScreen(onSetDefaultApp = {
                navController.navigate(ChatListRoute)
            })
        }

        composable<ContactsRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) {
            ContactsScreen(
                onContactClick = { phoneNumber ->
                    navController.navigate(ChatDetailsRoute(phoneNumber))
                },
                onBack = { navController.popBackStack() },
                onCreateGroup = { groupName, memberAddresses ->
                    navController.navigate(GroupChatRoute(groupName, memberAddresses))
                }
            )
        }

        composable<ChatProfileRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) { backStackEntry ->
            val phoneNumber: ChatDetailsRoute = backStackEntry.toRoute()
            MainChatProfileScreen(
                address = phoneNumber.phoneNumber,
                onBack = { navController.popBackStack() }
            )
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

        composable<GroupChatRoute>(
            enterTransition = slideEnterTransition,
            exitTransition = slideExitTransition
        ) { backStackEntry ->
            val route: GroupChatRoute = backStackEntry.toRoute()
            MainGroupChatScreen(
                groupName = route.groupName,
                members = route.members,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
