package com.tecsup.ayudacampus

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.ayudacampus.data.local.AyudaDatabase
import com.tecsup.ayudacampus.data.local.DatosIniciales
import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.data.repository.AyudaRepositoryRoom
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import com.tecsup.ayudacampus.viewmodel.BandejaViewModel.FiltroEstado
import com.tecsup.ayudacampus.viewmodel.BandejaViewModel.FiltroPrioridad
import com.tecsup.ayudacampus.viewmodel.PanelViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

@RunWith(AndroidJUnit4::class)
class PanelViewModelTest {
    private lateinit var db: AyudaDatabase
    private lateinit var repo: AyudaRepository
    private lateinit var sesion: MutableStateFlow<Usuario?>
    private val modelos = mutableListOf<PanelViewModel>()
    private fun main(accion: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(accion)

    @Before
    fun preparar(): Unit = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AyudaDatabase::class.java).build()
        DatosIniciales.sembrar(db)
        repo = AyudaRepositoryRoom(db)
        sesion = MutableStateFlow(requireNotNull(repo.login("soporte@campus.test", "admin")))
    }
    @After
    fun limpiar() {
        modelos.forEach { it.viewModelScope.cancel() }
        db.close()
    }
    private suspend fun nuevo(repository: AyudaRepository = repo, reloj: () -> Long = System::currentTimeMillis, intervalo: Long = 60_000): PanelViewModel {
        val vm = PanelViewModel(repository, sesion, reloj, intervalo)
        modelos.add(vm)
        withTimeout(15_000) { vm.cargando.first { !it } }
        return vm
    }
    private suspend fun datos(vm: PanelViewModel, condicion: (PanelViewModel.Indicadores) -> Boolean) =
        withTimeout(15_000) { vm.indicadores.first(condicion) }
    private suspend fun ids(vm: PanelViewModel, esperados: List<Int>) =
        withTimeout(15_000) { vm.tickets.first { lista -> lista.map { it.ticket.id } == esperados } }

    @Test
    fun indicadoresRealesGraficosYLeftJoinIncluyenCategoriasSinTickets(): Unit = runBlocking {
        val vm = nuevo()
        val indicadores = datos(vm) { it.porCategoria.size == 5 && it.abiertos == 2 }
        assertEquals(listOf(2, 2, 1, 1), indicadores.porEstado.map { it.cantidad })
        assertEquals(2, indicadores.enProceso)
        assertEquals(1, indicadores.resueltos)
        assertEquals(1, indicadores.cerrados)
        assertEquals(1, indicadores.altasSinAtender)
        assertEquals(mapOf("Hardware" to 1, "Software" to 2, "Red / Wi-Fi" to 1, "Cuenta" to 1, "Impresión" to 1), indicadores.porCategoria.associate { it.etiqueta to it.cantidad })
        repo.guardarCategoria(Categoria(nombre = "Accesorios", icono = "pc"))
        val actualizado = datos(vm) { it.porCategoria.any { c -> c.etiqueta == "Accesorios" } }
        assertEquals(0, actualizado.porCategoria.single { it.etiqueta == "Accesorios" }.cantidad)
        assertEquals(6, actualizado.porCategoria.sumOf { it.cantidad })
    }

    @Test
    fun busquedaPorTituloNumeroYEstudianteCombinaEstadoYPrioridad(): Unit = runBlocking {
        val vm = nuevo()
        val todos = repo.observarTodosLosTickets().first()
        ids(vm, todos.map { it.ticket.id })
        val wifi = todos.single { it.ticket.titulo == "Wi-Fi sin conexión" }.ticket.id
        val equipo = todos.single { it.ticket.titulo == "Equipo no enciende" }.ticket.id
        val cuenta = todos.single { it.ticket.titulo == "Cuenta de campus bloqueada" }.ticket.id
        main { vm.buscar("EQUIPO") }
        ids(vm, listOf(equipo))
        main { vm.buscar("#${cuenta.toString().padStart(4, '0')}") }
        ids(vm, listOf(cuenta))
        main { vm.buscar(" juAN ") }
        ids(vm, todos.map { it.ticket.id })
        main { vm.seleccionarEstado(FiltroEstado.ABIERTOS); vm.seleccionarPrioridad(FiltroPrioridad.ALTA) }
        ids(vm, listOf(wifi))
        main { vm.buscar("Juan"); vm.seleccionarEstado(FiltroEstado.CERRADOS) }
        ids(vm, emptyList())
        main { vm.seleccionarPrioridad(FiltroPrioridad.MEDIA) }
        ids(vm, listOf(cuenta))
        // Los indicadores nunca dependen de los filtros ni de la busqueda.
        val indicadores = datos(vm) { it.porEstado.size == 4 }
        assertEquals(listOf(2, 2, 1, 1), indicadores.porEstado.map { it.cantidad })
        main { vm.buscar(""); vm.seleccionarEstado(FiltroEstado.TODOS); vm.seleccionarPrioridad(FiltroPrioridad.TODAS) }
        ids(vm, todos.map { it.ticket.id })
        for (estado in FiltroEstado.entries.drop(1)) {
            main { vm.seleccionarEstado(estado) }
            val cantidad = if (estado in listOf(FiltroEstado.ABIERTOS, FiltroEstado.EN_PROCESO)) 2 else 1
            val lista = withTimeout(15_000) { vm.tickets.first { it.size == cantidad && it.all { d -> d.ticket.estado == estado.estado!!.name } } }
            assertEquals(cantidad, lista.size)
        }
        main { vm.seleccionarEstado(FiltroEstado.TODOS) }
        for (prioridad in FiltroPrioridad.entries.drop(1)) {
            main { vm.seleccionarPrioridad(prioridad) }
            withTimeout(15_000) { vm.tickets.first { it.size == 2 && it.all { d -> d.ticket.prioridad == prioridad.prioridad!!.name } } }
        }
    }

    @Test
    fun todosLosEstudiantesOrdenDelDaoYActualizacionDesdeRoom(): Unit = runBlocking {
        val vm = nuevo()
        val iniciales = repo.observarTodosLosTickets().first()
        ids(vm, iniciales.map { it.ticket.id })
        assertEquals(listOf("ALTA", "ALTA", "MEDIA", "MEDIA", "BAJA", "BAJA"), iniciales.map { it.ticket.prioridad })
        assertTrue(iniciales[0].ticket.fecha > iniciales[1].ticket.fecha)
        val otro = db.usuarioDao().insertarTodos(listOf(Usuario(nombre = "Lucía", correo = "lucia@test", clave = "1234", rol = "CLIENTE"))).single().toInt()
        val categoria = repo.observarCategorias().first().single { it.nombre == "Hardware" }.id
        val id = repo.crearTicket(otro, categoria, "Pantalla auxiliar sin imagen", "El monitor no recibe señal", "Aula 201", Prioridad.ALTA).toInt()
        val actualizados = withTimeout(15_000) { vm.todos.first { it.size == 7 } }
        assertEquals(id, actualizados.first().ticket.id)
        assertEquals("Lucía", actualizados.first().cliente.nombre)
        ids(vm, listOf(id) + iniciales.map { it.ticket.id })
        datos(vm) { it.abiertos == 3 && it.porCategoria.single { c -> c.etiqueta == "Hardware" }.cantidad == 2 }
        main { vm.buscar("LUCÍA") }
        ids(vm, listOf(id))
        repo.cambiarEstado(id, EstadoTicket.EN_PROCESO)
        datos(vm) { it.abiertos == 2 && it.enProceso == 3 }
        withTimeout(15_000) { vm.tickets.first { it.singleOrNull()?.ticket?.estado == "EN_PROCESO" } }
    }

    @Test
    fun alertaSeActualizaConElRelojSinModificarRoom(): Unit = runBlocking {
        val cliente = requireNotNull(repo.login("juan@campus.test", "1234"))
        val categoria = repo.observarCategorias().first().first().id
        repo.crearTicket(cliente.id, categoria, "Alta reciente", "Todavía no cumple una hora", "Biblioteca", Prioridad.ALTA)
        val reloj = AtomicLong(System.currentTimeMillis())
        val vm = nuevo(reloj = { reloj.get() }, intervalo = 100)
        datos(vm) { it.abiertos == 3 && it.altasSinAtender == 1 }
        reloj.addAndGet(3_600_100L)
        datos(vm) { it.altasSinAtender == 2 }
        assertEquals(7, repo.observarTodosLosTickets().first().size)
    }

    @Test
    fun bloqueaClienteSesionInvalidaYRolFalsificadoSinConsultarPanel(): Unit = runBlocking {
        val admin = requireNotNull(sesion.value)
        val juan = requireNotNull(repo.login("juan@campus.test", "1234"))
        var consultas = 0
        val vigilado = object : AyudaRepository by repo {
            override fun observarTodosLosTickets() = repo.observarTodosLosTickets().also { consultas++ }
            override fun observarCantidadPorEstado(estado: EstadoTicket) = repo.observarCantidadPorEstado(estado).also { consultas++ }
            override fun observarCantidadPorCategoria() = repo.observarCantidadPorCategoria().also { consultas++ }
            override fun observarAltasSinAtender(limite: Long) = repo.observarAltasSinAtender(limite).also { consultas++ }
        }
        for (invalido in listOf(null, juan, juan.copy(rol = "ADMIN"), admin.copy(id = Int.MAX_VALUE), admin.copy(id = 0))) {
            sesion.value = invalido
            val vm = nuevo(vigilado)
            assertNull(vm.administrador.value)
            assertTrue(vm.todos.value.isEmpty())
            assertTrue(vm.tickets.value.isEmpty())
            assertEquals(PanelViewModel.Indicadores(), vm.indicadores.value)
        }
        assertEquals(0, consultas)
        sesion.value = admin
        val vm = nuevo()
        withTimeout(15_000) { vm.todos.first { it.size == 6 } }
        datos(vm) { it.abiertos == 2 }
        sesion.value = juan
        withTimeout(15_000) { vm.administrador.first { it == null } }
        withTimeout(15_000) { vm.tickets.first { it.isEmpty() } }
        datos(vm) { it == PanelViewModel.Indicadores() }
    }

    @Test
    fun errorDeBandejaLimpiaDatosYPermiteReintentar(): Unit = runBlocking {
        var fallar = true
        val intermitente = object : AyudaRepository by repo {
            override fun observarTodosLosTickets() = flow {
                if (fallar) { fallar = false; throw IOException("Error de lectura") }
                emitAll(repo.observarTodosLosTickets())
            }
        }
        val vm = nuevo(intermitente)
        assertNotNull(vm.error.value)
        assertTrue(vm.todos.value.isEmpty())
        main { vm.reintentar() }
        withTimeout(15_000) { vm.todos.first { it.size == 6 } }
        datos(vm) { it.abiertos == 2 }
        assertNull(vm.error.value)
    }

    @Test
    fun errorDeIndicadoresNoInventaConteosYPermiteReintentar(): Unit = runBlocking {
        var fallar = true
        val intermitente = object : AyudaRepository by repo {
            override fun observarCantidadPorCategoria() = flow {
                if (fallar) { fallar = false; throw IOException("Error de categorias") }
                emitAll(repo.observarCantidadPorCategoria())
            }
        }
        val vm = nuevo(intermitente)
        withTimeout(15_000) { vm.errorIndicadores.first { it != null } }
        assertEquals(PanelViewModel.Indicadores(), vm.indicadores.value)
        assertEquals(6, vm.todos.value.size)
        main { vm.reintentar() }
        datos(vm) { it.abiertos == 2 && it.porCategoria.size == 5 }
        assertNull(vm.errorIndicadores.value)
    }
}
