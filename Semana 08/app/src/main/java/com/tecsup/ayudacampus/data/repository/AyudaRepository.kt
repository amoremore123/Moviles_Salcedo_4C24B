package com.tecsup.ayudacampus.data.repository

import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.data.local.entity.Notificacion
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import com.tecsup.ayudacampus.model.ConteoCategoria
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import kotlinx.coroutines.flow.Flow

interface AyudaRepository {
    suspend fun obtenerUsuario(id: Int): Usuario?

    suspend fun obtenerUsuarioPorRol(rol: String): Usuario?

    suspend fun login(correo: String, clave: String): Usuario?

    fun observarCategorias(): Flow<List<Categoria>>
    fun observarCategoriasActivas(): Flow<List<Categoria>>
    suspend fun guardarCategoria(c: Categoria)
    suspend fun eliminarCategoria(c: Categoria)
    suspend fun cambiarActiva(id: Int, activa: Boolean)

    suspend fun crearTicket(
        clienteId: Int,
        categoriaId: Int,
        titulo: String,
        descripcion: String,
        ubicacion: String,
        prioridad: Prioridad
    ): Long
    fun observarTicketsCliente(clienteId: Int): Flow<List<TicketConDetalle>>
    fun observarTodosLosTickets(): Flow<List<TicketConDetalle>>
    fun observarTicket(id: Int): Flow<TicketConDetalle?>
    suspend fun obtenerTicketPorId(id: Int): TicketConDetalle?
    suspend fun cambiarEstado(ticketId: Int, estado: EstadoTicket)
    suspend fun cambiarPrioridad(ticketId: Int, prioridad: Prioridad)
    suspend fun cerrarConCalificacion(ticketId: Int, calificacion: Int)
    suspend fun reabrir(ticketId: Int)
    suspend fun agregarComentario(ticketId: Int, autor: Usuario, texto: String)

    suspend fun obtenerUsuarioPorCorreo(correo: String): Usuario?
    suspend fun registrarUsuario(nombre: String, correo: String, clave: String, rol: String): Long

    fun observarNotificaciones(destinatarioId: Int): Flow<List<Notificacion>>
    suspend fun marcarComoLeida(id: Int)
    suspend fun marcarTodasComoLeidas(destinatarioId: Int)
    suspend fun eliminarNoLeidas(destinatarioId: Int)
    suspend fun generarNotificacion(
        destinatarioId: Int,
        tipo: String,
        origenId: Int,
        origenNombre: String,
        ticketId: Int,
        mensaje: String
    ): Long

    fun observarCantidadPorEstado(estado: EstadoTicket): Flow<Int>
    fun observarCantidadPorCategoria(): Flow<List<ConteoCategoria>>
    fun observarAltasSinAtender(limite: Long): Flow<Int>
    fun contarAltasSinAtender(limite: Long): Flow<Int>
}
