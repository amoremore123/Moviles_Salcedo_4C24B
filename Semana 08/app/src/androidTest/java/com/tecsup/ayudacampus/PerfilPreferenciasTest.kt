package com.tecsup.ayudacampus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.ayudacampus.data.local.DatosIniciales
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.model.Prioridad
import com.tecsup.ayudacampus.viewmodel.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerfilPreferenciasTest {
    private lateinit var app: AyudaApp
    private lateinit var juan: Usuario
    private lateinit var admin: Usuario
    private lateinit var sesion: MutableStateFlow<Usuario?>
    private val modelos = mutableListOf<ViewModel>()
    private var oscuro = false; private var avisos = true; private var recibir = true
    private fun main(accion: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(accion)
    @Before
    fun preparar(): Unit = runBlocking {
        app = ApplicationProvider.getApplicationContext()
        DatosIniciales.sembrar(app.db)
        app.preferencias.cerrarSesion()
        oscuro = app.preferencias.modoOscuro.first(); avisos = app.preferencias.notificaciones.first(); recibir = app.preferencias.recibirTickets.first()
        juan = requireNotNull(app.repository.login("juan@campus.test", "1234"))
        admin = requireNotNull(app.repository.login("soporte@campus.test", "admin"))
        sesion = MutableStateFlow(juan)
    }
    @After
    fun limpiar(): Unit = runBlocking {
        modelos.forEach { it.viewModelScope.cancel() }
        app.preferencias.cerrarSesion()
        app.preferencias.guardarModoOscuro(oscuro); app.preferencias.guardarNotificaciones(avisos); app.preferencias.guardarRecibirTickets(recibir)
    }
    private suspend fun perfil(repo: AyudaRepository = app.repository): PerfilViewModel {
        val vm = PerfilViewModel(repo, app.preferencias, sesion); modelos.add(vm)
        withTimeout(15_000) { vm.perfil.first { !it.cargando } }
        withTimeout(15_000) { vm.ajustes.first { it != null } }
        return vm
    }
    private suspend fun guardar(vm: PerfilViewModel, accion: () -> Unit) {
        main(accion); withTimeout(15_000) { vm.guardando.first { !it } }
    }
    private suspend fun sesion(repo: AyudaRepository = app.repository): SesionViewModel {
        val vm = SesionViewModel(repo, app.preferencias); modelos.add(vm)
        withTimeout(15_000) { vm.recuperandoSesion.first { !it } }; return vm
    }
    @Test
    fun perfilMuestraUsuarioRealYContadoresQueSeActualizanDesdeRoom(): Unit = runBlocking {
        val vm = perfil()
        assertEquals(juan, vm.perfil.value.usuario)
        val tickets = app.repository.observarTicketsCliente(juan.id).first()
        assertEquals(tickets.size, vm.perfil.value.total)
        assertEquals(tickets.count { it.ticket.estado == "ABIERTO" }, vm.perfil.value.abiertos)
        assertEquals(tickets.count { it.ticket.estado == "RESUELTO" }, vm.perfil.value.resueltos)
        val categoria = app.repository.observarCategoriasActivas().first().first()
        val id = app.repository.crearTicket(juan.id, categoria.id, "Prueba perfil", "Contador desde Room", "Laboratorio", Prioridad.MEDIA)
        try {
            withTimeout(15_000) { vm.perfil.first { it.total == tickets.size + 1 } }
            assertEquals(tickets.count { it.ticket.estado == "ABIERTO" } + 1, vm.perfil.value.abiertos)
            sesion.value = admin
            withTimeout(15_000) { vm.perfil.first { it.usuario?.id == admin.id } }
            assertEquals(0, vm.perfil.value.total)
        } finally { app.db.openHelper.writableDatabase.execSQL("DELETE FROM tickets WHERE id = ?", arrayOf(id)) }
    }
    @Test
    fun preferenciasPersistenYSeObservanSinAlterarDatosRoom(): Unit = runBlocking {
        val vm = perfil()
        val antes = app.repository.observarTodosLosTickets().first()
        guardar(vm) { vm.guardarModoOscuro(!oscuro) }
        guardar(vm) { vm.guardarNotificaciones(!avisos) }
        assertEquals(!oscuro, app.preferencias.modoOscuro.first())
        assertEquals(!avisos, app.preferencias.notificaciones.first())
        val nuevo = perfil()
        withTimeout(15_000) { nuevo.ajustes.first { it?.modoOscuro == !oscuro && it.notificaciones == !avisos } }
        assertEquals(antes, app.repository.observarTodosLosTickets().first())
    }
    @Test
    fun recibirTicketsRequiereAdminRealYSesionValida(): Unit = runBlocking {
        app.preferencias.guardarRecibirTickets(true)
        val vm = perfil()
        guardar(vm) { vm.guardarRecibirTickets(false) }
        assertNotNull(vm.error.value); assertTrue(app.preferencias.recibirTickets.first())
        sesion.value = juan.copy(rol = "ADMIN")
        guardar(vm) { vm.guardarRecibirTickets(false) }
        assertTrue(app.preferencias.recibirTickets.first())
        sesion.value = admin
        withTimeout(15_000) { vm.perfil.first { it.usuario?.id == admin.id } }
        guardar(vm) { vm.guardarRecibirTickets(false) }
        assertNull(vm.error.value); assertFalse(app.preferencias.recibirTickets.first())
        sesion.value = null
        withTimeout(15_000) { vm.perfil.first { !it.cargando && it.usuario == null } }
        guardar(vm) { vm.guardarNotificaciones(!avisos) }
        assertNotNull(vm.error.value); assertEquals(avisos, app.preferencias.notificaciones.first())
    }
    @Test
    fun mesaCerradaBloqueaFormularioYaAbiertoYReabrirPermiteGuardarUnaVez(): Unit = runBlocking {
        app.preferencias.guardarRecibirTickets(true)
        val vm = NuevoTicketViewModel(app.repository, sesion, app.preferencias.recibirTickets); modelos.add(vm)
        withTimeout(15_000) { vm.cargandoCategorias.first { !it } }
        val categoria = app.repository.observarCategoriasActivas().first().first()
        main { vm.seleccionarCategoria(categoria.id); vm.actualizarTitulo("Mesa de prueba"); vm.actualizarDescripcion("Formulario abierto antes del cierre"); vm.actualizarUbicacion("Aula 1") }
        val antes = app.repository.observarTodosLosTickets().first().size
        app.preferencias.guardarRecibirTickets(false)
        main { vm.enviar() }; withTimeout(15_000) { vm.guardando.first { !it } }
        assertNull(vm.ticketCreado.value); assertTrue(vm.error.value!!.contains("Mesa de ayuda cerrada"))
        assertEquals(antes, app.repository.observarTodosLosTickets().first().size)
        assertEquals("Mesa de prueba", vm.formulario.value.titulo)
        app.preferencias.guardarRecibirTickets(true)
        main { vm.enviar(); vm.enviar() }; withTimeout(15_000) { vm.guardando.first { !it } }
        val id = requireNotNull(vm.ticketCreado.value)
        try { assertEquals(antes + 1, app.repository.observarTodosLosTickets().first().size) }
        finally { app.db.openHelper.writableDatabase.execSQL("DELETE FROM tickets WHERE id = ?", arrayOf(id)) }
    }
    @Test
    fun cambioPracticaUsaIdsRealesRestauraSesionYCierreConservaTresPreferencias(): Unit = runBlocking {
        app.preferencias.guardarSesion(juan.rol, juan.id)
        app.preferencias.guardarModoOscuro(true); app.preferencias.guardarNotificaciones(false); app.preferencias.guardarRecibirTickets(false)
        val vm = sesion()
        main { vm.cambiarPerfil(); vm.cambiarPerfil() }; withTimeout(15_000) { vm.cargando.first { !it } }
        assertNull(vm.error.value); assertEquals(admin, vm.usuarioActual.value)
        assertEquals(admin.id, app.preferencias.usuarioId.first()); assertEquals("ADMIN", app.preferencias.perfilActivo.first())
        assertEquals(admin, sesion().usuarioActual.value)
        main { vm.cambiarPerfil() }; withTimeout(15_000) { vm.cargando.first { !it } }
        assertEquals(juan, vm.usuarioActual.value)
        main { vm.cerrarSesion() }; withTimeout(15_000) { vm.cargando.first { !it } }
        assertNull(vm.usuarioActual.value); assertEquals(0, app.preferencias.usuarioId.first()); assertEquals("", app.preferencias.perfilActivo.first())
        assertTrue(app.preferencias.modoOscuro.first()); assertFalse(app.preferencias.notificaciones.first()); assertFalse(app.preferencias.recibirTickets.first())
    }
    @Test
    fun cambioPracticaFalloOSesionAusenteNoCreaSesionFalsa(): Unit = runBlocking {
        val vigilado = object : AyudaRepository by app.repository {
            override suspend fun obtenerUsuarioPorRol(rol: String): Usuario? = null
        }
        val vacio = sesion(vigilado)
        main { vacio.cambiarPerfil() }; assertNull(vacio.usuarioActual.value)
        app.preferencias.guardarSesion(juan.rol, juan.id)
        val vm = sesion(vigilado)
        main { vm.cambiarPerfil() }; withTimeout(15_000) { vm.cargando.first { !it } }
        assertNotNull(vm.error.value); assertEquals(juan, vm.usuarioActual.value)
        assertEquals(juan.id, app.preferencias.usuarioId.first()); assertEquals("CLIENTE", app.preferencias.perfilActivo.first())
    }
}
