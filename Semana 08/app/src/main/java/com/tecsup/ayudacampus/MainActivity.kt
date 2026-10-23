package com.tecsup.ayudacampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.ayudacampus.ui.navigation.NavAyuda
import com.tecsup.ayudacampus.ui.theme.AyudaTheme
import com.tecsup.ayudacampus.viewmodel.SesionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = application as AyudaApp
            val vm: SesionViewModel = viewModel(factory = SesionViewModel.Factory(app.repository, app.preferencias))
            val usuario by vm.usuarioActual.collectAsStateWithLifecycle()
            val oscuro by vm.modoOscuro.collectAsStateWithLifecycle()
            var rolSeleccionado by rememberSaveable { mutableStateOf("CLIENTE") }
            AyudaTheme(rol = usuario?.rol ?: rolSeleccionado, modoOscuro = oscuro) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavAyuda(vm, rolSeleccionado) { rolSeleccionado = it }
                }
            }
        }
    }
}
