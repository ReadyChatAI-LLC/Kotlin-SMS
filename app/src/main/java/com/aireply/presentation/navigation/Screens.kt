package com.aireply.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object ChatListRoute

@Serializable
data class ChatRoute(val chatId: String)

@Serializable
object SettingsRoute

@Serializable
object DefaultSmsRoute

@Serializable
object StartChatRoute