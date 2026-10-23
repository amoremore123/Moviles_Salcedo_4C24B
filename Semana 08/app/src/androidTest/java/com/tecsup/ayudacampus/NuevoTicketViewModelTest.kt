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
import com.tecsup.ayudacampus.model.Prioridad
import com.tecsup.ayudacampus.viewmodel.MisTicketsViewModel
import com.tecsup.ayudacampus.viewmodel.NuevoTicketViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class NuevoTicketViewModelTest {
    private lateinit var db: AyudaDatabase
    private lateinit var repo: AyudaRepository
    private lateinit var sesion: MutableStateFlow<Usuario?>
    private val modelos = mutableListOf<NuevoTicketViewModel>()

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

    private suspend fun nuevo(repository: AyudaRepository = repo): NuevoTicketViewModel {
        val vm = NuevoTicketViewModel(repository, sesion)
        modelos.add(vm)
        withTimeout(15_000) { vm.categorias.first { it.size == 5 } }
        return vm
    }

    private fun completar(vm: NuevoTicketViewModel) {
        vm.seleccionarCategoria(vm.categorias.value.first { it.nombre == "Red / Wi-Fi" }.id)
        vm.actualizarTitulo("Wi-Fi en aula 108")
        vm.actualizarDescripcion("No hay conexión durante la clase de programación.")
        vm.actualizarUbicacion("Aula 108")
    }

    private suspend fun enviar(vm: NuevoTicketViewModel) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { vm.enviar() }
        withTimeout(15_000) { vm.guardando.first { !it } }
    }

    @Test
    fun validaCamposYCategoriasActivas() = runBlocking {
        val vm = nuevo()
        assertEquals(Prioridad.MEDIA, vm.formulario.value.prioridad)
        enviar(vm)
        assertNotNull(vm.errores.value.categoria)
        assertNotNull(vm.errores.value.titulo)
        assertNotNull(vm.errores.value.descripcion)
        assertNotNull(vm.errores.value.ubicacion)
        completar(vm)
        vm.actualizarTitulo("ab")
        vm.actualizarDescripcion("corta")
        enviar(vm)
        assertNotNull(vm.errores.value.titulo)
        assertNotNull(vm.errores.value.descripcion)
        completar(vm)
        val id = requireNotNull(vm.formulario.value.categoriaId)
        repo.cambiarActiva(id, false)
        withTimeout(15_000) { vm.categorias.first { lista -> lista.none { it.id == id } } }
        enviar(vm)
        assertNotNull(vm.errores.value.categoria)
        assertNull(vm.ticketCreado.value)
        assertEquals(6, repo.observarTodosLosTickets().first().size)
    }

    @Test
    fun guardaEnRoomUnaVezYActualizaMisTickets() = runBlocking {
        val vm = nuevo()
        completar(vm)
        vm.seleccionarPrioridad(Prioridad.ALTA)
        val usuario = requireNotNull(sesion.value)
        val listaVm = MisTicketsViewModel(repo, usuario.id)
        try {
            withTimeout(15_000) { listaVm.tickets.first { it.size == 6 } }
            val antes = System.currentTimeMillis()
            InstrumentationRegistry.getInstrumentation().runOnMainSync { vm.enviar(); vm.enviar() }
            val id = withTimeout(15_000) { vm.ticketCreado.first { it != null } }!!.toInt()
            InstrumentationRegistry.getInstrumentation().runOnMainSync { vm.enviar() }
            val lista = withTimeout(15_000) { listaVm.tickets.first { it.size == 7 } }
            val ticket = lista.first { it.ticket.id == id }.ticket
            assertEquals(usuario.id, ticket.clienteId)
            assertEquals("ABIERTO", ticket.estado)
            assertEquals("ALTA", ticket.prioridad)
            assertEquals(vm.formulario.value.titulo, ticket.titulo)
            assertEquals(vm.formulario.value.descripcion, ticket.descripcion)
            assertEquals(vm.formulario.value.ubicacion, ticket.ubicacion)
            assertTrue(ticket.fecha in antes..System.currentTimeMillis())
            assertEquals(1, lista.count { it.ticket.titulo == ticket.titulo })
            assertEquals(id, lista.first().ticket.id)
            assertEquals(3, withTimeout(15_000) { listaVm.cantidadAbiertos.first { it == 3 } })
        } finally {
            listaVm.viewModelScope.cancel()
        }
    }

    @Test
    fun conservaDatosTrasErrorYPermiteReintento() = runBlocking {
        var llamadas = 0
        val intermitente = object : AyudaRepository by repo {
            override suspend fun crearTicket(clienteId: Int, categoriaId: Int, titulo: String, descripcion: String, ubicacion: String, prioridad: Prioridad): Long {
                llamadas++
                if (llamadas == 1) throw IOException("Fallo simulado antes de insertar")
                return repo.crearTicket(clienteId, categoriaId, titulo, descripcion, ubicacion, prioridad)
            }
        }
        val vm = nuevo(intermitente)
        completar(vm)
        val datos = vm.formulario.value
        enviar(vm)
        assertNotNull(vm.error.value)
        assertEquals(datos, vm.formulario.value)
        assertNull(vm.ticketCreado.value)
        assertEquals(6, repo.observarTodosLosTickets().first().size)
        enviar(vm)
        assertNotNull(vm.ticketCreado.value)
        assertNull(vm.error.value)
        assertEquals(2, llamadas)
        assertEquals(7, repo.observarTodosLosTickets().first().size)
    }

    @Test
    fun rechazaSesionInvalidaYPerdidaDeSesionDuranteElEnvio() = runBlocking {
        val vm = nuevo()
        completar(vm)
        val juan = requireNotNull(sesion.value)
        for (invalida in listOf(null, repo.login("soporte@campus.test", "admin"), juan.copy(id = Int.MAX_VALUE))) {
            sesion.value = invalida
            enviar(vm)
            assertNotNull(vm.error.value)
            assertNull(vm.ticketCreado.value)
        }
        sesion.value = juan
        val inicio = CompletableDeferred<Unit>()
        val continuar = CompletableDeferred<Unit>()
        val lento = object : AyudaRepository by repo {
            override suspend fun obtenerUsuario(id: Int): Usuario? {
                inicio.complete(Unit)
                continuar.await()
                return repo.obtenerUsuario(id)
            }
        }
        val otroVm = nuevo(lento)
        completar(otroVm)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { otroVm.enviar() }
        withTimeout(15_000) { inicio.await() }
        sesion.value = null
        continuar.complete(Unit)
        withTimeout(15_000) { otroVm.guardando.first { !it } }
        assertNotNull(otroVm.error.value)
        assertNull(otroVm.ticketCreado.value)
        assertEquals(6, repo.observarTodosLosTickets().first().size)
    }
}
