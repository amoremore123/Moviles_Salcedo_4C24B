package com.tecsup.ayudacampus.ui.cliente

import com.tecsup.ayudacampus.ui.components.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.viewmodel.PerfilViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PerfilScreen(vm: PerfilViewModel, onVolver: () -> Unit, onCerrarSesion: () -> Unit,
                 onCambiarPerfil: () -> Unit, ocupado: Boolean, errorSesion: String?, soporte: Boolean = false) {
    val perfil by vm.perfil.collectAsStateWithLifecycle()
    val ajustes by vm.ajustes.collectAsStateWithLifecycle()
    val guardando by vm.guardando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val confirmacion by vm.confirmacion.collectAsStateWithLifecycle()
    val usuario = perfil.usuario
    val bloqueado = ocupado || guardando
    BackHandler(enabled = bloqueado) { }
    Scaffold(modifier = Modifier.fillMaxSize().safeDrawingPadding().semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AyudaTopAppBar(if (soporte) "Perfil y ajustes" else "Mi perfil", onVolver, "Volver al inicio", !bloqueado)
        }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).testTag("perfil_lista").padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (perfil.cargando) CircularProgressIndicator()
            else if (usuario == null || (usuario.rol == "ADMIN") != soporte) {
                Text(perfil.error ?: "Perfil no disponible", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
            } else {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(usuario.correo, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(if (soporte) "Soporte TI" else "Estudiante", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                if (!soporte) {
                    Text("Tus tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Abiertos" to perfil.abiertos, "Resueltos" to perfil.resueltos, "Totales" to perfil.total).forEach { (etiqueta, cantidad) ->
                            Surface(Modifier.weight(1f), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp)) {
                                Column(Modifier.padding(vertical = 16.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(cantidad.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(etiqueta, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
                Text("Preferencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                val valores = ajustes
                if (valores == null) LinearProgressIndicator(Modifier.fillMaxWidth()) else {
                    Preferencia("Modo oscuro", "Una apariencia cómoda para ambientes con poca luz", valores.modoOscuro, !bloqueado, "perfil_oscuro", vm::guardarModoOscuro)
                    Preferencia("Notificaciones", "Guardar tu preferencia de avisos", valores.notificaciones, !bloqueado, "perfil_notificaciones", vm::guardarNotificaciones)
                    if (soporte) Preferencia("Recibir tickets", "Permitir que los estudiantes creen nuevas solicitudes", valores.recibirTickets, !bloqueado, "perfil_recibir", vm::guardarRecibirTickets)
                }
                if (bloqueado) LinearProgressIndicator(Modifier.fillMaxWidth())
                (error ?: errorSesion)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                confirmacion?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                HorizontalDividerCompat()
                Text("Modo práctica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Cambia al otro perfil de demostración del laboratorio sin volver a ingresar credenciales.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onCambiarPerfil, enabled = !bloqueado, modifier = Modifier.fillMaxWidth().testTag("perfil_cambiar")) {
                    Text(if (soporte) "Cambiar a Estudiante" else "Cambiar a Soporte TI")
                }
                Button(onClick = onCerrarSesion, enabled = !bloqueado, modifier = Modifier.fillMaxWidth().testTag("perfil_salir")) { Text("Cerrar sesión") }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
@Composable
private fun Preferencia(titulo: String, descripcion: String, valor: Boolean, habilitado: Boolean, tag: String, cambiar: (Boolean) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = valor, onCheckedChange = cambiar, enabled = habilitado, modifier = Modifier.testTag(tag))
        }
    }
}
@Composable
private fun HorizontalDividerCompat() { Divider(color = MaterialTheme.colorScheme.outlineVariant) }
