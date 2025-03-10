package com.aireply.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object ChatListRoute

@Serializable
data class ChatRoute(val phoneNumber: String)

@Serializable
object SettingsRoute

@Serializable
object DefaultSmsRoute

@Serializable
object ContactsRoute

@Serializable
object ChatProfileRoute