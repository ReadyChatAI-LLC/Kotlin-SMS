package com.aireply.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object ChatListRoute

@Serializable
data class ChatDetailsRoute(val phoneNumber: String)

@Serializable
object SettingsRoute

@Serializable
object DefaultSmsRoute

@Serializable
object ContactsRoute

@Serializable
data class ChatProfileRoute(val phoneNumber: String)

@Serializable
object ArchivedChatsRoute