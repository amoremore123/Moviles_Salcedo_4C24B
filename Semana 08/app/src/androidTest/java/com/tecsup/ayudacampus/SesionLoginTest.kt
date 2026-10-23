package com.tecsup.ayudacampus

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.ayudacampus.data.local.DatosIniciales
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Usa accesibilidad de Android para funcionar también en el emulador API 37,
// donde Espresso depende de una API oculta que ya no existe.
@RunWith(AndroidJUnit4::class)
class SesionLoginTest {
    private lateinit var app: AyudaApp
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val automation get() = InstrumentationRegistry.getInstrumentation().uiAutomation

    @Before
    fun preparar() {
        app = ApplicationProvider.getApplicationContext()
        runBlocking {
            DatosIniciales.sembrar(app.db)
            app.preferencias.cerrarSesion()
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
        esperar { nodos().count { it.isEditable } == 2 }
    }

    @After
    fun terminar() {
        if (::scenario.isInitialized) scenario.close()
        if (::app.isInitialized) runBlocking { app.preferencias.cerrarSesion() }
    }

    private fun nodos(): List<AccessibilityNodeInfo> {
        // Compose reemplaza sus nodos al recomponer; no reutilizar la caché anterior.
        if (Build.VERSION.SDK_INT >= 33) automation.clearCache()
        val root = automation.rootInActiveWindow ?: return emptyList()
        fun recorrer(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> =
            listOf(node) + (0 until node.childCount).flatMap { index ->
                node.getChild(index)?.let { recorrer(it) } ?: emptyList()
            }
        return recorrer(root)
    }

    private fun existe(texto: String) = nodos().any { it.text?.toString() == texto }

    private fun esperar(condicion: () -> Boolean) {
        val limite = SystemClock.uptimeMillis() + 15_000
        while (SystemClock.uptimeMillis() < limite) {
            // Atiende solo avisos ajenos a AyudaCampus del emulador de pruebas.
            // Un fallo de nuestra aplicación no se descarta ni se oculta.
            val pantalla = nodos()
            val accion = when {
                pantalla.any { it.text?.toString() == "System UI isn't responding" } -> "Wait"
                pantalla.any { it.text?.toString() == "Chrome keeps stopping" } -> "Close app"
                else -> null
            }
            if (accion != null) {
                pantalla.firstOrNull { it.text?.toString() == accion }?.let { texto ->
                    var boton = texto
                    while (!boton.isClickable && boton.parent != null) boton = boton.parent
                    boton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                SystemClock.sleep(300)
                continue
            }
            if (condicion()) return
            SystemClock.sleep(100)
        }
        fail("No se cumplió la condición de interfaz. Textos: ${nodos().mapNotNull { it.text }}")
    }

    private fun pulsar(texto: String) {
        esperar {
            var node = nodos().firstOrNull { it.text?.toString() == texto } ?: return@esperar false
            while (!node.isClickable && node.parent != null) node = node.parent
            if (!node.isEnabled || !node.isClickable) return@esperar false
            node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id)
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK).also {
                if (it) estabilizar()
            }
        }
    }

    private fun estabilizar() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        SystemClock.sleep(350)
    }

