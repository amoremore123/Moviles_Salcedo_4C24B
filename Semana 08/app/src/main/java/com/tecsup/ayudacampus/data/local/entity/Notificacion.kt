package com.tecsup.ayudacampus.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["destinatarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["destinatarioId"]), Index(value = ["leida"])]
)
data class Notificacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destinatarioId: Int,
    val tipo: String, // NUEVO_COMENTARIO, TICKET_ACTUALIZADO, NUEVO_TICKET, RESPUESTA_COMENTARIO
    val origenId: Int,
    val origenNombre: String,
    val ticketId: Int,
    val mensaje: String,
    val leida: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)
