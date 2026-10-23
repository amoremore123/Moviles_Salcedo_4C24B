package com.tecsup.ayudacampus

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.ayudacampus.data.local.AyudaDatabase
import com.tecsup.ayudacampus.data.local.DatosIniciales
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.data.repository.AyudaRepositoryRoom
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import com.tecsup.ayudacampus.viewmodel.DetalleTicketViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DetalleTicketViewModelTest {
    private lateinit var db: AyudaDatabase
    private lateinit var repo: AyudaRepository
    private lateinit var sesion: MutableStateFlow<Usuario?>
    private val modelos = mutableListOf<DetalleTicketViewModel>()
    private val instrumentacion get() = InstrumentationRegistry.getInstrumentation()

    @Before
    fun preparar(): Unit = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AyudaDatabase::class.java).build()
        DatosIniciales.sembrar(db)
        repo = AyudaRepositoryRoom(db)
        sesion = MutableStateFlow(requireNotNull(repo.login("juan@campus.test", "1234")))
    }
    @After
    fun limpiar() {
        modelos.forEach { it.viewModelScope.cancel() }
        db.close()
    }
    private suspend fun id(estado: String) = repo.observarTodosLosTickets().first().first { it.ticket.estado == estado }.ticket.id
    private suspend fun nuevo(id: Int, repository: AyudaRepository = repo): DetalleTicketViewModel {
        val vm = DetalleTicketViewModel(id, repository, sesion)
        modelos.add(vm)
        esperar(vm) { !it.cargando }
        return vm
    }
    private suspend fun esperar(vm: DetalleTicketViewModel, condicion: (DetalleTicketViewModel.Estado) -> Boolean) =
        withTimeout(15_000) { vm.estado.first(condicion) }
    private suspend fun accion(vm: DetalleTicketViewModel, bloque: () -> Unit) {
        instrumentacion.runOnMainSync(bloque)
        esperar(vm) { !it.operando }
    }

    @Test
    fun observaInformacionCompletaComentariosYEstadoDeRoom(): Unit = runBlocking {
        val id = id("ABIERTO")
        val vm = nuevo(id)
        val inicial = requireNotNull(vm.estado.value.detalle)
        assertEquals(id, inicial.ticket.id)
        assertEquals(sesion.value!!.id, inicial.cliente.id)
        assertEquals("Red / Wi-Fi", inicial.categoria.nombre)
        assertFalse(inicial.ticket.descripcion.isBlank())
        assertFalse(inicial.comentarios.isEmpty())
        val soporte = requireNotNull(repo.login("soporte@campus.test", "admin"))
        repo.agregarComentario(id, soporte, "Respuesta de soporte en tiempo real")
        val actualizado = esperar(vm) { it.detalle?.comentarios?.any { c -> c.texto == "Respuesta de soporte en tiempo real" } == true }
        assertEquals(soporte.id, actualizado.detalle!!.comentarios.last().autorId)
        repo.cambiarEstado(id, EstadoTicket.RESUELTO)
        esperar(vm) { it.detalle?.ticket?.estado == "RESUELTO" }
    }

    @Test
    fun validaComentarioConservaBorradorReintentaYEvitaDobleEnvio(): Unit = runBlocking {
        val id = id("ABIERTO")
        var fallar = true
        val inicio = CompletableDeferred<Unit>()
        val continuar = CompletableDeferred<Unit>()
        val controlado = object : AyudaRepository by repo {
            override suspend fun agregarComentario(ticketId: Int, autor: Usuario, texto: String) {
                if (fallar) { fallar = false; throw IOException("Fallo anterior a la insercion") }
                inicio.complete(Unit)
                continuar.await()
                repo.agregarComentario(ticketId, autor, texto)
            }
        }
        val vm = nuevo(id, controlado)
        val cantidad = vm.estado.value.detalle!!.comentarios.size
        accion(vm) { vm.comentar("   ") }
        assertNotNull(vm.estado.value.error)
        instrumentacion.runOnMainSync { vm.actualizarComentario("  El problema continúa en mi equipo  ") }
        accion(vm) { vm.comentar() }
        assertNotNull(vm.estado.value.error)
        assertEquals("  El problema continúa en mi equipo  ", vm.estado.value.texto)
        instrumentacion.runOnMainSync { vm.comentar(); vm.comentar() }
        withTimeout(15_000) { inicio.await() }
        continuar.complete(Unit)
        esperar(vm) { !it.operando && it.confirmacion != null }
        val resultado = esperar(vm) { it.detalle?.comentarios?.size == cantidad + 1 }
        val comentario = resultado.detalle!!.comentarios.last()
        assertEquals("El problema continúa en mi equipo", comentario.texto)
        assertEquals(sesion.value!!.id, comentario.autorId)
        assertEquals(sesion.value!!.nombre, comentario.autorNombre)
        assertEquals(sesion.value!!.rol, comentario.autorRol)
        assertEquals("", vm.estado.value.texto)
        assertNull(vm.estado.value.error)
    }

    @Test
    fun cerrarExigeResueltoYCalificacionValidaYPersisteSinRepetirse(): Unit = runBlocking {
        val abierto = nuevo(id("ABIERTO"))
        accion(abierto) { abierto.cerrar(5) }
        assertNotNull(abierto.estado.value.error)
        val id = id("RESUELTO")
        val vm = nuevo(id)
        for (calificacion in listOf(0, 6, -1)) {
            accion(vm) { vm.cerrar(calificacion) }
            assertNotNull(vm.estado.value.error)
            assertEquals("RESUELTO", repo.observarTicket(id).first()!!.ticket.estado)
        }
        val antes = System.currentTimeMillis()
        accion(vm) { vm.cerrar(4); vm.cerrar(1) }
        esperar(vm) { it.detalle?.ticket?.estado == "CERRADO" }
        val cerrado = repo.observarTicket(id).first()!!.ticket
        assertEquals(4, cerrado.calificacion)
        assertTrue(requireNotNull(cerrado.fechaCierre) in antes..System.currentTimeMillis())
        accion(vm) { vm.cerrar(1) }
        assertNotNull(vm.estado.value.error)
        assertEquals(cerrado, repo.observarTicket(id).first()!!.ticket)
        val comentarios = repo.observarTicket(id).first()!!.comentarios.size
        accion(vm) { vm.comentar("No debe guardarse") }
        assertEquals(comentarios, repo.observarTicket(id).first()!!.comentarios.size)
        accion(vm) { vm.reabrir() }
        assertEquals("CERRADO", repo.observarTicket(id).first()!!.ticket.estado)
        // Los límites 1 y 5 también deben admitirse en tickets resueltos.
        for (nota in listOf(1, 5)) {
            repo.cambiarEstado(id, EstadoTicket.RESUELTO)
            esperar(vm) { it.detalle?.ticket?.estado == "RESUELTO" }
            accion(vm) { vm.cerrar(nota) }
            assertEquals(nota, repo.observarTicket(id).first()!!.ticket.calificacion)
        }
    }

    @Test
    fun reabreSoloResueltosYLimpiaDatosDeCierre(): Unit = runBlocking {
        val id = id("RESUELTO")
        repo.cerrarConCalificacion(id, 3)
        repo.cambiarEstado(id, EstadoTicket.RESUELTO)
        val vm = nuevo(id)
        accion(vm) { vm.reabrir(); vm.reabrir() }
        esperar(vm) { it.detalle?.ticket?.estado == "ABIERTO" }
        val ticket = repo.observarTicket(id).first()!!.ticket
        assertNull(ticket.calificacion)
        assertNull(ticket.fechaCierre)
        accion(vm) { vm.reabrir() }
        assertNotNull(vm.estado.value.error)
        repo.cambiarEstado(id, EstadoTicket.EN_PROCESO)
        accion(vm) { vm.cerrar(5) }
        assertEquals("EN_PROCESO", repo.observarTicket(id).first()!!.ticket.estado)
    }

    @Test
    fun bloqueaRutasAjenasIdsInvalidosYSesionesInvalidas(): Unit = runBlocking {
        val juan = requireNotNull(sesion.value)
        val otroId = db.usuarioDao().insertarTodos(listOf(Usuario(nombre = "Otro", correo = "otro@test", clave = "1234", rol = "CLIENTE"))).single().toInt()
        val categoria = repo.observarCategorias().first().first().id
        val ajeno = repo.crearTicket(otroId, categoria, "Información privada", "Descripción privada", "Otra aula", Prioridad.MEDIA).toInt()
        repo.cambiarEstado(ajeno, EstadoTicket.RESUELTO)
        for (id in listOf(ajeno, -1, Int.MAX_VALUE)) {
            val vm = nuevo(id)
            assertNull(vm.estado.value.detalle)
            assertNotNull(vm.estado.value.errorCarga)
            accion(vm) { vm.comentar("Intrusión") }
            accion(vm) { vm.cerrar(5) }
            accion(vm) { vm.reabrir() }
            assertNull(vm.estado.value.detalle)
        }
        assertEquals("RESUELTO", repo.observarTicket(ajeno).first()!!.ticket.estado)
        assertTrue(repo.observarTicket(ajeno).first()!!.comentarios.isEmpty())
        val propio = id("ABIERTO")
        for (usuario in listOf(null, repo.login("soporte@campus.test", "admin"), juan.copy(id = Int.MAX_VALUE))) {
            sesion.value = usuario
            val vm = nuevo(propio)
            assertNull(vm.estado.value.detalle)
            accion(vm) { vm.comentar("Sin autorización") }
            accion(vm) { vm.cerrar(5) }
            assertNotNull(vm.estado.value.error)
        }
        sesion.value = juan
        val vm = nuevo(propio)
        assertNotNull(vm.estado.value.detalle)
        sesion.value = repo.obtenerUsuario(otroId)
        esperar(vm) { !it.cargando && it.errorCarga != null && it.detalle == null }
    }

    @Test
    fun errorDeLecturaOcultaDatosYPermiteReintentar(): Unit = runBlocking {
        val id = id("ABIERTO")
        var fallar = true
        val intermitente = object : AyudaRepository by repo {
            override fun observarTicket(id: Int) = flow {
                if (fallar) { fallar = false; throw IOException("Lectura interrumpida") }
                emitAll(repo.observarTicket(id))
            }
        }
        val vm = nuevo(id, intermitente)
        assertNotNull(vm.estado.value.errorCarga)
        assertNull(vm.estado.value.detalle)
        instrumentacion.runOnMainSync { vm.reintentar() }
        esperar(vm) { !it.cargando && it.detalle != null }
        assertNull(vm.estado.value.errorCarga)
        assertEquals(id, vm.estado.value.detalle!!.ticket.id)
    }

    @Test
    fun perdidaDeSesionDuranteEscrituraNoGuardaComentario(): Unit = runBlocking {
        val id = id("ABIERTO")
        var bloquear = false
        val inicio = CompletableDeferred<Unit>()
        val continuar = CompletableDeferred<Unit>()
        val lento = object : AyudaRepository by repo {
            override suspend fun obtenerUsuario(id: Int): Usuario? {
                if (bloquear) { inicio.complete(Unit); continuar.await() }
                return repo.obtenerUsuario(id)
            }
        }
        val vm = nuevo(id, lento)
        val cantidad = repo.observarTicket(id).first()!!.comentarios.size
        bloquear = true
        instrumentacion.runOnMainSync { vm.comentar("No debe guardarse sin sesión") }
        withTimeout(15_000) { inicio.await() }
        sesion.value = null
        continuar.complete(Unit)
        esperar(vm) { !it.operando && (it.error != null || it.errorCarga != null) }
        assertEquals(cantidad, repo.observarTicket(id).first()!!.comentarios.size)
        esperar(vm) { it.detalle == null && !it.cargando }
    }
}