    private fun completar(correo: String, clave: String) {
        listOf(correo, clave).forEachIndexed { index, value ->
            esperar { nodos().filter { it.isEditable }.let { it.size == 2 && it.all { campo -> campo.isEnabled } } }
            val campos = nodos().filter { it.isEditable }
            assertEquals(2, campos.size)
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
            }
            assertTrue(campos[index].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args))
            estabilizar()
            esperar {
                val campo = nodos().filter { it.isEditable }.getOrNull(index)
                campo != null && (campo.text?.toString() == value ||
                    (campo.isPassword && campo.text?.length == value.length))
            }
        }
    }

    private fun pulsarDescripcion(descripcion: String) {
        esperar {
            var node = nodos().firstOrNull { it.contentDescription?.toString() == descripcion } ?: return@esperar false
            while (!node.isClickable && node.parent != null) node = node.parent
            (node.isEnabled && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)).also { if (it) estabilizar() }
        }
    }

    private fun campoNuevo(tag: String, texto: String) {
        esperar {
            val campo = nodos().firstOrNull { it.viewIdResourceName == tag || it.viewIdResourceName?.endsWith("/$tag") == true }
                ?: return@esperar false
            campo.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id)
            val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, texto) }
            (campo.isEnabled && campo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)).also { if (it) estabilizar() }
        }
        esperar { nodos().any { (it.viewIdResourceName == tag || it.viewIdResourceName?.endsWith("/$tag") == true) && it.text?.toString() == texto } }
    }

    private fun pulsarTag(tag: String) {
        esperar {
            var node = nodos().firstOrNull { it.viewIdResourceName == tag || it.viewIdResourceName?.endsWith("/$tag") == true }
                ?: return@esperar false
            while (!node.isClickable && node.parent != null) node = node.parent
            node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id)
            (node.isEnabled && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)).also { if (it) estabilizar() }
        }
    }

    @Test
    fun validacionesYLoginEstudiante() {
        pulsar("Ingresar")
        esperar { existe("Ingresa tu correo y contraseña") }
        completar("juan@campus.test", "incorrecta")
        pulsar("Ingresar")
        esperar { existe("Correo o contraseña incorrectos") }
        assertFalse(existe("Perfil: Estudiante"))
        completar("juan@campus.test", "1234")
        pulsar("Mostrar")
        esperar { existe("Ocultar") }
        pulsar("Soporte TI")
        pulsar("Ingresar")
        esperar { existe("El usuario no corresponde al perfil seleccionado") }
        pulsar("Estudiante")
        pulsar("Ingresar")
        esperar { existe("Perfil: Estudiante") }
        esperar { existe("2 abiertos") }
        assertEquals(1, nodos().count { it.isEditable })
        runBlocking {
            assertEquals("CLIENTE", app.preferencias.perfilActivo.first())
            assertTrue(app.preferencias.usuarioId.first() > 0)
        }
    }

    @Test
    fun misTicketsBuscaFiltraYNavega() {
        completar("juan@campus.test", "1234")
        pulsar("Ingresar")
        esperar { existe("Mis tickets") && existe("2 abiertos") && existe("Impresora sin papel") }
        val lista = runBlocking {
            val juan = requireNotNull(app.repository.login("juan@campus.test", "1234"))
            app.repository.observarTicketsCliente(juan.id).first()
        }
        assertEquals(6, lista.size)
        val wifi = lista.first { it.ticket.titulo == "Wi-Fi sin conexión" }.ticket
        ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso7.png")
        ).use { it.readBytes() }

        fun buscar(texto: String) {
            val campo = nodos().single { it.isEditable }
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, texto)
            }
            assertTrue(campo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args))
            estabilizar()
            esperar { nodos().singleOrNull { it.isEditable }?.text?.toString() == texto }
        }
        pulsar("Resueltos")
        esperar { existe("Error al abrir el editor") }
        pulsar("En proceso")
        esperar { existe("Equipo no enciende") }
        pulsar("Todos")
        buscar("CUENTA")
        esperar { existe("Cuenta de campus bloqueada") && existe("Cerrado") }
        pulsar("Abiertos")
        esperar { existe("No tienes tickets aquí") }
        assertTrue(existe("2 abiertos"))
        buscar("#${wifi.id.toString().padStart(4, '0')}")
        esperar { existe(wifi.titulo) }
        pulsar(wifi.titulo)
        esperar { existe("Detalle del ticket #${wifi.id.toString().padStart(4, '0')}") }
        pulsarDescripcion("Volver a mis tickets")
        esperar { existe("Mis tickets") }
        buscar("")
        esperar { nodos().any { it.contentDescription?.toString() == "Nuevo ticket" } }
        var boton = nodos().first { it.contentDescription?.toString() == "Nuevo ticket" }
        while (!boton.isClickable && boton.parent != null) boton = boton.parent
        assertTrue(boton.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        esperar { existe("Crear ticket") }
        pulsarDescripcion("Volver a mis tickets")
        esperar { existe("Mis tickets") }
    }

    @Test
    fun nuevoTicketValidaGuardaEnRoomYRegresaSinDuplicados() {
        val iniciales = runBlocking { app.repository.observarTodosLosTickets().first().map { it.ticket.id }.toSet() }
        val inactiva = runBlocking { app.repository.observarCategorias().first().first { it.nombre == "Cuenta" } }
        val titulo = "Sin Wi-Fi en el laboratorio 3"
        try {
            runBlocking { app.repository.cambiarActiva(inactiva.id, false) }
            completar("juan@campus.test", "1234")
            pulsar("Ingresar")
            esperar { existe("Mis tickets") && existe("2 abiertos") }
            pulsarDescripcion("Nuevo ticket")
            esperar { existe("Crear ticket") }
            campoNuevo("nuevo_titulo", "Borrador que no se guarda")
            pulsarDescripcion("Volver a mis tickets")
            esperar { existe("Mis tickets") }
            runBlocking { assertEquals(iniciales.size, app.repository.observarTodosLosTickets().first().size) }

            // El guardado debe regresar a Todos incluso si antes había un filtro activo.
            pulsar("Resueltos")
            esperar { existe("Error al abrir el editor") }
            pulsarDescripcion("Nuevo ticket")
            esperar { existe("Crear ticket") }
            pulsar("Crear ticket")
            esperar { existe("Selecciona una categoría") }
            esperar { existe("El título debe tener al menos 5 caracteres") }
            campoNuevo("nuevo_titulo", titulo)
            campoNuevo("nuevo_descripcion", "Desde ayer no hay señal Wi-Fi en el laboratorio. Afecta a toda la clase.")
            campoNuevo("nuevo_ubicacion", "Laboratorio 3 · Pabellón B")
            pulsarTag("nuevo_categoria")
            esperar { existe("Hardware") && existe("Red / Wi-Fi") }
            assertFalse(existe("Cuenta"))
            pulsar("Red / Wi-Fi")
            pulsar("ALTA")
            nodos().firstOrNull { it.viewIdResourceName == "nuevo_categoria" }?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id)
            SystemClock.sleep(500)
            ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso8.png")
            ).use { it.readBytes() }

            esperar { existe("Crear ticket") }
            var boton = nodos().first { it.text?.toString() == "Crear ticket" }
            while (!boton.isClickable && boton.parent != null) boton = boton.parent
            assertTrue(boton.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            boton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            esperar { existe("Mis tickets") && existe(titulo) && existe("3 abiertos") }
            runBlocking {
                val juan = requireNotNull(app.repository.login("juan@campus.test", "1234"))
                val lista = app.repository.observarTicketsCliente(juan.id).first()
                val creados = lista.filter { it.ticket.id !in iniciales }
                assertEquals(1, creados.size)
                val ticket = creados.single().ticket
                assertEquals(titulo, ticket.titulo)
                assertEquals(juan.id, ticket.clienteId)
                assertEquals("ABIERTO", ticket.estado)
                assertEquals("ALTA", ticket.prioridad)
                assertEquals(ticket.id, lista.first().ticket.id)
            }
        } finally {
            runBlocking {
                // Limpieza exclusiva de los registros creados por esta prueba de interfaz.
                val nuevos = app.repository.observarTodosLosTickets().first().filter { it.ticket.id !in iniciales }
                nuevos.forEach { app.db.openHelper.writableDatabase.execSQL("DELETE FROM tickets WHERE id = ?", arrayOf(it.ticket.id)) }
                app.repository.cambiarActiva(inactiva.id, inactiva.activa)
            }
        }
    }

    private fun mostrarEnDetalle(texto: String, atras: Boolean = false) {
        esperar {
            if (existe(texto)) true else {
                val lista = nodos().firstOrNull { it.viewIdResourceName?.endsWith("detalle_lista") == true }
                    ?: nodos().firstOrNull { it.isScrollable }
                lista?.performAction(if (atras) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                estabilizar()
                false
            }
        }
    }

    @Test
    fun detalleComentaCalificaCierraYVuelveALaLista() {
        val inicial = runBlocking { app.repository.observarTodosLosTickets().first().first { it.ticket.estado == "RESUELTO" } }
        val texto = "Probé el editor y ahora abre correctamente. Gracias por la atención."
        try {
            completar("juan@campus.test", "1234")
            pulsar("Ingresar")
            esperar { existe("Mis tickets") }
            pulsar("Resueltos")
            esperar { existe(inicial.ticket.titulo) }
            pulsar(inicial.ticket.titulo)
            esperar { existe("Detalle del ticket #${inicial.ticket.id.toString().padStart(4, '0')}") }
            esperar { existe(inicial.ticket.titulo) && existe(inicial.ticket.descripcion) }
            assertTrue(nodos().any { it.text?.toString()?.contains(inicial.categoria.nombre) == true })
            assertTrue(nodos().any { it.text?.toString()?.contains(inicial.ticket.ubicacion) == true })
            assertTrue(existe("Estudiante: ${inicial.cliente.nombre}"))
            assertTrue(nodos().any { it.text?.toString()?.startsWith("Creado: ") == true })
            mostrarEnDetalle(inicial.comentarios.first().texto)
            mostrarEnDetalle("Enviar comentario")
            pulsar("Enviar comentario")
            mostrarEnDetalle("Escribe un comentario antes de enviarlo")
            campoNuevo("detalle_comentario", texto)
            pulsar("Enviar comentario")
            mostrarEnDetalle("Comentario enviado")
            mostrarEnDetalle(texto, atras = true)
            runBlocking {
                val comentario = app.repository.observarTicket(inicial.ticket.id).first()!!.comentarios.single { it.texto == texto }
                assertEquals(inicial.cliente.id, comentario.autorId)
                assertEquals(inicial.cliente.nombre, comentario.autorNombre)
                assertEquals("CLIENTE", comentario.autorRol)
            }
            mostrarEnDetalle("Calificar y cerrar", atras = true)
            pulsar("Calificar y cerrar")
            esperar { existe("Calificar y cerrar ticket") }
            var confirmar = nodos().first { it.text?.toString() == "Confirmar y cerrar" }
            while (!confirmar.isClickable && confirmar.parent != null) confirmar = confirmar.parent
            assertFalse(confirmar.isEnabled)
            pulsarDescripcion("4 estrellas")
            esperar { existe("Calificación seleccionada: 4 / 5") }
            pulsar("Cancelar")
            runBlocking { assertEquals("RESUELTO", app.repository.observarTicket(inicial.ticket.id).first()!!.ticket.estado) }
            pulsar("Calificar y cerrar")
            pulsarDescripcion("4 estrellas")
            confirmar = nodos().first { it.text?.toString() == "Confirmar y cerrar" }
            while (!confirmar.isClickable && confirmar.parent != null) confirmar = confirmar.parent
            assertTrue(confirmar.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            confirmar.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            mostrarEnDetalle("Calificación: 4 / 5", atras = true)
            esperar { existe("Ticket cerrado") }
            runBlocking {
                val ticket = app.repository.observarTicket(inicial.ticket.id).first()!!.ticket
                assertEquals("CERRADO", ticket.estado)
                assertEquals(4, ticket.calificacion)
                assertNotNull(ticket.fechaCierre)
            }
            for (paso in 1..8) {
                val lista = nodos().firstOrNull { it.viewIdResourceName?.endsWith("detalle_lista") == true }
                if (lista?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != true) break
                estabilizar()
            }
            estabilizar()
            ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso9.png")
            ).use { it.readBytes() }
            assertFalse(nodos().any { it.isEditable })
            assertFalse(existe("Calificar y cerrar"))
            pulsarDescripcion("Volver a mis tickets")
            esperar { existe("Mis tickets") }
        } finally {
            runBlocking {
                app.db.openHelper.writableDatabase.execSQL("DELETE FROM comentarios WHERE ticketId = ? AND texto = ?", arrayOf(inicial.ticket.id, texto))
                app.db.openHelper.writableDatabase.execSQL(
                    "UPDATE tickets SET estado = ?, calificacion = ?, fechaCierre = ? WHERE id = ?",
                    arrayOf(inicial.ticket.estado, inicial.ticket.calificacion, inicial.ticket.fechaCierre, inicial.ticket.id)
                )
            }
        }
    }

    @Test
    fun soporteRecuperaSesionYCierraSesion() {
        pulsar("Soporte TI")
        completar("soporte@campus.test", "admin")
        pulsar("Ingresar")
        esperar { existe("Perfil: Soporte TI") }
        scenario.close()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        esperar { existe("Perfil: Soporte TI") }
        assertEquals(0, nodos().count { it.isEditable })
        pulsar("Cerrar sesión")
        esperar { nodos().count { it.isEditable } == 2 }
        runBlocking {
            assertEquals("", app.preferencias.perfilActivo.first())
            assertEquals(0, app.preferencias.usuarioId.first())
        }
    }

    @Test
    fun sesionesInexistentesOConRolInvalidoSeLimpian() {
        scenario.close()
        runBlocking { app.preferencias.guardarSesion("CLIENTE", Int.MAX_VALUE) }
        scenario = ActivityScenario.launch(MainActivity::class.java)
        esperar { nodos().count { it.isEditable } == 2 }
        runBlocking {
            assertEquals("", app.preferencias.perfilActivo.first())
            assertEquals(0, app.preferencias.usuarioId.first())
        }
        scenario.close()
        runBlocking {
            val juan = requireNotNull(app.repository.login("juan@campus.test", "1234"))
            app.preferencias.guardarSesion("ADMIN", juan.id)
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
        esperar { nodos().count { it.isEditable } == 2 }
        runBlocking { assertEquals("", app.preferencias.perfilActivo.first()) }
    }
}
