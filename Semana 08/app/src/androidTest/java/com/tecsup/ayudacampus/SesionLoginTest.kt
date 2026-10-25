package com.tecsup.ayudacampus

import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.ui.navigation.NavAyuda
import com.tecsup.ayudacampus.ui.navigation.Rutas
import com.tecsup.ayudacampus.ui.theme.AyudaTheme
import com.tecsup.ayudacampus.viewmodel.SesionViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeout
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
            mostrarTextoSoporte("nuevo_formulario", "ALTA"); pulsar("ALTA")
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
        pulsarDescripcion("Cerrar sesión")
        esperar { existe("¿Cerrar sesión?") }
        pulsar("Cerrar sesión")
        esperar { nodos().count { it.isEditable } == 2 }
        runBlocking {
            assertEquals("", app.preferencias.perfilActivo.first())
            assertEquals(0, app.preferencias.usuarioId.first())
        }
    }

    private fun mostrarResumen(texto: String) {
        esperar {
            if (existe(texto)) true else {
                nodos().firstOrNull { it.viewIdResourceName?.endsWith("panel_resumen") == true }
                    ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                estabilizar()
                false
            }
        }
    }

    @Test
    fun panelDeSoporteMuestraIndicadoresBuscaFiltraYNavega() {
        pulsar("Soporte TI")
        completar("soporte@campus.test", "admin")
        pulsar("Ingresar")
        esperar { existe("Panel de soporte") && existe("Perfil: Soporte TI") }
        esperar { nodos().any { it.contentDescription?.toString() == "Abiertos: 2" } }
        assertTrue(nodos().any { it.contentDescription?.toString() == "En proceso: 2" })
        assertTrue(nodos().any { it.contentDescription?.toString() == "Resueltos: 1" })
        assertTrue(nodos().any { it.contentDescription?.toString() == "Cerrados: 1" })
        assertTrue(existe("Hola, Soporte TI"))
        esperar { existe("1 ticket ALTA sin atender") }
        mostrarResumen("Software: 2")
        mostrarResumen("Impresión: 1")
        for (paso in 1..8) {
            val lista = nodos().firstOrNull { it.viewIdResourceName?.endsWith("panel_resumen") == true }
            if (lista?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != true) break
            estabilizar()
        }
        estabilizar()
        ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso10_Panel.png")
        ).use { it.readBytes() }
        pulsarTag("nav_admin_1")
        esperar { existe("6 resultados") && existe("Wi-Fi sin conexión") && existe("Equipo no enciende") }
        val textos = nodos().mapNotNull { it.text?.toString() }
        assertTrue(textos.indexOf("Wi-Fi sin conexión") < textos.indexOf("Equipo no enciende"))
        assertTrue(existe("Estudiante: Juan"))
        val lista = runBlocking { app.repository.observarTodosLosTickets().first() }
        val cuenta = lista.single { it.ticket.titulo == "Cuenta de campus bloqueada" }.ticket.id
        val wifi = lista.single { it.ticket.titulo == "Wi-Fi sin conexión" }.ticket.id
        campoNuevo("panel_busqueda", "EQUIPO")
        esperar { existe("Equipo no enciende") && existe("1 resultado") }
        campoNuevo("panel_busqueda", "#${cuenta.toString().padStart(4, '0')}")
        esperar { existe("Cuenta de campus bloqueada") && existe("Cerrado") }
        campoNuevo("panel_busqueda", "JUAN")
        esperar { existe("6 resultados") }
        pulsarTag("panel_estado_ABIERTOS")
        pulsarTag("panel_prioridad_ALTA")
        esperar { existe("1 resultado") && existe("Wi-Fi sin conexión") }
        pulsarTag("panel_prioridad_BAJA")
        esperar { existe("1 resultado") && existe("Impresora sin papel") }
        pulsarTag("panel_estado_CERRADOS")
        esperar { existe("No hay tickets con estos filtros") }
        pulsarTag("nav_admin_0")
        esperar { nodos().any { it.contentDescription?.toString() == "Abiertos: 2" } }
        assertTrue(nodos().any { it.contentDescription?.toString() == "Cerrados: 1" })
        pulsar("Ver")
        esperar { existe("Wi-Fi sin conexión") && existe("1 resultado") }
        pulsar("Wi-Fi sin conexión")
        esperar { existe("Atender ticket #${wifi.toString().padStart(4, '0')}") }
        esperar { existe("Estudiante: Juan") && existe("Wi-Fi sin conexión") }
        pulsarDescripcion("Volver al panel")
        esperar { existe("Bandeja de tickets") && existe("Wi-Fi sin conexión") }
        pulsarTag("panel_estado_TODOS")
        pulsarTag("panel_prioridad_TODAS")
        esperar { existe("6 resultados") }
        estabilizar()
        ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso10_Bandeja.png")
        ).use { it.readBytes() }
    }

    private fun mostrarControlSoporte(listaTag: String, controlTag: String, atras: Boolean = false) {
        esperar {
            if (nodos().any { it.viewIdResourceName?.endsWith(controlTag) == true }) true else {
                nodos().firstOrNull { it.viewIdResourceName?.endsWith(listaTag) == true }
                    ?.performAction(if (atras) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                estabilizar()
                false
            }
        }
    }
    private fun mostrarTextoSoporte(listaTag: String, texto: String) {
        esperar {
            if (existe(texto)) true else {
                nodos().firstOrNull { it.viewIdResourceName?.endsWith(listaTag) == true }
                    ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                estabilizar()
                false
            }
        }
    }
    private fun inicioListaSoporte(listaTag: String) {
        for (paso in 1..8) {
            val lista = nodos().firstOrNull { it.viewIdResourceName?.endsWith(listaTag) == true }
            if (lista?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) != true) break
            estabilizar()
        }
    }

    @Test
    fun atenderRespondeGuardaEstadoYPrioridadYActualizaPanel() {
        val inicial = runBlocking { app.repository.observarTodosLosTickets().first().first { it.ticket.estado == "ABIERTO" } }
        val respuesta = "Soporte verificó la incidencia y continúa trabajando en la solución."
        try {
            pulsar("Soporte TI")
            completar("soporte@campus.test", "admin")
            pulsar("Ingresar")
            esperar { existe("Perfil: Soporte TI") }
            pulsarTag("nav_admin_1")
            esperar { existe(inicial.ticket.titulo) }
            pulsar(inicial.ticket.titulo)
            esperar { existe("Atender ticket #${inicial.ticket.id.toString().padStart(4, '0')}") }
            esperar { existe("Estudiante: ${inicial.cliente.nombre}") && existe(inicial.ticket.descripcion) }
            mostrarControlSoporte("atender_lista", "atender_estado_CERRADO")
            assertFalse(nodos().first { it.viewIdResourceName?.endsWith("atender_estado_CERRADO") == true }.isEnabled)
            mostrarTextoSoporte("atender_lista", "Enviar respuesta")
            pulsar("Enviar respuesta")
            esperar { existe("Escribe una respuesta antes de enviarla") }
            campoNuevo("atender_respuesta", respuesta)
            pulsar("Enviar respuesta")
            esperar { existe("Respuesta enviada") }
            runBlocking {
                val comentario = app.repository.observarTicket(inicial.ticket.id).first()!!.comentarios.single { it.texto == respuesta }
                val soporte = requireNotNull(app.repository.login("soporte@campus.test", "admin"))
                assertEquals(soporte.id, comentario.autorId)
                assertEquals(soporte.nombre, comentario.autorNombre)
                assertEquals("ADMIN", comentario.autorRol)
            }
            mostrarControlSoporte("atender_lista", "atender_estado_EN_PROCESO", atras = true)
            pulsarTag("atender_estado_EN_PROCESO")
            pulsarTag("atender_prioridad_MEDIA")
            pulsar("Guardar cambios")
            esperar { existe("Cambios guardados") }
            runBlocking {
                val ticket = app.repository.observarTicket(inicial.ticket.id).first()!!.ticket
                assertEquals("EN_PROCESO", ticket.estado); assertEquals("MEDIA", ticket.prioridad)
                assertNull(ticket.calificacion); assertNull(ticket.fechaCierre)
            }
            inicioListaSoporte("atender_lista")
            estabilizar()
            ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso11_Atender.png")
            ).use { it.readBytes() }
            pulsarDescripcion("Volver al panel")
            esperar { existe("Bandeja de tickets") }
            pulsarTag("nav_admin_0")
            esperar { nodos().any { it.contentDescription?.toString() == "Abiertos: 1" } && nodos().any { it.contentDescription?.toString() == "En proceso: 3" } }
        } finally {
            runBlocking {
                app.db.openHelper.writableDatabase.execSQL("DELETE FROM comentarios WHERE ticketId = ? AND texto = ?", arrayOf(inicial.ticket.id, respuesta))
                app.db.openHelper.writableDatabase.execSQL(
                    "UPDATE tickets SET estado = ?, prioridad = ?, calificacion = ?, fechaCierre = ? WHERE id = ?",
                    arrayOf(inicial.ticket.estado, inicial.ticket.prioridad, inicial.ticket.calificacion, inicial.ticket.fechaCierre, inicial.ticket.id))
            }
        }
    }

    @Test
    fun categoriasCreaEditaActivaYEliminaConConfirmacionSinRomperRelaciones() {
        val iniciales = runBlocking { app.repository.observarCategorias().first() }
        val nombre = "Audio de prueba"
        try {
            pulsar("Soporte TI")
            completar("soporte@campus.test", "admin")
            pulsar("Ingresar")
            esperar { existe("Perfil: Soporte TI") }
            pulsar("Categorías")
            esperar { existe("Categorías") && existe("Cuenta") }
            pulsarDescripcion("Agregar categoría")
            esperar { existe("Nueva categoría") }
            pulsar("Guardar categoría")
            esperar { existe("Ingresa el nombre de la categoría") }
            campoNuevo("categoria_nombre", " HARDWARE ")
            pulsar("Guardar categoría")
            esperar { existe("Ya existe una categoría con ese nombre") }
            campoNuevo("categoria_nombre", nombre)
            pulsarTag("categoria_icono_wifi")
            pulsarTag("categoria_form_activa")
            estabilizar()
            ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso11_Formulario.png")
            ).use { it.readBytes() }
            pulsar("Guardar categoría")
            esperar { existe("Categorías") }
            val creada = runBlocking { app.repository.observarCategorias().first().single { it.nombre == nombre } }
            assertTrue(creada.id > 0); assertEquals("wifi", creada.icono); assertFalse(creada.activa)
            assertEquals(iniciales.size + 1, runBlocking { app.repository.observarCategorias().first().size })
            mostrarControlSoporte("categorias_lista", "categoria_editar_${creada.id}", atras = true)
            pulsarTag("categoria_editar_${creada.id}")
            esperar { existe("Editar categoría") }
            campoNuevo("categoria_nombre", "Audio editado")
            pulsarTag("categoria_icono_printer")
            pulsarTag("categoria_form_activa")
            pulsar("Guardar categoría")
            esperar { existe("Categorías") }
            val editada = runBlocking { app.repository.observarCategorias().first().single { it.id == creada.id } }
            assertEquals("Audio editado", editada.nombre); assertEquals("printer", editada.icono); assertTrue(editada.activa)
            mostrarControlSoporte("categorias_lista", "categoria_activa_${creada.id}", atras = true)
            pulsarTag("categoria_activa_${creada.id}")
            esperar { existe("Categoría desactivada") }
            runBlocking {
                assertFalse(app.repository.observarCategoriasActivas().first().any { it.id == creada.id })
                assertEquals(iniciales.size + 1, app.repository.observarCategorias().first().size)
            }
            inicioListaSoporte("categorias_lista")
            estabilizar()
            ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("screencap -p /sdcard/Download/AyudaCampus_Paso11_Categorias.png")
            ).use { it.readBytes() }
            val usada = iniciales.single { it.nombre == "Hardware" }
            mostrarControlSoporte("categorias_lista", "categoria_eliminar_${usada.id}")
            pulsarTag("categoria_eliminar_${usada.id}")
            esperar { existe("Eliminar categoría") }
            pulsar("Eliminar")
            esperar { existe("Esta categoría tiene tickets. Desactívala en lugar de eliminarla.") }
            runBlocking { assertTrue(app.repository.observarTodosLosTickets().first().any { it.ticket.categoriaId == usada.id }) }
            mostrarControlSoporte("categorias_lista", "categoria_eliminar_${creada.id}", atras = true)
            pulsarTag("categoria_eliminar_${creada.id}")
            pulsar("Cancelar")
            runBlocking { assertTrue(app.repository.observarCategorias().first().any { it.id == creada.id }) }
            pulsarTag("categoria_eliminar_${creada.id}")
            pulsar("Eliminar")
            esperar { existe("Categoría eliminada") }
            runBlocking { assertFalse(app.repository.observarCategorias().first().any { it.id == creada.id }) }
            pulsarDescripcion("Volver al panel")
            esperar { existe("Panel de soporte") }
        } finally {
            runBlocking {
                val nuevas = app.repository.observarCategorias().first().filter { c -> iniciales.none { it.id == c.id } }
                nuevas.forEach { app.repository.eliminarCategoria(it) }
                iniciales.forEach { app.repository.guardarCategoria(it) }
            }
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

    private fun capturaPerfil(nombre: String) {
        estabilizar()
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("screencap -p /sdcard/Download/$nombre.png")).use { it.readBytes() }
    }
    @Test
    fun perfilEstudiantePersisteAjustesReiniciaYCierraConConfirmacion() {
        val original = runBlocking { Triple(app.preferencias.modoOscuro.first(), app.preferencias.notificaciones.first(), app.preferencias.recibirTickets.first()) }
        try {
            runBlocking { app.preferencias.guardarModoOscuro(false); app.preferencias.guardarNotificaciones(true) }
            completar("juan@campus.test", "1234"); pulsar("Ingresar")
            esperar { existe("Mis tickets") }; pulsarDescripcion("Mi perfil")
            esperar { existe("Mi perfil") && existe("juan@campus.test") }
            assertTrue(existe("Juan")); assertTrue(existe("Estudiante")); assertTrue(existe("Abiertos")); assertTrue(existe("Resueltos")); assertTrue(existe("Totales"))
            assertFalse(nodos().any { it.viewIdResourceName?.endsWith("perfil_recibir") == true })
            pulsarTag("perfil_oscuro")
            esperar { runBlocking { app.preferencias.modoOscuro.first() } }
            pulsarTag("perfil_notificaciones")
            esperar { !runBlocking { app.preferencias.notificaciones.first() } }
            capturaPerfil("AyudaCampus_Paso12_Perfil")
            scenario.close(); scenario = ActivityScenario.launch(MainActivity::class.java)
            esperar { existe("Mis tickets") }; pulsarDescripcion("Mi perfil")
            esperar { existe("juan@campus.test") }
            esperar { nodos().firstOrNull { it.viewIdResourceName?.endsWith("perfil_oscuro") == true }?.isChecked == true }
            assertFalse(nodos().first { it.viewIdResourceName?.endsWith("perfil_notificaciones") == true }.isChecked)
            mostrarControlSoporte("perfil_lista", "perfil_salir")
            pulsarTag("perfil_salir"); esperar { existe("¿Cerrar sesión?") }; pulsar("Cancelar")
            assertTrue(existe("Mi perfil"))
            pulsarTag("perfil_salir"); esperar { existe("¿Cerrar sesión?") }; pulsar("Cerrar sesión")
            esperar { nodos().count { it.isEditable } == 2 }
            runBlocking {
                assertEquals("", app.preferencias.perfilActivo.first()); assertEquals(0, app.preferencias.usuarioId.first())
                assertTrue(app.preferencias.modoOscuro.first()); assertFalse(app.preferencias.notificaciones.first())
                assertEquals(original.third, app.preferencias.recibirTickets.first())
            }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() } }
            estabilizar()
            assertFalse(existe("Mis tickets")); assertFalse(existe("Mi perfil"))
            scenario.close(); scenario = ActivityScenario.launch(MainActivity::class.java)
            esperar { nodos().count { it.isEditable } == 2 }
        } finally {
            runBlocking { app.preferencias.guardarModoOscuro(original.first); app.preferencias.guardarNotificaciones(original.second); app.preferencias.guardarRecibirTickets(original.third) }
        }
    }
    @Test
    fun ajustesSoporteCierraMesaYCambiaPerfilSinArrastrarLaPila() {
        val original = runBlocking { Triple(app.preferencias.modoOscuro.first(), app.preferencias.notificaciones.first(), app.preferencias.recibirTickets.first()) }
        try {
            runBlocking { app.preferencias.guardarModoOscuro(false); app.preferencias.guardarRecibirTickets(true) }
            pulsar("Soporte TI"); completar("soporte@campus.test", "admin"); pulsar("Ingresar")
            esperar { existe("Panel de soporte") }; pulsarDescripcion("Perfil y ajustes")
            esperar { existe("soporte@campus.test") }; assertTrue(existe("Soporte TI"))
            mostrarControlSoporte("perfil_lista", "perfil_recibir")
            pulsarTag("perfil_recibir"); esperar { !runBlocking { app.preferencias.recibirTickets.first() } }
            pulsarTag("perfil_oscuro"); esperar { runBlocking { app.preferencias.modoOscuro.first() } }
            inicioListaSoporte("perfil_lista"); capturaPerfil("AyudaCampus_Paso12_Ajustes")
            mostrarControlSoporte("perfil_lista", "perfil_cambiar"); pulsarTag("perfil_cambiar")
            esperar { existe("Cambiar de perfil de práctica") }; pulsar("Cambiar perfil")
            esperar { existe("Mis tickets") && existe("Mesa de ayuda cerrada") }
            assertFalse(nodos().any { it.contentDescription?.toString() == "Nuevo ticket" })
            val juan = runBlocking { app.repository.login("juan@campus.test", "1234")!! }
            runBlocking { assertEquals("CLIENTE", app.preferencias.perfilActivo.first()); assertEquals(juan.id, app.preferencias.usuarioId.first()) }
            scenario.close(); scenario = ActivityScenario.launch(MainActivity::class.java)
            esperar { existe("Mis tickets") && existe("Mesa de ayuda cerrada") }
            pulsarDescripcion("Mi perfil"); esperar { existe("juan@campus.test") }
            assertFalse(nodos().any { it.viewIdResourceName?.endsWith("perfil_recibir") == true })
            mostrarControlSoporte("perfil_lista", "perfil_cambiar"); pulsarTag("perfil_cambiar"); pulsar("Cambiar perfil")
            esperar { existe("Panel de soporte") }
            pulsarDescripcion("Perfil y ajustes"); esperar { existe("soporte@campus.test") }
            mostrarControlSoporte("perfil_lista", "perfil_recibir"); pulsarTag("perfil_recibir")
            esperar { runBlocking { app.preferencias.recibirTickets.first() } }
            pulsarDescripcion("Volver al inicio"); esperar { existe("Panel de soporte") }
            pulsarDescripcion("Cerrar sesión"); pulsar("Cerrar sesión"); esperar { nodos().count { it.isEditable } == 2 }
            completar("juan@campus.test", "1234"); pulsar("Ingresar")
            esperar { existe("Mis tickets") }
            esperar { nodos().any { it.contentDescription?.toString() == "Nuevo ticket" } }
            assertFalse(existe("Mesa de ayuda cerrada"))
        } finally {
            runBlocking { app.preferencias.guardarModoOscuro(original.first); app.preferencias.guardarNotificaciones(original.second); app.preferencias.guardarRecibirTickets(original.third) }
        }
    }
    @Test
    fun rutasManipuladasNoAbrenPantallasDelOtroRol() {
        val sesionVm = SesionViewModel(app.repository, app.preferencias)
        lateinit var nav: NavHostController
        try {
            runBlocking { withTimeout(15_000) { sesionVm.recuperandoSesion.first { !it } } }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { sesionVm.login("juan@campus.test", "1234", "CLIENTE") }
            runBlocking { withTimeout(15_000) { sesionVm.cargando.first { !it } } }
            scenario.onActivity { activity ->
                activity.setContent {
                    val controlador = rememberNavController()
                    SideEffect { nav = controlador }
                    val usuario by sesionVm.usuarioActual.collectAsState()
                    val oscuro by sesionVm.modoOscuro.collectAsState()
                    AyudaTheme(usuario?.rol ?: "CLIENTE", oscuro) { NavAyuda(sesionVm, "CLIENTE", {}, controlador) }
                }
            }
            esperar { existe("Mis tickets") }
            for (ruta in listOf(Rutas.Panel, Rutas.Categorias, Rutas.formularioCategoria(), Rutas.atenderTicket(1), Rutas.Ajustes)) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync { nav.navigate(ruta) }
                estabilizar()
                assertFalse(existe("Panel de soporte")); assertFalse(existe("Perfil y ajustes")); assertFalse(existe("Nueva categoría"))
                assertFalse(nodos().any { it.viewIdResourceName?.endsWith("atender_lista") == true || it.viewIdResourceName?.endsWith("categorias_lista") == true })
                InstrumentationRegistry.getInstrumentation().runOnMainSync { nav.popBackStack() }
                esperar { existe("Mis tickets") }
            }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { sesionVm.cambiarPerfil() }
            esperar { existe("Panel de soporte") }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { assertNull(nav.previousBackStackEntry) }
            for (ruta in listOf(Rutas.MisTickets, Rutas.NuevoTicket, Rutas.detalleTicket(1), Rutas.Perfil)) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync { nav.navigate(ruta) }
                estabilizar()
                assertFalse(existe("Mis tickets")); assertFalse(existe("Nuevo ticket")); assertFalse(existe("Mi perfil"))
                assertFalse(nodos().any { it.viewIdResourceName?.endsWith("detalle_lista") == true })
                InstrumentationRegistry.getInstrumentation().runOnMainSync { nav.popBackStack() }
                esperar { existe("Panel de soporte") }
            }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { sesionVm.cerrarSesion() }
            esperar { nodos().count { it.isEditable } == 2 }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { assertNull(nav.previousBackStackEntry); nav.navigate(Rutas.Ajustes) }
            estabilizar(); assertFalse(existe("Perfil y ajustes")); assertFalse(existe("soporte@campus.test"))
        } finally { sesionVm.viewModelScope.cancel() }
    }

    @Test
    fun aceptacionFinalCicloCompletoEntreEstudianteYSoporte() {
        val titulo = "Aceptación final ${System.currentTimeMillis()}"
        val respuesta = "Revisamos el equipo y aplicamos la solución solicitada."
        val recepcionOriginal = runBlocking { app.preferencias.recibirTickets.first() }
        var ticketId: Int? = null
        fun cambiarDesdeInicio(soporte: Boolean) {
            pulsarDescripcion(if (soporte) "Perfil y ajustes" else "Mi perfil")
            esperar { existe(if (soporte) "soporte@campus.test" else "juan@campus.test") }
            mostrarControlSoporte("perfil_lista", "perfil_cambiar")
            pulsarTag("perfil_cambiar"); esperar { existe("Cambiar de perfil de práctica") }; pulsar("Cambiar perfil")
            esperar { existe(if (soporte) "Mis tickets" else "Panel de soporte") }
        }
        fun abrirEnSoporte(id: Int) {
            pulsarTag("nav_admin_1"); esperar { existe("Bandeja de tickets") }
            campoNuevo("panel_busqueda", "#$id")
            esperar { existe(titulo) && existe("1 resultado") }; pulsar(titulo)
            esperar { existe("Atender ticket #${id.toString().padStart(4, '0')}") }
        }
        try {
            capturaPerfil("AyudaCampus_Paso13_Login")
            runBlocking { app.preferencias.guardarRecibirTickets(true) }
            completar("juan@campus.test", "1234"); pulsar("Ingresar")
            esperar { existe("Mis tickets") }; pulsarDescripcion("Nuevo ticket")
            esperar { existe("Crear ticket") }
            campoNuevo("nuevo_titulo", titulo)
            campoNuevo("nuevo_descripcion", "El equipo del aula no permite iniciar la práctica final.")
            campoNuevo("nuevo_ubicacion", "Laboratorio de aceptación")
            pulsarTag("nuevo_categoria"); esperar { existe("Hardware") }; pulsar("Hardware")
            mostrarTextoSoporte("nuevo_formulario", "ALTA"); pulsar("ALTA"); pulsar("Crear ticket")
            esperar { existe("Mis tickets") && existe(titulo) }
            val creado = runBlocking { app.repository.observarTodosLosTickets().first().single { it.ticket.titulo == titulo }.ticket }
            ticketId = creado.id
            assertEquals("ABIERTO", creado.estado); assertEquals("ALTA", creado.prioridad)
            cambiarDesdeInicio(soporte = false)
            abrirEnSoporte(creado.id)
            mostrarControlSoporte("atender_lista", "atender_estado_EN_PROCESO")
            pulsarTag("atender_estado_EN_PROCESO"); pulsarTag("atender_prioridad_MEDIA"); pulsar("Guardar cambios")
            esperar { existe("Cambios guardados") }
            runBlocking {
                val actual = app.repository.observarTicket(creado.id).first()!!.ticket
                assertEquals("EN_PROCESO", actual.estado); assertEquals("MEDIA", actual.prioridad)
            }
            mostrarTextoSoporte("atender_lista", "Enviar respuesta")
            campoNuevo("atender_respuesta", respuesta); pulsar("Enviar respuesta"); esperar { existe("Respuesta enviada") }
            mostrarControlSoporte("atender_lista", "atender_estado_RESUELTO", atras = true)
            pulsarTag("atender_estado_RESUELTO"); pulsar("Guardar cambios"); esperar { existe("Cambios guardados") }
            pulsarDescripcion("Volver al panel"); esperar { existe("Bandeja de tickets") }
            cambiarDesdeInicio(soporte = true)
            esperar { existe(titulo) }; pulsar(titulo)
            mostrarEnDetalle("Tu incidencia está resuelta")
            mostrarEnDetalle(respuesta)
            mostrarEnDetalle("Reabrir ticket", atras = true); pulsar("Reabrir ticket")
            mostrarEnDetalle("Ticket reabierto")
            runBlocking {
                val abierto = app.repository.observarTicket(creado.id).first()!!.ticket
                assertEquals("ABIERTO", abierto.estado); assertNull(abierto.calificacion); assertNull(abierto.fechaCierre)
            }
            pulsarDescripcion("Volver a mis tickets"); esperar { existe("Mis tickets") }
            cambiarDesdeInicio(soporte = false); abrirEnSoporte(creado.id)
            mostrarControlSoporte("atender_lista", "atender_estado_RESUELTO")
            pulsarTag("atender_estado_RESUELTO"); pulsar("Guardar cambios"); esperar { existe("Cambios guardados") }
            pulsarDescripcion("Volver al panel"); esperar { existe("Bandeja de tickets") }
            cambiarDesdeInicio(soporte = true); esperar { existe(titulo) }; pulsar(titulo)
            mostrarEnDetalle("Calificar y cerrar"); pulsar("Calificar y cerrar")
            esperar { existe("Calificar y cerrar ticket") }; pulsarDescripcion("4 estrellas"); pulsar("Confirmar y cerrar")
            mostrarEnDetalle("Ticket cerrado"); mostrarEnDetalle("Calificación: 4 / 5")
            assertFalse(nodos().any { it.isEditable }); assertFalse(existe("Reabrir ticket"))
            runBlocking {
                val final = app.repository.observarTicket(creado.id).first()!!
                assertEquals("CERRADO", final.ticket.estado); assertEquals(4, final.ticket.calificacion); assertNotNull(final.ticket.fechaCierre)
                assertEquals("ADMIN", final.comentarios.single { it.texto == respuesta }.autorRol)
            }
        } finally {
            runBlocking {
                // Solo se retira el registro de prueba creado en este recorrido.
                val ids = ticketId?.let { listOf(it) } ?: app.repository.observarTodosLosTickets().first().filter { it.ticket.titulo == titulo }.map { it.ticket.id }
                ids.forEach { app.db.openHelper.writableDatabase.execSQL("DELETE FROM tickets WHERE id = ?", arrayOf(it)) }
                app.preferencias.guardarRecibirTickets(recepcionOriginal)
            }
        }
    }

    @Test
    fun redisenioNavegacionPorRolesModoOscuroYEvidenciasReales() {
        val oscuro = runBlocking { app.preferencias.modoOscuro.first() }
        try {
            runBlocking { app.preferencias.guardarModoOscuro(false) }
            capturaPerfil("Fase2_01_LoginCliente")
            pulsar("Soporte TI"); capturaPerfil("Fase2_02_LoginSoporte"); pulsar("Estudiante")
            completar("juan@campus.test", "1234"); pulsar("Ingresar")
            esperar { existe("Mis tickets") && existe("2 abiertos") }
            capturaPerfil("Fase2_03_MisTickets")
            pulsarTag("nav_cliente_1"); esperar { existe("Nuevo ticket") && existe("Crear ticket") }
            capturaPerfil("Fase2_04_NuevoTicket")
            pulsarDescripcion("Volver a mis tickets"); esperar { existe("Mis tickets") }
            pulsar("Resueltos"); esperar { existe("Error al abrir el editor") }; pulsar("Error al abrir el editor")
            mostrarEnDetalle("Tu incidencia está resuelta"); capturaPerfil("Fase2_05_Resuelto")
            pulsar("Calificar y cerrar"); pulsarDescripcion("4 estrellas"); capturaPerfil("Fase2_06_Calificacion")
            pulsar("Cancelar"); pulsarDescripcion("Volver a mis tickets")
            pulsar("Todos"); esperar { existe("Wi-Fi sin conexión") }; pulsar("Wi-Fi sin conexión")
            esperar { existe("Conversación") }; capturaPerfil("Fase2_07_Detalle")
            pulsarTag("nav_cliente_2"); esperar { existe("juan@campus.test") }; capturaPerfil("Fase2_08_Perfil")
            mostrarControlSoporte("perfil_lista", "perfil_oscuro"); pulsarTag("perfil_oscuro")
            esperar { runBlocking { app.preferencias.modoOscuro.first() } }
            inicioListaSoporte("perfil_lista"); capturaPerfil("Fase2_09_PerfilOscuro")
            pulsarTag("nav_cliente_0"); esperar { existe("Mis tickets") }; capturaPerfil("Fase2_10_TicketsOscuro")
            pulsarTag("nav_cliente_2"); mostrarControlSoporte("perfil_lista", "perfil_cambiar")
            pulsarTag("perfil_cambiar"); pulsar("Cambiar perfil"); esperar { existe("Panel de soporte") }
            capturaPerfil("Fase2_11_PanelOscuro")
            pulsarTag("nav_admin_3"); esperar { existe("soporte@campus.test") }
            mostrarControlSoporte("perfil_lista", "perfil_oscuro"); pulsarTag("perfil_oscuro")
            esperar { !runBlocking { app.preferencias.modoOscuro.first() } }
            inicioListaSoporte("perfil_lista"); capturaPerfil("Fase2_12_Ajustes")
            pulsarTag("nav_admin_0"); esperar { existe("Panel de soporte") }
            capturaPerfil("Fase2_13_Panel")
            mostrarResumen("Software: 2"); mostrarResumen("Impresión: 1"); capturaPerfil("Fase2_14_Graficos")
            pulsarTag("nav_admin_1"); esperar { existe("Bandeja de tickets") && existe("6 resultados") }
            capturaPerfil("Fase2_15_Bandeja")
            pulsar("Wi-Fi sin conexión"); esperar { existe("Estudiante: Juan") }; capturaPerfil("Fase2_16_Atender")
            pulsarDescripcion("Volver al panel"); esperar { existe("Bandeja de tickets") }
            pulsarTag("nav_admin_2"); esperar { existe("Hardware") }; capturaPerfil("Fase2_17_Categorias")
            pulsarDescripcion("Agregar categoría"); esperar { existe("Nueva categoría") }
            campoNuevo("categoria_nombre", "Laboratorios"); pulsarTag("categoria_icono_pc")
            capturaPerfil("Fase2_18_NuevaCategoria")
            pulsarDescripcion("Volver a categorías"); esperar { existe("Hardware") }
            val hardware = runBlocking { app.repository.observarCategorias().first().first { it.nombre == "Hardware" } }
            pulsarTag("categoria_editar_${hardware.id}"); esperar { existe("Editar categoría") }
            capturaPerfil("Fase2_19_EditarCategoria")
            // No submit: visiting forms and changing stars must not mutate Room.
            assertEquals(6, runBlocking { app.repository.observarTodosLosTickets().first().size })
            assertFalse(runBlocking { app.repository.observarCategorias().first().any { it.nombre == "Laboratorios" } })
        } finally {
            runBlocking { app.preferencias.guardarModoOscuro(oscuro) }
        }
    }
}
