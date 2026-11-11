package com.tecsup.smartcatalog.ui.admin

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.data.notificaciones.NotificacionesCatalogo
import com.tecsup.smartcatalog.domain.model.Sesion
import java.text.DateFormat
import java.util.Date

@Composable
fun PanelAdminScreen(
    panel: PanelAdminViewModel,
    notificaciones: NotificacionesCatalogo,
    sesionActual: () -> Sesion?,
    onCatalogo: () -> Unit,
    onPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val almacenamiento by panel.almacenamiento.collectAsStateWithLifecycle()
    val sincronizacion by panel.sincronizacion.collectAsStateWithLifecycle()
    val ultima by panel.ultimaSincronizacion.collectAsStateWithLifecycle()
    val cache by panel.cache.collectAsStateWithLifecycle()
    var confirmar by rememberSaveable { mutableStateOf(false) }
    val ocupado = sincronizacion == SincronizacionAdmin.EnCurso

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding().testTag("panel-admin"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Panel de administrador", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Bienvenido, administrador", style = MaterialTheme.typography.bodyMedium)
                    FilledTonalButton(onClick = onCatalogo, modifier = Modifier.fillMaxWidth()) { Text("Explorar catálogo") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onPerfil, colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current)) {
                            Text("Perfil")
                        }
                        TextButton(onClick = onCerrarSesion, colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current)) {
                            Text("Cerrar sesión")
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Almacenamiento local", style = MaterialTheme.typography.titleLarge)
                when (val datos = almacenamiento) {
                    AlmacenamientoCatalogo.Cargando -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    AlmacenamientoCatalogo.Error -> Text("No se pudo consultar el almacenamiento.", color = MaterialTheme.colorScheme.error)
                    is AlmacenamientoCatalogo.Disponible -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Contador(datos.productos, "Productos almacenados", Modifier.weight(1f))
                            Contador(datos.favoritos, "Favoritos locales", Modifier.weight(1f))
                        }
                        Text(if (datos.productos == 0) "Catálogo vacío" else "Productos disponibles en la base local")
                        Text("Los favoritos se comparten entre las cuentas de este dispositivo.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            OutlinedCard(shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mantenimiento", style = MaterialTheme.typography.titleLarge)
                    val hora = ultima?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it)) }
                    Text(hora?.let { "Última sincronización correcta: $it" } ?: "Sin sincronizaciones registradas desde que se abrió la app.")
                    when (val estado = sincronizacion) {
                        SincronizacionAdmin.Inicial -> Unit
                        SincronizacionAdmin.EnCurso -> {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text("Sincronizando catálogo…")
                        }
                        is SincronizacionAdmin.Correcta -> Text(
                            if (estado.recibidos == 0) "Sincronización completada. La API no devolvió productos."
                            else "Sincronización completada. Productos recibidos: ${estado.recibidos}."
                        )
                        is SincronizacionAdmin.Fallida -> Text(
                            if (estado.sinConexion) "Sin conexión. Se conservaron los productos y favoritos locales."
                            else "No se pudo sincronizar. Se conservaron los datos locales.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(onClick = { panel.sincronizar(sesionActual()) }, enabled = !ocupado, modifier = Modifier.fillMaxWidth()) {
                        Icon(painterResource(R.drawable.ic_actualizar), null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronizar catálogo")
                    }
                    OutlinedButton(onClick = { confirmar = true }, modifier = Modifier.fillMaxWidth()) { Text("Limpiar caché") }
                    when (cache) {
                        ResultadoCache.INICIAL -> Unit
                        ResultadoCache.LIMPIADA -> Text("Caché de imágenes limpiada.")
                        ResultadoCache.ERROR -> Text("No se pudo limpiar la caché de imágenes.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        item { PreferenciaAvisos(notificaciones) }
    }

    if (confirmar) AlertDialog(
        onDismissRequest = { confirmar = false },
        title = { Text("Limpiar caché de imágenes") },
        text = { Text("Se eliminarán las imágenes temporales en memoria. Se conservarán los productos, los favoritos y tu sesión.") },
        confirmButton = {
            TextButton(onClick = {
                confirmar = false
                panel.limpiarCache(sesionActual())
            }) { Text("Confirmar limpieza") }
        },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
    )
}

@Composable
private fun PreferenciaAvisos(notificaciones: NotificacionesCatalogo) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var habilitados by remember { mutableStateOf(notificaciones.avisosHabilitados()) }
    var puedeSolicitar by remember { mutableStateOf(notificaciones.puedeSolicitarPermiso()) }
    val actualizar = {
        habilitados = notificaciones.avisosHabilitados()
        puedeSolicitar = notificaciones.puedeSolicitarPermiso()
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { actualizar() }
    DisposableEffect(lifecycle, notificaciones) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) actualizar()
        }
        lifecycle.addObserver(observador)
        onDispose { lifecycle.removeObserver(observador) }
    }

    OutlinedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Avisos de sincronización", style = MaterialTheme.typography.titleMedium)
            Text(
                if (habilitados) "Activados. Android te avisará al guardar productos desde el panel."
                else "Opcionales. Android puede avisarte al guardar productos. El catálogo se sincroniza aunque desactives los avisos."
            )
            if (!habilitados) {
                if (puedeSolicitar) {
                    TextButton(onClick = {
                        if (notificaciones.puedeSolicitarPermiso()) {
                            notificaciones.registrarSolicitud()
                            puedeSolicitar = false
                            permiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }) { Text("Activar avisos") }
                } else {
                    TextButton(onClick = {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        }
                        context.startActivity(intent)
                    }) { Text("Ajustes de notificaciones") }
                }
            }
        }
    }
}

@Composable
private fun Contador(total: Int, etiqueta: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.Start) {
            Text(total.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(etiqueta, style = MaterialTheme.typography.bodySmall)
        }
    }
}
