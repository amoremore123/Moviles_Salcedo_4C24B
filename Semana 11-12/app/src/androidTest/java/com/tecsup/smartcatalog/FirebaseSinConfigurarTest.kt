package com.tecsup.smartcatalog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.tecsup.smartcatalog.data.auth.ErrorAutenticacion
import com.tecsup.smartcatalog.data.auth.TipoErrorAutenticacion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseSinConfigurarTest {
    @Test fun authSinConfiguracionRespondeSinInicializarUnaAppFirebaseFicticia() = runBlocking {
        assumeFalse("Este caso comprueba la variante sin google-services.json", BuildConfig.FIREBASE_CONFIGURADO)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = AppContainer(context).authRepository
        assertNull(repo.sesionActual())
        val resultado = repo.iniciarSesion("simulado@example.invalid", "valor-exclusivo-de-prueba")
        assertEquals(TipoErrorAutenticacion.CONFIGURACION, (resultado.exceptionOrNull() as ErrorAutenticacion).tipo)
        repo.cerrarSesion()
        assertTrue(FirebaseApp.getApps(context).isEmpty())
    }
}
