package com.tecsup.ayudacampus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tecsup.ayudacampus.data.local.entity.Notificacion
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    @Query("SELECT * FROM notificaciones WHERE destinatarioId = :destinatarioId ORDER BY fecha DESC")
    fun observarPorDestinatario(destinatarioId: Int): Flow<List<Notificacion>>

    @Query("SELECT * FROM notificaciones WHERE destinatarioId = :destinatarioId AND leida = 0 ORDER BY fecha DESC")
    fun observarNoLeidas(destinatarioId: Int): Flow<List<Notificacion>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE destinatarioId = :destinatarioId AND leida = 0")
    fun contarNoLeidas(destinatarioId: Int): Flow<Int>

    @Query("SELECT * FROM notificaciones WHERE destinatarioId = :destinatarioId AND leida = 0 ORDER BY fecha DESC LIMIT 1")
    suspend fun obtenerMasRecienteNoLeida(destinatarioId: Int): Notificacion?

    @Insert
    suspend fun insertar(n: Notificacion): Long

    @Query("UPDATE notificaciones SET leida = 1 WHERE destinatarioId = :destinatarioId AND leida = 0")
    suspend fun marcarComoLeidas(destinatarioId: Int)

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun marcarComoLeida(id: Int)

    @Query("DELETE FROM notificaciones WHERE id = :id")
    suspend fun eliminar(id: Int)
}
