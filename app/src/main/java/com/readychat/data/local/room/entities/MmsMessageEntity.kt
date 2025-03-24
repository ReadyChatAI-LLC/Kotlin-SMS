package com.readychat.data.local.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.readychat.domain.models.MmsMessageModel
import com.readychat.domain.models.MmsPartModel

// Entidad para mensajes MMS
@Entity(
    tableName = "mms_messages",
    foreignKeys = [ForeignKey(
        entity = ChatDetailsEntity::class,
        parentColumns = ["id"],
        childColumns = ["chatId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["chatId"]), Index(value = ["mmsId"], unique = true)]
)
data class MmsMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mmsId: Long,           // ID original del sistema
    val chatId: Long,          // Relación con el chat
    val subject: String?,      // Asunto del MMS (puede ser nulo)
    val timeStamp: Long,       // Tiempo de recepción/envío
    val status: String,        // Igual que los mensajes normales (enviado, recibido, etc.)
    val read: Boolean,         // Si fue leído
    val messageBox: Int        // Indicador del tipo (enviado, recibido, borrador, etc.)
)

// Entidad para las partes de un MMS (texto, imágenes, etc.)
@Entity(
    tableName = "mms_parts",
    foreignKeys = [ForeignKey(
        entity = MmsMessageEntity::class,
        parentColumns = ["id"],
        childColumns = ["mmsMessageId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["mmsMessageId"])]
)
data class MmsPartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val partId: Long,          // ID de la parte en el sistema
    val mmsMessageId: Int,     // Referencia al mensaje MMS
    val contentType: String,   // Tipo MIME: text/plain, image/jpeg, etc.
    val text: String?,         // Texto si es una parte de texto
    val filePath: String?      // Ruta donde se guardó el archivo para imágenes/videos
)

// Relación MMS con sus partes
data class MmsWithParts(
    @Embedded val mms: MmsMessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mmsMessageId"
    )
    val parts: List<MmsPartEntity>
)

// Relación Chat con mensajes MMS
data class ChatWithMmsMessages(
    @Embedded val chat: ChatDetailsEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chatId"
    )
    val mmsMessages: List<MmsMessageEntity>
)

// Relación Chat con todos los tipos de mensajes
data class ChatWithAllMessages(
    @Embedded val chat: ChatDetailsEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chatId"
    )
    val messages: List<MessageEntity>,
    @Relation(
        entity = MmsMessageEntity::class,
        parentColumn = "id",
        entityColumn = "chatId"
    )
    val mmsWithParts: List<MmsWithParts>
)

// Funciones de mapeo a modelos de dominio
fun MmsMessageEntity.toDomain(parts: List<MmsPartEntity>): MmsMessageModel {
    return MmsMessageModel(
        id = id,
        mmsId = mmsId,
        chatId = chatId,
        subject = subject,
        timeStamp = timeStamp,
        status = status,
        read = read,
        messageBox = messageBox,
        parts = parts.map { it.toDomain() }
    )
}

fun MmsPartEntity.toDomain(): MmsPartModel {
    return MmsPartModel(
        id = id,
        partId = partId,
        mmsMessageId = mmsMessageId,
        contentType = contentType,
        text = text,
        filePath = filePath
    )
}

fun MmsWithParts.toDomain(): MmsMessageModel {
    return mms.toDomain(parts)
}