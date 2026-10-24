package com.tecsup.ayudacampus.ui.soporte

import androidx.compose.runtime.Composable
import com.tecsup.ayudacampus.ui.cliente.PerfilScreen
import com.tecsup.ayudacampus.viewmodel.PerfilViewModel

@Composable
fun AjustesScreen(vm: PerfilViewModel, onVolver: () -> Unit, onCerrarSesion: () -> Unit,
                  onCambiarPerfil: () -> Unit, ocupado: Boolean, errorSesion: String?) {
    PerfilScreen(vm, onVolver, onCerrarSesion, onCambiarPerfil, ocupado, errorSesion, soporte = true)
}
