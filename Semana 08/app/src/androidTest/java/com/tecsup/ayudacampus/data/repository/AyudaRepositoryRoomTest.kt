package com.tecsup.ayudacampus.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.ayudacampus.data.local.AyudaDatabase
import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.data.local.entity.Ticket
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AyudaRepositoryRoomTest {
    private lateinit var db: AyudaDatabase
    private lateinit var repo: AyudaRepository

    @Before
    fun preparar() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AyudaDatabase::class.java
        ).build()
        repo = AyudaRepositoryRoom(db)
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun operacionesPersistenYRespetanRelaciones() = runBlocking {
        withTimeout(30_000) {
            val usuario = Usuario(nombre = "Prueba", correo = "prueba@test", clave = "1234", rol = "CLIENTE")
            val usuarioId = db.usuarioDao().insertarTodos(listOf(usuario)).single().toInt()
            val autor = usuario.copy(id = usuarioId)
            assertEquals(autor, repo.login(usuario.correo, usuario.clave))
            assertNull(repo.login(usuario.correo, "incorrecta"))

            repo.guardarCategoria(Categoria(nombre = "Red", icono = "wifi"))
            repo.guardarCategoria(Categoria(nombre = "Vacia", icono = "tag"))
            val categoria = repo.observarCategorias().first().first { it.nombre == "Red" }
            val vacia = repo.observarCategorias().first().first { it.nombre == "Vacia" }
            repo.cambiarActiva(vacia.id, false)
            assertEquals(listOf(categoria), repo.observarCategoriasActivas().first())

            val antes = System.currentTimeMillis()
            val id = repo.crearTicket(usuarioId, categoria.id, "Falla de red", "Sin acceso a Internet", "Aula", Prioridad.ALTA).toInt()
            val creado = requireNotNull(repo.observarTicket(id).first())
            assertEquals("ABIERTO", creado.ticket.estado)
            assertEquals("ALTA", creado.ticket.prioridad)
            assertTrue(creado.ticket.fecha in antes..System.currentTimeMillis())
            assertEquals(categoria, creado.categoria)
            assertEquals(autor, creado.cliente)
            assertNull(repo.observarTicket(Int.MAX_VALUE).first())

            repo.guardarCategoria(categoria.copy(nombre = "Red actualizada"))
            assertEquals("Red actualizada", repo.observarTicket(id).first()!!.categoria.nombre)
            repo.agregarComentario(id, autor, "Continua la falla")
            val comentario = repo.observarTicket(id).first()!!.comentarios.single()
            assertEquals(id, comentario.ticketId)
            assertEquals(autor.id, comentario.autorId)
            assertEquals(autor.nombre, comentario.autorNombre)
            assertEquals(autor.rol, comentario.autorRol)
            assertEquals("Continua la falla", comentario.texto)
            assertTrue(comentario.fecha in antes..System.currentTimeMillis())

            repo.cambiarEstado(id, EstadoTicket.EN_PROCESO)
            repo.cambiarPrioridad(id, Prioridad.BAJA)
            assertEquals("EN_PROCESO", repo.observarTicket(id).first()!!.ticket.estado)
            assertEquals("BAJA", repo.observarTicket(id).first()!!.ticket.prioridad)
            repo.cambiarEstado(id, EstadoTicket.RESUELTO)
            val antesDeCerrar = System.currentTimeMillis()
            repo.cerrarConCalificacion(id, 4)
            val cerrado = repo.observarTicket(id).first()!!.ticket
            assertEquals("CERRADO", cerrado.estado)
            assertEquals(4, cerrado.calificacion)
            assertTrue(requireNotNull(cerrado.fechaCierre) in antesDeCerrar..System.currentTimeMillis())
            repo.reabrir(id)
            val reabierto = repo.observarTicket(id).first()!!.ticket
            assertEquals("ABIERTO", reabierto.estado)
            assertNull(reabierto.calificacion)
            assertNull(reabierto.fechaCierre)

            val conteos = repo.observarCantidadPorCategoria().first().associate { it.categoria to it.cantidad }
            assertEquals(mapOf("Red actualizada" to 1, "Vacia" to 0), conteos)
            repo.eliminarCategoria(vacia)
            assertEquals(1, repo.observarCategorias().first().size)
        }
    }

    @Test
    fun flujosOrdenanFiltranYNotificanCambios() = runBlocking {
        withTimeout(30_000) {
            val usuarios = db.usuarioDao().insertarTodos(listOf(
                Usuario(nombre = "Uno", correo = "uno@test", clave = "1", rol = "CLIENTE"),
                Usuario(nombre = "Dos", correo = "dos@test", clave = "2", rol = "CLIENTE")
            ))
            val clienteId = usuarios[0].toInt()
            val otroClienteId = usuarios[1].toInt()
            repo.guardarCategoria(Categoria(nombre = "Red", icono = "wifi"))
            val categoriaId = repo.observarCategorias().first().single().id
            val ahora = System.currentTimeMillis()
            suspend fun insertar(cliente: Int, prioridad: Prioridad, fecha: Long): Int =
                db.ticketDao().insertarTicket(Ticket(
                    clienteId = cliente, categoriaId = categoriaId, titulo = "Ticket de prueba",
                    descripcion = "Descripcion de prueba", ubicacion = "Aula",
                    prioridad = prioridad.name, fecha = fecha
                )).toInt()
            val baja = insertar(clienteId, Prioridad.BAJA, ahora)
            val altaAntigua = insertar(clienteId, Prioridad.ALTA, ahora - 7_200_000)
            val media = insertar(otroClienteId, Prioridad.MEDIA, ahora - 1000)
            val altaReciente = insertar(clienteId, Prioridad.ALTA, ahora - 2000)
            assertEquals(listOf(altaReciente, altaAntigua, media, baja), repo.observarTodosLosTickets().first().map { it.ticket.id })
            assertEquals(listOf(baja, altaReciente, altaAntigua), repo.observarTicketsCliente(clienteId).first().map { it.ticket.id })
            assertEquals(4, repo.observarCantidadPorEstado(EstadoTicket.ABIERTO).first())
            assertEquals(1, repo.observarAltasSinAtender(ahora - 3_600_000).first())

            val cambio = async(start = CoroutineStart.UNDISPATCHED) {
                repo.observarTicket(altaAntigua).first { it?.ticket?.estado == "EN_PROCESO" }
            }
            repo.cambiarEstado(altaAntigua, EstadoTicket.EN_PROCESO)
            assertEquals("EN_PROCESO", cambio.await()!!.ticket.estado)
            assertEquals(0, repo.observarAltasSinAtender(ahora - 3_600_000).first())
            assertEquals(1, repo.observarCantidadPorEstado(EstadoTicket.EN_PROCESO).first())
        }
    }
}
