package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.data.auth.*
import com.tecsup.smartcatalog.domain.model.Rol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** Todos los valores son dobles reservados de prueba; no representan cuentas ni contraseñas del laboratorio. */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryFirebaseTest {
    private val correo = "cuenta-simulada@example.invalid"
    private val claveSimulada = "valor-exclusivo-de-prueba"
    private fun entorno(rol: String? = "usuario"): Entorno {
        val auth = AuthFalso(IdentidadFirebase("uid-simulado", correo))
        val perfiles = PerfilesFalsos(PerfilFirebase(correo, rol))
        return Entorno(auth, perfiles, AuthRepositoryFirebase(auth, perfiles))
    }
    private fun tipo(resultado: Result<*>) = (resultado.exceptionOrNull() as ErrorAutenticacion).tipo

    @Test fun inicioExitosoDevuelveUsuarioYConsultaElUidAutenticado() = runTest {
        val e = entorno()
        val sesion = e.repo.iniciarSesion(correo, claveSimulada).getOrThrow()
        assertEquals("uid-simulado", sesion.uid); assertEquals(correo, sesion.correo); assertEquals(Rol.USUARIO, sesion.rol)
        assertEquals(listOf("uid-simulado"), e.perfiles.uids)
    }

    @Test fun adminProvieneDelPerfilYNoDelCorreo() = runTest {
        val e = entorno("admin") // Mismo correo que la prueba de rol usuario.
        assertEquals(Rol.ADMIN, e.repo.iniciarSesion(correo, claveSimulada).getOrThrow().rol)
    }

    @Test fun credencialesIncorrectasNoConsultanPerfilNiDejanSesion() = runTest {
        val e = entorno(); e.auth.fallo = ErrorAutenticacion(TipoErrorAutenticacion.CREDENCIALES)
        assertEquals(TipoErrorAutenticacion.CREDENCIALES, tipo(e.repo.iniciarSesion(correo, claveSimulada)))
        assertTrue(e.perfiles.uids.isEmpty()); assertNull(e.auth.uidActual)
    }

    @Test fun documentoInexistenteRechazaLaCuentaYCierraAuth() = runTest {
        val e = entorno(); e.perfiles.perfil = null
        assertEquals(TipoErrorAutenticacion.PERFIL_AUSENTE, tipo(e.repo.iniciarSesion(correo, claveSimulada)))
        assertNull(e.auth.uidActual)
    }

    @Test fun rolesAusentesInvalidosOMalFormadosNoConcedenAdmin() = runTest {
        for (rol in listOf(null, "", "ADMIN", "admin ", "root")) {
            val e = entorno(rol)
            assertEquals(TipoErrorAutenticacion.PERFIL_INVALIDO, tipo(e.repo.iniciarSesion(correo, claveSimulada)))
            assertNull(e.auth.uidActual)
        }
    }

    @Test fun perfilDeOtroCorreoNoConcedePermisos() = runTest {
        val e = entorno("admin"); e.perfiles.perfil = PerfilFirebase("otro-simulado@example.invalid", "admin")
        assertEquals(TipoErrorAutenticacion.PERFIL_INVALIDO, tipo(e.repo.iniciarSesion(correo, claveSimulada)))
    }

    @Test fun falloDeRedEnAuthSeDevuelveSeguro() = runTest {
        val e = entorno(); e.auth.fallo = ErrorAutenticacion(TipoErrorAutenticacion.RED)
        assertEquals(TipoErrorAutenticacion.RED, tipo(e.repo.iniciarSesion(correo, claveSimulada)))
        assertTrue(e.perfiles.uids.isEmpty())
    }

    @Test fun falloDeRedEnFirestoreNoPublicaElUsuarioAutenticado() = runTest {
        val e = entorno("admin"); e.perfiles.fallo = ErrorAutenticacion(TipoErrorAutenticacion.RED)
        assertEquals(TipoErrorAutenticacion.RED, tipo(e.repo.iniciarSesion(correo, claveSimulada)))
        assertNull(e.auth.uidActual)
    }

    @Test fun cerrarSesionEliminaLaIdentidadYNoConsultaFirestore() = runTest {
        val e = entorno(); e.repo.iniciarSesion(correo, claveSimulada).getOrThrow()
        e.repo.cerrarSesion(); assertNull(e.repo.sesionActual()); assertNull(e.auth.uidActual)
        assertEquals(1, e.perfiles.uids.size)
    }

    @Test fun sesionActualVerificaLaIdentidadYElRolNuevamente() = runTest {
        val e = entorno(); e.auth.actual = e.auth.identidad
        assertEquals(Rol.USUARIO, e.repo.sesionActual()?.rol)
        assertEquals(1, e.auth.consultasActual)
        assertEquals(listOf("uid-simulado"), e.perfiles.uids)
    }

    @Test fun unAdminAnteriorNoSeRecuperaDesdeMemoriaSiFallaElServidor() = runTest {
        val e = entorno("admin"); e.repo.iniciarSesion(correo, claveSimulada).getOrThrow()
        e.perfiles.fallo = ErrorAutenticacion(TipoErrorAutenticacion.RED)
        assertNull(e.repo.sesionActual()); assertNull(e.auth.uidActual)
        assertEquals(2, e.perfiles.uids.size)
    }

    @Test fun falloDesconocidoNoFiltraDetallesNiConservaLaCausa() = runTest {
        val e = entorno(); e.auth.fallo = IllegalStateException("detalle-interno-$claveSimulada-$correo")
        val error = e.repo.iniciarSesion(correo, claveSimulada).exceptionOrNull()!!
        assertEquals(TipoErrorAutenticacion.GENERAL, (error as ErrorAutenticacion).tipo)
        assertFalse(error.message.orEmpty().contains(claveSimulada)); assertFalse(error.message.orEmpty().contains(correo)); assertNull(error.cause)
    }

    @Test fun cerrarSesionDuranteLaConsultaDeRolInvalidaElLoginPendiente() = runTest {
        val e = entorno("admin"); val pendiente = CompletableDeferred<Unit>(); e.perfiles.pendiente = pendiente
        val resultado = async { e.repo.iniciarSesion(correo, claveSimulada) }
        runCurrent(); e.repo.cerrarSesion(); pendiente.complete(Unit)
        assertEquals(TipoErrorAutenticacion.SESION_CAMBIADA, tipo(resultado.await())); assertNull(e.auth.uidActual)
    }

    @Test fun camposVaciosSeRechazanAntesDeEnviarCredenciales() = runTest {
        val e = entorno()
        assertEquals(TipoErrorAutenticacion.CREDENCIALES, tipo(e.repo.iniciarSesion(" ", claveSimulada)))
        assertEquals(TipoErrorAutenticacion.CREDENCIALES, tipo(e.repo.iniciarSesion(correo, " ")))
        assertEquals(0, e.auth.intentos)
    }

    @Test fun cancelacionSePropagaYNoDevuelveUnaSesionParcial() = runTest {
        val e = entorno(); e.perfiles.fallo = CancellationException("Cancelación simulada")
        try { e.repo.iniciarSesion(correo, claveSimulada); fail("Debe propagar cancelación") }
        catch (_: CancellationException) { assertNull(e.auth.uidActual) }
    }

    private data class Entorno(val auth: AuthFalso, val perfiles: PerfilesFalsos, val repo: AuthRepositoryFirebase)
    private class AuthFalso(val identidad: IdentidadFirebase) : ProveedorAutenticacion {
        var actual: IdentidadFirebase? = null
        var fallo: Exception? = null
        var intentos = 0
        var consultasActual = 0
        override val uidActual get() = actual?.uid
        override suspend fun autenticar(correo: String, clave: String): IdentidadFirebase {
            intentos++; fallo?.let { throw it }; actual = identidad; return identidad
        }
        override suspend fun usuarioActual(): IdentidadFirebase? { consultasActual++; fallo?.let { throw it }; return actual }
        override fun cerrarSesion() { actual = null }
    }
    private class PerfilesFalsos(var perfil: PerfilFirebase?) : ProveedorPerfiles {
        var fallo: Exception? = null
        var pendiente: CompletableDeferred<Unit>? = null
        val uids = mutableListOf<String>()
        override suspend fun obtener(uid: String): PerfilFirebase? { uids.add(uid); pendiente?.await(); fallo?.let { throw it }; return perfil }
    }
}
