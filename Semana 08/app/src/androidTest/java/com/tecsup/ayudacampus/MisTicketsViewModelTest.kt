package com.tecsup.ayudacampus

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.ayudacampus.data.local.AyudaDatabase
import com.tecsup.ayudacampus.data.local.DatosIniciales
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.data.repository.AyudaRepositoryRoom
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import com.tecsup.ayudacampus.viewmodel.MisTicketsViewModel
import com.tecsup.ayudacampus.viewmodel.MisTicketsViewModel.Filtro
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MisTicketsViewModelTest {
    private lateinit var db: AyudaDatabase
    private lateinit var repo: AyudaRepository
    private lateinit var vm: MisTicketsViewModel
    private var clienteId = 0

    @Before
    fun preparar() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AyudaDatabase::class.java).build()
        DatosIniciales.sembrar(db)
        repo = AyudaRepositoryRoom(db)
        clienteId = requireNotNull(repo.login("juan@campus.test", "1234")).id
        vm = MisTicketsViewModel(repo, clienteId)
        withTimeout(15_000) { vm.tickets.first { it.size == 6 } }
        Unit
    }

    @After
    fun limpiar() {
        vm.viewModelScope.cancel()
        db.close()
    }

    @Test
    fun seisTicketsFiltrosBusquedaYContador() = runBlocking {
        withTimeout(20_000) {
            val iniciales = vm.tickets.first { it.size == 6 }
            assertTrue(iniciales.any { it.ticket.estado == "CERRADO" })
            assertEquals(2, vm.cantidadAbiertos.first { it == 2 })
            vm.seleccionarFiltro(Filtro.ABIERTOS)
            val abiertos = vm.tickets.first { it.size == 2 && it.all { d -> d.ticket.estado == "ABIERTO" } }
            assertEquals(2, abiertos.size)
            vm.seleccionarFiltro(Filtro.EN_PROCESO)
            assertEquals(2, vm.tickets.first { it.size == 2 && it.all { d -> d.ticket.estado == "EN_PROCESO" } }.size)
            vm.seleccionarFiltro(Filtro.RESUELTOS)
            assertEquals("RESUELTO", vm.tickets.first { it.size == 1 }.single().ticket.estado)
            vm.buscar("wi-fi")
            assertTrue(vm.tickets.first { it.isEmpty() }.isEmpty())
            vm.seleccionarFiltro(Filtro.TODOS)
            val wifi = vm.tickets.first { it.size == 1 }.single()
            assertEquals("Wi-Fi sin conexión", wifi.ticket.titulo)
            vm.buscar("  CUENTA  ")
            assertEquals("CERRADO", vm.tickets.first { it.singleOrNull()?.ticket?.titulo?.contains("Cuenta") == true }.single().ticket.estado)
            vm.buscar("#${wifi.ticket.id.toString().padStart(4, '0')}")
            assertEquals(wifi.ticket.id, vm.tickets.first { it.singleOrNull()?.ticket?.id == wifi.ticket.id }.single().ticket.id)
            vm.buscar(wifi.ticket.id.toString())
            assertEquals(wifi.ticket.id, vm.tickets.first { it.singleOrNull()?.ticket?.id == wifi.ticket.id }.single().ticket.id)
            assertEquals(2, vm.cantidadAbiertos.value)
            vm.buscar("")
            assertEquals(6, vm.tickets.first { it.size == 6 }.size)
        }
    }

    @Test
    fun excluyeOtrosClientesYActualizaDesdeRoom() = runBlocking {
        withTimeout(20_000) {
            val iniciales = vm.tickets.first { it.size == 6 }
            val categoriaId = iniciales.first().categoria.id
            val otro = db.usuarioDao().insertarTodos(listOf(
                Usuario(nombre = "Otro estudiante", correo = "otro@test", clave = "1234", rol = "CLIENTE")
            )).single().toInt()
            val ajeno = repo.crearTicket(otro, categoriaId, "Ticket privado ajeno", "Solo para el otro estudiante", "Aula", Prioridad.ALTA).toInt()
            val otroVm = MisTicketsViewModel(repo, otro)
            try {
                assertEquals(ajeno, otroVm.tickets.first { it.size == 1 }.single().ticket.id)
                vm.buscar("privado ajeno")
                assertTrue(vm.tickets.first { it.isEmpty() }.isEmpty())
                vm.buscar("")
                val nuevo = repo.crearTicket(clienteId, categoriaId, "Nuevo propio desde Room", "Cambio recibido en tiempo real", "Aula", Prioridad.MEDIA).toInt()
                val lista = vm.tickets.first { it.size == 7 }
                assertTrue(lista.all { it.ticket.clienteId == clienteId })
                assertFalse(lista.any { it.ticket.id == ajeno })
                assertEquals(3, vm.cantidadAbiertos.first { it == 3 })
                vm.seleccionarFiltro(Filtro.ABIERTOS)
                vm.tickets.first { it.size == 3 }
                repo.cambiarEstado(nuevo, EstadoTicket.RESUELTO)
                val filtrados = vm.tickets.first { it.size == 2 }
                assertFalse(filtrados.any { it.ticket.id == nuevo })
                assertEquals(2, vm.cantidadAbiertos.first { it == 2 })
            } finally {
                otroVm.viewModelScope.cancel()
            }
        }
    }
}
