package com.tecsup.ayudacampus.data.repository

import com.tecsup.ayudacampus.data.local.AyudaDatabase
import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.data.local.entity.Comentario
import com.tecsup.ayudacampus.data.local.entity.Ticket
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import com.tecsup.ayudacampus.model.ConteoCategoria
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import kotlinx.coroutines.flow.Flow

class AyudaRepositoryRoom(db: AyudaDatabase) : AyudaRepository {
    private val categoriaDao = db.categoriaDao()
    private val usuarioDao = db.usuarioDao()
    private val ticketDao = db.ticketDao()

    override suspend fun obtenerUsuario(id: Int): Usuario? = usuarioDao.obtenerPorId(id)

    override suspend fun login(correo: String, clave: String): Usuario? =
        usuarioDao.login(correo, clave)

    override fun observarCategorias(): Flow<List<Categoria>> = categoriaDao.observarTodas()

    override fun observarCategoriasActivas(): Flow<List<Categoria>> = categoriaDao.observarActivas()

    override suspend fun guardarCategoria(c: Categoria) {
        if (c.id == 0) categoriaDao.insertar(c) else categoriaDao.actualizar(c)
    }

    override suspend fun eliminarCategoria(c: Categoria) = categoriaDao.eliminar(c)

    override suspend fun cambiarActiva(id: Int, activa: Boolean) = categoriaDao.cambiarActiva(id, activa)

    override suspend fun crearTicket(
        clienteId: Int,
        categoriaId: Int,
        titulo: String,
        descripcion: String,
        ubicacion: String,
        prioridad: Prioridad
    ): Long = ticketDao.insertarTicket(
        Ticket(
            clienteId = clienteId,
            categoriaId = categoriaId,
            titulo = titulo,
            descripcion = descripcion,
            ubicacion = ubicacion,
            prioridad = prioridad.name,
            estado = EstadoTicket.ABIERTO.name,
            fecha = System.currentTimeMillis()
        )
    )

    override fun observarTicketsCliente(clienteId: Int): Flow<List<TicketConDetalle>> =
        ticketDao.observarPorCliente(clienteId)

    override fun observarTodosLosTickets(): Flow<List<TicketConDetalle>> = ticketDao.observarTodos()

    override fun observarTicket(id: Int): Flow<TicketConDetalle?> = ticketDao.observarUno(id)

    override suspend fun cambiarEstado(ticketId: Int, estado: EstadoTicket) =
        ticketDao.actualizarEstado(ticketId, estado.name)

    override suspend fun cambiarPrioridad(ticketId: Int, prioridad: Prioridad) =
        ticketDao.actualizarPrioridad(ticketId, prioridad.name)

    override suspend fun cerrarConCalificacion(ticketId: Int, calificacion: Int) =
        ticketDao.cerrar(ticketId, calificacion, System.currentTimeMillis())

    override suspend fun reabrir(ticketId: Int) = ticketDao.reabrir(ticketId)

    override suspend fun agregarComentario(ticketId: Int, autor: Usuario, texto: String) {
        ticketDao.insertarComentario(
            Comentario(
                ticketId = ticketId,
                autorId = autor.id,
                autorNombre = autor.nombre,
                autorRol = autor.rol,
                texto = texto,
                fecha = System.currentTimeMillis()
            )
        )
    }

    override fun observarCantidadPorEstado(estado: EstadoTicket): Flow<Int> =
        ticketDao.contarPorEstado(estado.name)

    override fun observarCantidadPorCategoria(): Flow<List<ConteoCategoria>> =
        ticketDao.contarPorCategoria()

    override fun observarAltasSinAtender(limite: Long): Flow<Int> = ticketDao.contarAltasSinAtender(limite)
}
