package com.tecsup.ayudacampus

import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.ayudacampus.data.local.DatosIniciales
import com.tecsup.ayudacampus.viewmodel.SesionViewModel
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
class SesionViewModelTest {
    private lateinit var app: AyudaApp
    private val modelos = mutableListOf<SesionViewModel>()

    @Before
    fun preparar() = runBlocking {
        app = ApplicationProvider.getApplicationContext()
        DatosIniciales.sembrar(app.db)
        app.preferencias.cerrarSesion()
    }

    @After
    fun limpiar() = runBlocking {
        modelos.forEach { it.viewModelScope.cancel() }
        app.preferencias.cerrarSesion()
    }

    private suspend fun nuevo(): SesionViewModel {
        lateinit var vm: SesionViewModel
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            vm = SesionViewModel(app.repository, app.preferencias)
        }
        modelos.add(vm)
        withTimeout(15_000) { vm.recuperandoSesion.first { !it } }
        return vm
    }

    private suspend fun ingresar(vm: SesionViewModel, correo: String, clave: String, rol: String) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { vm.login(correo, clave, rol) }
        withTimeout(15_000) { vm.cargando.first { !it } }
    }

    @Test
    fun validaCredencialesPerfilYEstudiante() = runBlocking {
        val vm = nuevo()
        ingresar(vm, "", "", "CLIENTE")
        assertEquals("Ingresa tu correo y contraseña", vm.error.value)
        ingresar(vm, "juan@campus.test", "incorrecta", "CLIENTE")
        assertEquals("Correo o contraseña incorrectos", vm.error.value)
        assertNull(vm.usuarioActual.value)
        ingresar(vm, "juan@campus.test", "1234", "ADMIN")
        assertEquals("El usuario no corresponde al perfil seleccionado", vm.error.value)
        assertEquals("", app.preferencias.perfilActivo.first())
        ingresar(vm, "juan@campus.test", "1234", "CLIENTE")
        assertNull(vm.error.value)
        assertEquals("CLIENTE", vm.usuarioActual.value?.rol)
        assertEquals(vm.usuarioActual.value!!.id, app.preferencias.usuarioId.first())
        val restaurado = nuevo()
        assertEquals(vm.usuarioActual.value, restaurado.usuarioActual.value)
    }

    @Test
    fun soporteRestauraYCierraSinBorrarAjustes() = runBlocking {
        val oscuroAntes = app.preferencias.modoOscuro.first()
        try {
            val vm = nuevo()
            ingresar(vm, "soporte@campus.test", "admin", "ADMIN")
            assertEquals("ADMIN", vm.usuarioActual.value?.rol)
            val restaurado = nuevo()
            assertEquals(vm.usuarioActual.value, restaurado.usuarioActual.value)
            app.preferencias.guardarModoOscuro(!oscuroAntes)
            withTimeout(15_000) { restaurado.modoOscuro.first { it == !oscuroAntes } }
            InstrumentationRegistry.getInstrumentation().runOnMainSync { restaurado.cerrarSesion() }
            withTimeout(15_000) { restaurado.cargando.first { !it } }
            assertNull(restaurado.usuarioActual.value)
            assertEquals("", app.preferencias.perfilActivo.first())
            assertEquals(0, app.preferencias.usuarioId.first())
            assertEquals(!oscuroAntes, app.preferencias.modoOscuro.first())
        } finally {
            app.preferencias.guardarModoOscuro(oscuroAntes)
        }
    }

    @Test
    fun limpiaSesionesInvalidas() = runBlocking {
        app.preferencias.guardarSesion("CLIENTE", Int.MAX_VALUE)
        assertNull(nuevo().usuarioActual.value)
        assertEquals("", app.preferencias.perfilActivo.first())
        val juan = requireNotNull(app.repository.login("juan@campus.test", "1234"))
        app.preferencias.guardarSesion("ADMIN", juan.id)
        assertNull(nuevo().usuarioActual.value)
        assertEquals(0, app.preferencias.usuarioId.first())
        app.preferencias.guardarSesion("OTRO", juan.id)
        assertNull(nuevo().usuarioActual.value)
        assertEquals("", app.preferencias.perfilActivo.first())
    }
}
