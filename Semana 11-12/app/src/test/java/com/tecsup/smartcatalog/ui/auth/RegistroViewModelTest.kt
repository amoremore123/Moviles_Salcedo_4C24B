package com.tecsup.smartcatalog.ui.auth

import com.tecsup.smartcatalog.data.auth.ErrorRegistro
import com.tecsup.smartcatalog.data.auth.ServicioRegistro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {
    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun restaurar() { Dispatchers.resetMain() }

    @Test fun rechazaCamposInvalidos() {
        assertNotNull(validarRegistro(" ", "vista@example.invalid", "prueba1", "prueba1"))
        assertNotNull(validarRegistro("Vista", "incorrecto", "prueba1", "prueba1"))
        assertNotNull(validarRegistro("Vista", "vista@example.invalid", "corta", "corta"))
        assertNotNull(validarRegistro("Vista", "vista@example.invalid", "prueba1", "prueba2"))
        assertNotNull(validarRegistro("Vista", "vista@example.invalid", " prueba1", " prueba1"))
        assertNull(validarRegistro(" Vista ", " vista@example.invalid ", "prueba1", "prueba1"))
    }

    @Test fun entradaDeProduccionNoHabilitaElRegistro() {
        val modelo = RegistroViewModel()
        modelo.iniciar("Vista", "vista@example.invalid", "prueba1", "prueba1")
        assertFalse(modelo.disponible)
        assertFalse(modelo.estado.value.completo)
        assertTrue(modelo.estado.value.mensaje!!.contains("servidor"))
    }

    @Test fun requiereVerificarElCorreoYReintentaElPerfilSinRecrearLaCuenta() = runTest {
        var cuentas = 0
        var enlaces = 0
        var verificado = false
        var escrituras = 0
        var pendiente: String? = null
        val servicio = object : ServicioRegistro {
            override fun recuperarNombrePendiente() = pendiente
            override suspend fun crearCuenta(nombre: String, correo: String, clave: String) {
                cuentas++
                pendiente = nombre
            }
            override suspend fun enviarVerificacion() { enlaces++ }
            override suspend fun correoVerificado() = verificado
            override suspend fun finalizarPerfil(nombre: String) {
                assertEquals("Vista", nombre)
                escrituras++
                if (escrituras == 1) throw ErrorRegistro("Falló Firestore.")
                pendiente = null
            }
        }
        val modelo = RegistroViewModel(servicio)
        modelo.iniciar(" Vista ", "vista@example.invalid", "prueba1", "prueba1")
        assertTrue(modelo.estado.value.cargando)
        runCurrent()
        assertTrue(modelo.estado.value.pendiente)
        val reabierto = RegistroViewModel(servicio)
        assertTrue(reabierto.estado.value.pendiente)
        reabierto.iniciar("Vista", "vista@example.invalid", "prueba1", "prueba1")
        reabierto.comprobarCorreo()
        runCurrent()
        assertEquals(0, escrituras)
        verificado = true
        reabierto.comprobarCorreo()
        runCurrent()
        assertTrue(reabierto.estado.value.pendiente)
        assertFalse(reabierto.estado.value.completo)
        val trasFallo = RegistroViewModel(servicio)
        trasFallo.comprobarCorreo()
        runCurrent()
        assertTrue(trasFallo.estado.value.completo)
        assertFalse(RegistroViewModel(servicio).estado.value.pendiente)
        assertEquals(1, cuentas)
        assertEquals(1, enlaces)
        assertEquals(2, escrituras)
    }
}
