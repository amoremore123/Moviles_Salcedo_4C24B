package com.tecsup.ayudacampus

import androidx.lifecycle.ViewModel
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
import com.tecsup.ayudacampus.viewmodel.*
import kotlinx.coroutines.CompletableDeferred
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

@RunWith(AndroidJUnit4::class)
class GestionSoporteViewModelTest {
    private lateinit var db: AyudaDatabase
    private lateinit var repo: AyudaRepository
    private lateinit var admin: Usuario
    private lateinit var juan: Usuario
    private lateinit var sesion: MutableStateFlow<Usuario?>
    private var abierto = 0
    private val modelos = mutableListOf<ViewModel>()
    private fun main(accion: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(accion)
    @Before
    fun preparar(): Unit = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AyudaDatabase::class.java).build()
        DatosIniciales.sembrar(db)
        repo = AyudaRepositoryRoom(db)
        admin = requireNotNull(repo.login("soporte@campus.test", "admin"))
        juan = requireNotNull(repo.login("juan@campus.test", "1234"))
        sesion = MutableStateFlow(admin)
        abierto = repo.observarTodosLosTickets().first().first { it.ticket.estado == "ABIERTO" }.ticket.id
    }
    @After
    fun limpiar() { modelos.forEach { it.viewModelScope.cancel() }; db.close() }
    private suspend fun atender(id: Int = abierto, repository: AyudaRepository = repo): AtenderViewModel {
        val vm = AtenderViewModel(id, repository, sesion); modelos.add(vm)
        withTimeout(15_000) { vm.pantalla.first { !it.cargando } }
        return vm
    }
    private suspend fun categorias(id: Int? = null, repository: AyudaRepository = repo): CategoriasViewModel {
        val vm = CategoriasViewModel(repository, sesion, id); modelos.add(vm)
        withTimeout(15_000) { vm.pantalla.first { !it.cargando } }
        return vm
    }
    private suspend fun accion(vm: AtenderViewModel, bloque: () -> Unit) {
        main(bloque); withTimeout(15_000) { vm.pantalla.first { !it.operando } }
    }
    private suspend fun accion(vm: CategoriasViewModel, bloque: () -> Unit) {
        main(bloque); withTimeout(15_000) { vm.pantalla.first { !it.operando } }
    }

    @Test
    fun respuestaAdminValidaAutorNoDuplicaYActualizaAlEstudiante(): Unit = runBlocking {
        val inicio = CompletableDeferred<Unit>(); val continuar = CompletableDeferred<Unit>()
        val controlado = object : AyudaRepository by repo {
            override suspend fun agregarComentario(ticketId: Int, autor: Usuario, texto: String) {
                inicio.complete(Unit); continuar.await(); repo.agregarComentario(ticketId, autor, texto)
            }
        }
        val vm = atender(repository = controlado)
        val inicial = requireNotNull(vm.pantalla.value.detalle)
        assertEquals(juan.id, inicial.cliente.id)
        assertFalse(inicial.ticket.descripcion.isBlank())
        assertEquals("Red / Wi-Fi", inicial.categoria.nombre)
        val estudiante = DetalleTicketViewModel(abierto, repo, MutableStateFlow(juan)); modelos.add(estudiante)
        withTimeout(15_000) { estudiante.estado.first { it.detalle != null } }
        accion(vm) { vm.responder("  ") }
        assertNotNull(vm.pantalla.value.error)
        main { vm.actualizarRespuesta("  Revisamos el equipo de red  "); vm.responder(); vm.responder() }
        withTimeout(15_000) { inicio.await() }; continuar.complete(Unit)
        withTimeout(15_000) { vm.pantalla.first { !it.operando && it.confirmacion != null } }
        val recibido = withTimeout(15_000) { estudiante.estado.first { it.detalle?.comentarios?.size == inicial.comentarios.size + 1 } }
        val comentario = recibido.detalle!!.comentarios.last()
        assertEquals(admin.id, comentario.autorId)
        assertEquals(admin.nombre, comentario.autorNombre)
        assertEquals("ADMIN", comentario.autorRol)
        assertEquals("Revisamos el equipo de red", comentario.texto)
        assertEquals("", vm.pantalla.value.respuesta)
    }

    @Test
    fun guardarSeleccionEstadoYPrioridadActualizaPanelYPermiteReabrirResuelto(): Unit = runBlocking {
        val vm = atender()
        val panel = PanelViewModel(repo, sesion); modelos.add(panel)
        withTimeout(15_000) { panel.indicadores.first { it.abiertos == 2 } }
        main { vm.cambiarEstado(EstadoTicket.EN_PROCESO); vm.cambiarPrioridad(Prioridad.MEDIA) }
        assertEquals("ABIERTO", repo.observarTicket(abierto).first()!!.ticket.estado)
        assertEquals("ALTA", repo.observarTicket(abierto).first()!!.ticket.prioridad)
        accion(vm) { vm.guardar() }
        val guardado = repo.observarTicket(abierto).first()!!.ticket
        assertEquals("EN_PROCESO", guardado.estado)
        assertEquals("MEDIA", guardado.prioridad)
        withTimeout(15_000) { panel.indicadores.first { it.abiertos == 1 && it.enProceso == 3 } }
        withTimeout(15_000) { panel.todos.first { it.firstOrNull { d -> d.ticket.id == abierto }?.ticket?.prioridad == "MEDIA" } }
        accion(vm) { vm.cambiarEstado(EstadoTicket.RESUELTO); vm.guardar() }
        assertEquals("RESUELTO", repo.observarTicket(abierto).first()!!.ticket.estado)
        accion(vm) { vm.cambiarEstado(EstadoTicket.ABIERTO); vm.guardar() }
        val reabierto = repo.observarTicket(abierto).first()!!.ticket
        assertEquals("ABIERTO", reabierto.estado)
        assertNull(reabierto.calificacion); assertNull(reabierto.fechaCierre)
        withTimeout(15_000) { panel.indicadores.first { it.abiertos == 2 && it.enProceso == 2 } }
    }

    @Test
    fun noPermiteCerrarNiModificarTicketsCerradosNiPisarCierreDelEstudiante(): Unit = runBlocking {
        val vm = atender()
        accion(vm) { vm.cambiarEstado(EstadoTicket.CERRADO); vm.guardar() }
        assertEquals("ABIERTO", repo.observarTicket(abierto).first()!!.ticket.estado)
        assertNull(repo.observarTicket(abierto).first()!!.ticket.calificacion)
        val cerrado = repo.observarTodosLosTickets().first().first { it.ticket.estado == "CERRADO" }
        val lectura = atender(cerrado.ticket.id)
        accion(lectura) { lectura.cambiarEstado(EstadoTicket.ABIERTO); lectura.cambiarPrioridad(Prioridad.ALTA); lectura.guardar() }
        accion(lectura) { lectura.responder("No se debe registrar") }
        assertNotNull(lectura.pantalla.value.error)
        assertEquals(cerrado, repo.observarTicket(cerrado.ticket.id).first())
        main { vm.cambiarEstado(EstadoTicket.RESUELTO); vm.cambiarPrioridad(Prioridad.BAJA) }
        repo.cerrarConCalificacion(abierto, 4)
        val cierre = repo.observarTicket(abierto).first()!!.ticket
        accion(vm) { vm.guardar() }
        assertNotNull(vm.pantalla.value.error)
        assertEquals(cierre, repo.observarTicket(abierto).first()!!.ticket)
        assertEquals(4, cierre.calificacion)
    }

    @Test
    fun atenderBloqueaClientesRolesFalsosYSesionPerdidaAntesDeEscribir(): Unit = runBlocking {
        var lecturas = 0
        val vigilado = object : AyudaRepository by repo {
            override fun observarTicket(id: Int) = repo.observarTicket(id).also { lecturas++ }
        }
        for (usuario in listOf(null, juan, juan.copy(rol = "ADMIN"), admin.copy(id = Int.MAX_VALUE))) {
            sesion.value = usuario
            val vm = atender(repository = vigilado)
            assertNull(vm.pantalla.value.detalle)
            accion(vm) { vm.responder("Intento sin permisos") }
            accion(vm) { vm.cambiarEstado(EstadoTicket.RESUELTO); vm.guardar() }
            assertNotNull(vm.pantalla.value.error)
        }
        assertEquals(0, lecturas)
        sesion.value = admin
        var bloquear = false
        val inicio = CompletableDeferred<Unit>(); val continuar = CompletableDeferred<Unit>()
        val lento = object : AyudaRepository by repo {
            override suspend fun obtenerUsuario(id: Int): Usuario? {
                if (bloquear) { inicio.complete(Unit); continuar.await() }
                return repo.obtenerUsuario(id)
            }
        }
        val vm = atender(repository = lento)
        val cantidad = repo.observarTicket(abierto).first()!!.comentarios.size
        bloquear = true
        main { vm.responder("No debe persistir tras perder la sesión") }
        withTimeout(15_000) { inicio.await() }; sesion.value = null; continuar.complete(Unit)
        withTimeout(15_000) { vm.pantalla.first { !it.operando && (it.error != null || it.errorCarga != null) } }
        assertEquals(cantidad, repo.observarTicket(abierto).first()!!.comentarios.size)
        withTimeout(15_000) { vm.pantalla.first { it.detalle == null && !it.cargando } }
    }

    @Test
    fun conservaRespuestaYSeleccionTrasErrorYReintentaSinDuplicarCambios(): Unit = runBlocking {
        var respuestas = 0; var cambiosEstado = 0; var cambiosPrioridad = 0
        val intermitente = object : AyudaRepository by repo {
            override suspend fun agregarComentario(ticketId: Int, autor: Usuario, texto: String) {
                respuestas++; if (respuestas == 1) throw IOException("Error previo a insertar")
                repo.agregarComentario(ticketId, autor, texto)
            }
            override suspend fun cambiarPrioridad(ticketId: Int, prioridad: Prioridad) { cambiosPrioridad++; repo.cambiarPrioridad(ticketId, prioridad) }
            override suspend fun cambiarEstado(ticketId: Int, estado: EstadoTicket) {
                cambiosEstado++; if (cambiosEstado == 1) throw IOException("Error previo al estado")
                repo.cambiarEstado(ticketId, estado)
            }
        }
        val vm = atender(repository = intermitente)
        main { vm.actualizarRespuesta("Respuesta que se conserva") }
        accion(vm) { vm.responder() }; assertNotNull(vm.pantalla.value.error)
        assertEquals("Respuesta que se conserva", vm.pantalla.value.respuesta)
        accion(vm) { vm.responder() }
        assertEquals(1, repo.observarTicket(abierto).first()!!.comentarios.count { it.texto == "Respuesta que se conserva" })
        main { vm.cambiarEstado(EstadoTicket.EN_PROCESO); vm.cambiarPrioridad(Prioridad.MEDIA) }
        accion(vm) { vm.guardar() }; assertNotNull(vm.pantalla.value.error)
        assertEquals("MEDIA", repo.observarTicket(abierto).first()!!.ticket.prioridad)
        assertEquals("ABIERTO", repo.observarTicket(abierto).first()!!.ticket.estado)
        accion(vm) { vm.guardar() }
        assertEquals("EN_PROCESO", repo.observarTicket(abierto).first()!!.ticket.estado)
        assertEquals(1, cambiosPrioridad); assertEquals(2, cambiosEstado)
    }

    @Test
    fun ticketInexistenteYFalloDeLecturaSeMuestranSinDatosYPermitenReintentar(): Unit = runBlocking {
        for (id in listOf(-1, Int.MAX_VALUE)) {
            val vm = atender(id)
            assertNull(vm.pantalla.value.detalle); assertNotNull(vm.pantalla.value.errorCarga)
            accion(vm) { vm.responder("No existe") }
            assertNotNull(vm.pantalla.value.error)
        }
        var fallar = true
        val intermitente = object : AyudaRepository by repo {
            override fun observarTicket(id: Int) = flow {
                if (fallar) { fallar = false; throw IOException("Fallo de lectura") }
                emitAll(repo.observarTicket(id))
            }
        }
        val vm = atender(repository = intermitente)
        assertNotNull(vm.pantalla.value.errorCarga)
        main { vm.reintentar() }
        withTimeout(15_000) { vm.pantalla.first { it.detalle != null && !it.cargando } }
    }

    @Test
    fun categoriasValidaNombresDuplicadosIconosEIdsAntesDeGuardar(): Unit = runBlocking {
        val vm = categorias()
        for (categoria in listOf(Categoria(nombre = "  ", icono = "pc"), Categoria(nombre = " HARDWARE ", icono = "pc"),
            Categoria(nombre = "Red /    Wi-Fi", icono = "wifi"), Categoria(nombre = "Nuevo", icono = "no-existe"),
            Categoria(id = -1, nombre = "Nuevo", icono = "pc"), Categoria(id = Int.MAX_VALUE, nombre = "Nuevo", icono = "pc"))) {
            accion(vm) { vm.guardar(categoria) }; assertNotNull(vm.pantalla.value.error)
            assertEquals(5, repo.observarCategorias().first().size)
        }
        accion(vm) { vm.guardar(Categoria(nombre = "  Audio   Visual  ", icono = "tag")) }
        assertNull(vm.pantalla.value.error)
        val nueva = repo.observarCategorias().first().single { it.nombre == "Audio Visual" }
        assertTrue(nueva.id > 0)
        accion(vm) { vm.guardar(nueva.copy(nombre = " SOFTWARE ")) }
        assertNotNull(vm.pantalla.value.error)
        assertEquals("Audio Visual", repo.observarCategorias().first().single { it.id == nueva.id }.nombre)
    }

    @Test
    fun editaSinClonarEInactivaSoloParaNuevosTicketsManteniendoRelaciones(): Unit = runBlocking {
        val hardware = repo.observarCategorias().first().single { it.nombre == "Hardware" }
        val vm = categorias(hardware.id)
        assertEquals(hardware, vm.pantalla.value.formulario)
        val nuevoTicket = NuevoTicketViewModel(repo, MutableStateFlow(juan)); modelos.add(nuevoTicket)
        withTimeout(15_000) { nuevoTicket.categorias.first { it.size == 5 } }
        val idTicket = repo.observarTodosLosTickets().first().first { it.ticket.categoriaId == hardware.id }.ticket.id
        val detalle = DetalleTicketViewModel(idTicket, repo, MutableStateFlow(juan)); modelos.add(detalle)
        withTimeout(15_000) { detalle.estado.first { it.detalle != null } }
        main { vm.actualizarNombre("Equipos del laboratorio"); vm.seleccionarIcono("pc"); vm.establecerActiva(false) }
        accion(vm) { vm.guardar() }
        val editada = repo.observarCategorias().first().single { it.id == hardware.id }
        assertEquals("Equipos del laboratorio", editada.nombre)
        assertFalse(editada.activa); assertEquals(5, repo.observarCategorias().first().size)
        withTimeout(15_000) { nuevoTicket.categorias.first { lista -> lista.none { it.id == hardware.id } } }
        withTimeout(15_000) { detalle.estado.first { it.detalle?.categoria?.nombre == editada.nombre && it.detalle?.categoria?.activa == false } }
        assertEquals(hardware.id, repo.observarTicket(idTicket).first()!!.ticket.categoriaId)
        accion(vm) { vm.cambiarActiva(editada, true) }
        withTimeout(15_000) { nuevoTicket.categorias.first { lista -> lista.any { it.id == hardware.id } } }
    }

    @Test
    fun protegeCategoriasUsadasYLaClaveForaneaTambienAnteUsoConcurrente(): Unit = runBlocking {
        val vm = categorias()
        val usada = repo.observarCategorias().first().single { it.nombre == "Hardware" }
        accion(vm) { vm.eliminar(usada) }
        assertTrue(vm.pantalla.value.error!!.contains("tiene tickets"))
        accion(vm) { vm.cambiarActiva(usada, false) }
        accion(vm) { vm.eliminar(usada) }
        assertTrue(repo.observarCategorias().first().any { it.id == usada.id })
        repo.guardarCategoria(Categoria(nombre = "Temporal", icono = "tag"))
        val temporal = repo.observarCategorias().first().single { it.nombre == "Temporal" }
        accion(vm) { vm.eliminar(temporal) }
        assertFalse(repo.observarCategorias().first().any { it.id == temporal.id })
        repo.guardarCategoria(Categoria(nombre = "Uso concurrente", icono = "pc"))
        val concurrente = repo.observarCategorias().first().single { it.nombre == "Uso concurrente" }
        val carrera = object : AyudaRepository by repo {
            override suspend fun eliminarCategoria(c: Categoria) {
                repo.crearTicket(juan.id, c.id, "Ticket nuevo", "Creado antes de eliminar", "Aula", Prioridad.MEDIA)
                repo.eliminarCategoria(c)
            }
        }
        val protegido = categorias(repository = carrera)
        accion(protegido) { protegido.eliminar(concurrente) }
        assertNotNull(protegido.pantalla.value.error)
        assertTrue(repo.observarCategorias().first().any { it.id == concurrente.id })
        assertNotNull(repo.observarTodosLosTickets().first().firstOrNull { it.ticket.categoriaId == concurrente.id })
    }

    @Test
    fun categoriasBloqueaClientesRolesFalsosYLimpiaLaSesionInvalida(): Unit = runBlocking {
        var lecturas = 0
        val vigilado = object : AyudaRepository by repo {
            override fun observarCategorias() = repo.observarCategorias().also { lecturas++ }
        }
        val original = repo.observarCategorias().first()
        for (usuario in listOf(null, juan, juan.copy(rol = "ADMIN"), admin.copy(id = Int.MAX_VALUE))) {
            sesion.value = usuario
            val vm = categorias(repository = vigilado)
            assertTrue(vm.pantalla.value.categorias.isEmpty())
            accion(vm) { vm.guardar(Categoria(nombre = "Sin permiso", icono = "pc")) }
            accion(vm) { vm.cambiarActiva(original.first(), false) }
            accion(vm) { vm.eliminar(original.first()) }
            assertNotNull(vm.pantalla.value.error)
        }
        assertEquals(0, lecturas)
        assertEquals(original, repo.observarCategorias().first())
        sesion.value = admin
        val vm = categorias(original.first().id)
        assertFalse(vm.pantalla.value.formulario.nombre.isBlank())
        sesion.value = admin.copy(rol = "CLIENTE")
        withTimeout(15_000) { vm.pantalla.first { !it.cargando && it.errorCarga != null && it.categorias.isEmpty() } }
        assertTrue(vm.pantalla.value.formulario.nombre.isEmpty())
    }

    @Test
    fun categoriasRecuperaLecturaYConservaFormularioTrasFalloAlGuardar(): Unit = runBlocking {
        var lectura = true; var escritura = true
        val intermitente = object : AyudaRepository by repo {
            override fun observarCategorias() = flow {
                if (lectura) { lectura = false; throw IOException("Fallo de lectura") }
                emitAll(repo.observarCategorias())
            }
            override suspend fun guardarCategoria(c: Categoria) {
                if (escritura) { escritura = false; throw IOException("Fallo de escritura") }
                repo.guardarCategoria(c)
            }
        }
        val vm = categorias(repository = intermitente)
        assertNotNull(vm.pantalla.value.errorCarga)
        main { vm.reintentar() }
        withTimeout(15_000) { vm.pantalla.first { it.categorias.size == 5 && !it.cargando } }
        main { vm.actualizarNombre("Audio"); vm.seleccionarIcono("wifi"); vm.establecerActiva(false) }
        val borrador = vm.pantalla.value.formulario
        accion(vm) { vm.guardar() }
        assertNotNull(vm.pantalla.value.error)
        assertEquals(borrador, vm.pantalla.value.formulario)
        assertFalse(vm.pantalla.value.guardado)
        accion(vm) { vm.guardar() }
        assertTrue(vm.pantalla.value.guardado)
        val categoria = repo.observarCategorias().first().single { it.nombre == "Audio" }
        assertEquals("wifi", categoria.icono); assertFalse(categoria.activa)
    }

    @Test
    fun categoriasAceptaSeisIconosYSerializaDuplicadosEntreFormularios(): Unit = runBlocking {
        val vm = categorias()
        for (icono in listOf("pc", "code", "wifi", "key", "printer", "tag")) {
            accion(vm) { vm.guardar(Categoria(nombre = "Icono $icono", icono = icono)) }
            assertEquals(icono, repo.observarCategorias().first().single { it.nombre == "Icono $icono" }.icono)
        }
        val otro = categorias()
        main {
            vm.guardar(Categoria(nombre = "Equipos nuevos", icono = "pc"))
            otro.guardar(Categoria(nombre = " EQUIPOS    NUEVOS ", icono = "pc"))
        }
        withTimeout(15_000) { vm.pantalla.first { !it.operando } }
        withTimeout(15_000) { otro.pantalla.first { !it.operando } }
        assertEquals(1, repo.observarCategorias().first().count { it.nombre.equals("Equipos nuevos", ignoreCase = true) })
        assertTrue(vm.pantalla.value.error != null || otro.pantalla.value.error != null)
        val inexistente = categorias(Int.MAX_VALUE)
        assertNotNull(inexistente.pantalla.value.errorCarga)
    }
}
