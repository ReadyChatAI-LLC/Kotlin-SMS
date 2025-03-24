package com.readychat.smsbase.domain.models

import android.graphics.Color

data class MmsMessageModel(
    val id: Int = 0,
    val mmsId: Long,
    val chatId: Long,
    val subject: String?,
    val timeStamp: Long,
    val status: String,
    val read: Boolean,
    val messageBox: Int,
    val parts: List<MmsPartModel> = emptyList()
)

data class MmsPartModel(
    val id: Int = 0,
    val partId: Long,
    val mmsMessageId: Int,
    val contentType: String,
    val text: String?,
    val filePath: String?
) {
    // Función auxiliar para determinar el tipo de contenido
    fun isText(): Boolean = contentType.contains("text")
    fun isImage(): Boolean = contentType.contains("image")
    fun isVideo(): Boolean = contentType.contains("video")
    fun isAudio(): Boolean = contentType.contains("audio")
}