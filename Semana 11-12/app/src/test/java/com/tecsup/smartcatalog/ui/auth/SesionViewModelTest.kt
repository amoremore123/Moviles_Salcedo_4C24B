package com.tecsup.smartcatalog.ui.auth

import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.auth.*
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {
    private val modelos = mutableListOf<SesionViewModel>()
    private val sesion = Sesion("uid-simulado", "simulado@example.invalid", Rol.USUARIO)
    private val clave = "valor-exclusivo-de-prueba"
    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun limpiar() { modelos.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }
    private fun crear(repo: Repo = Repo()) = SesionViewModel(repo).also { modelos.add(it) }

    @Test fun verificacionPendienteNoPublicaLaSesionGuardada() = runTest {
        val r = Repo(); r.actual = sesion; r.pendienteRestaurar = CompletableDeferred()
        val vm = crear(r); runCurrent()
        assertEquals(EstadoSesion.Verificando, vm.estado.value)
        r.pendienteRestaurar!!.complete(Unit); runCurrent()
        assertTrue(vm.estado.value is EstadoSesion.Usuario)
    }
    @Test fun sinUsuarioPersistidoMuestraLogin() = runTest {
        val vm = crear(); runCurrent(); assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }
    @Test fun restauraUsuarioVerificado() = runTest {
        val r = Repo(); r.actual = sesion; val vm = crear(r); runCurrent()
        assertEquals(sesion, (vm.estado.value as EstadoSesion.Usuario).sesion); assertEquals(1, r.consultas)
    }
    @Test fun restauraAdminVerificado() = runTest {
        val r = Repo(); r.actual = sesion.copy(rol = Rol.ADMIN); val vm = crear(r); runCurrent()
        assertTrue(vm.estado.value is EstadoSesion.Administrador)
    }
    @Test fun loginUsuarioEsperaResultadoAntesDePublicarAcceso() = runTest {
        val r = Repo(); r.pendienteLogin = CompletableDeferred(); val vm = crear(r); runCurrent()
        vm.iniciarSesion("  ${sesion.correo}  ", clave); runCurrent()
        assertEquals(EstadoSesion.Iniciando, vm.estado.value); assertEquals(sesion.correo, r.correoRecibido)
        r.pendienteLogin!!.complete(Unit); runCurrent(); assertTrue(vm.estado.value is EstadoSesion.Usuario)
    }
    @Test fun loginAdminDependeDelRolNoDelCorreo() = runTest {
        val r = Repo(); r.rol = Rol.ADMIN; val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); runCurrent(); assertTrue(vm.estado.value is EstadoSesion.Administrador)
    }
    @Test fun camposVaciosNoLleganAlRepositorio() = runTest {
        val r = Repo(); val vm = crear(r); runCurrent(); vm.iniciarSesion(" ", " "); runCurrent()
        val e = vm.estado.value as EstadoSesion.Error
        assertNotNull(e.errorCorreo); assertNotNull(e.errorClave); assertEquals(0, r.intentos)
    }
    @Test fun correoInvalidoNoLlegaAlRepositorio() = runTest {
        val r = Repo(); val vm = crear(r); runCurrent()
        for (correo in listOf("sin-arroba", "a@b", "a b@dominio.test", "a@@dominio.test")) {
            vm.iniciarSesion(correo, clave); assertNotNull((vm.estado.value as EstadoSesion.Error).errorCorreo)
        }
        assertEquals(0, r.intentos)
    }
    @Test fun credencialesIncorrectasMuestranMensajeSeguro() = runTest {
        val r = Repo(); r.error = ErrorAutenticacion(TipoErrorAutenticacion.CREDENCIALES); val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); runCurrent()
        assertTrue((vm.estado.value as EstadoSesion.Error).mensaje.contains("credenciales"))
    }
    @Test fun perfilAusenteNoConcedeAcceso() = runTest {
        val r = Repo(); r.error = ErrorAutenticacion(TipoErrorAutenticacion.PERFIL_AUSENTE); val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); runCurrent(); assertTrue(vm.estado.value is EstadoSesion.Error)
    }
    @Test fun rolInvalidoNoConcedeAcceso() = runTest {
        val r = Repo(); r.error = ErrorAutenticacion(TipoErrorAutenticacion.PERFIL_INVALIDO); val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); runCurrent(); assertTrue(vm.estado.value is EstadoSesion.Error)
    }
    @Test fun falloFirestoreInformaConexionSinPublicarSesion() = runTest {
        val r = Repo(); r.error = ErrorAutenticacion(TipoErrorAutenticacion.RED); val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); runCurrent()
        assertTrue((vm.estado.value as EstadoSesion.Error).mensaje.contains("conexión"))
    }
    @Test fun falloDesconocidoNoFiltraMensajeInterno() = runTest {
        val r = Repo(); r.error = IllegalStateException("detalle-$clave"); val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); runCurrent()
        assertFalse((vm.estado.value as EstadoSesion.Error).mensaje.contains(clave))
    }
    @Test fun nuevaInstanciaConsultaOtraVezLaSesionEnElRepositorio() = runTest {
        val r = Repo(); val primero = crear(r); runCurrent(); primero.iniciarSesion(sesion.correo, clave); runCurrent()
        primero.viewModelScope.cancel(); val segundo = crear(r)
        assertEquals(EstadoSesion.Verificando, segundo.estado.value); runCurrent()
        assertTrue(segundo.estado.value is EstadoSesion.Usuario); assertEquals(2, r.consultas)
    }
    @Test fun cerrarSesionLimpiaRolYLaIdentidadDelRepositorio() = runTest {
        val r = Repo(); r.actual = sesion.copy(rol = Rol.ADMIN); val vm = crear(r); runCurrent(); vm.cerrarSesion()
        assertEquals(EstadoSesion.SinSesion, vm.estado.value); assertNull(r.actual); assertEquals(1, r.cierres)
    }
    @Test fun logoutInvalidaRestauracionPendienteInclusoSiNoCooperaConCancelacion() = runTest {
        val r = Repo(); r.actual = sesion; r.pendienteRestaurar = CompletableDeferred(); r.ignorarCancelacion = true
        val vm = crear(r); runCurrent(); vm.cerrarSesion(); r.pendienteRestaurar!!.complete(Unit); runCurrent()
        assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }
    @Test fun logoutInvalidaLoginPendienteInclusoSiNoCooperaConCancelacion() = runTest {
        val r = Repo(); r.pendienteLogin = CompletableDeferred(); r.ignorarCancelacion = true; val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); runCurrent(); vm.cerrarSesion(); r.pendienteLogin!!.complete(Unit); runCurrent()
        assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }
    @Test fun doblePulsacionNoDuplicaLaAutenticacion() = runTest {
        val r = Repo(); r.pendienteLogin = CompletableDeferred(); val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); vm.iniciarSesion(sesion.correo, clave); runCurrent(); assertEquals(1, r.intentos)
        r.pendienteLogin!!.complete(Unit); runCurrent()
    }
    @Test fun noIniciaOtroLoginMientrasSeRecuperaSesion() = runTest {
        val r = Repo(); r.pendienteRestaurar = CompletableDeferred(); val vm = crear(r); runCurrent()
        vm.iniciarSesion(sesion.correo, clave); assertEquals(0, r.intentos)
        r.pendienteRestaurar!!.complete(Unit); runCurrent()
    }
    @Test fun falloAlRestaurarCierraSesionYMantieneFueraLasRutas() = runTest {
        val r = Repo(); r.falloRestaurar = true; val vm = crear(r); runCurrent()
        assertTrue(vm.estado.value is EstadoSesion.Error); assertEquals(1, r.cierres)
    }

    private class Repo : AuthRepository {
        var actual: Sesion? = null
        var rol = Rol.USUARIO
        var error: Exception? = null
        var consultas = 0; var intentos = 0; var cierres = 0
        var correoRecibido: String? = null
        var pendienteRestaurar: CompletableDeferred<Unit>? = null
        var pendienteLogin: CompletableDeferred<Unit>? = null
        var ignorarCancelacion = false
        var falloRestaurar = false
        private suspend fun esperar(pendiente: CompletableDeferred<Unit>?) {
            if (ignorarCancelacion) withContext(NonCancellable) { pendiente?.await() } else pendiente?.await()
        }
        override suspend fun sesionActual(): Sesion? { consultas++; esperar(pendienteRestaurar); if (falloRestaurar) error("Fallo simulado"); return actual }
        override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> {
            intentos++; correoRecibido = correo; esperar(pendienteLogin)
            error?.let { return Result.failure(it) }
            return Result.success(Sesion("uid-simulado", correo, rol).also { actual = it })
        }
        override fun cerrarSesion() { actual = null; cierres++ }
    }
}
