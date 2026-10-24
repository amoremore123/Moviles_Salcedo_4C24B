package com.tecsup.ayudacampus.ui.soporte

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import com.tecsup.ayudacampus.ui.components.EstadoChip
import com.tecsup.ayudacampus.ui.components.PrioridadChip
import com.tecsup.ayudacampus.viewmodel.AtenderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AtenderTicketScreen(vm: AtenderViewModel, onVolver: () -> Unit) {
    val pantalla by vm.pantalla.collectAsStateWithLifecycle()
    val detalle = pantalla.detalle
    val cerrado = detalle?.ticket?.estado == "CERRADO"
    val focus = LocalFocusManager.current
    BackHandler(enabled = pantalla.operando) { }
    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver, enabled = !pantalla.operando) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver al panel") }
                Text("Atender ticket #${detalle?.ticket?.id?.toString()?.padStart(4, '0') ?: "----"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (pantalla.operando) LinearProgressIndicator(Modifier.fillMaxWidth())
                pantalla.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                pantalla.confirmacion?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            }
        }
    ) { padding ->
        when {
            pantalla.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            detalle == null -> Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(pantalla.errorCarga ?: "Ticket no disponible")
                OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("atender_lista"),
                contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(detalle.ticket.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { EstadoChip(detalle.ticket.estado); PrioridadChip(detalle.ticket.prioridad) }
                        Text("Estudiante: ${detalle.cliente.nombre}", style = MaterialTheme.typography.titleSmall)
                        Text(detalle.cliente.correo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${detalle.categoria.nombre} · ${detalle.ticket.ubicacion}")
                        Text("Creado: ${fechaAtencion(detalle.ticket.fecha)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
                            Text(detalle.ticket.descripcion, Modifier.fillMaxWidth().padding(16.dp))
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Estado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstadoTicket.entries.forEach { estado ->
                                val etiqueta = when (estado) { EstadoTicket.ABIERTO -> "Abierto"; EstadoTicket.EN_PROCESO -> "En proceso"; EstadoTicket.RESUELTO -> "Resuelto"; EstadoTicket.CERRADO -> "Cerrado" }
                                FilterChip(selected = pantalla.estadoSeleccionado == estado,
                                    onClick = { vm.cambiarEstado(estado) }, enabled = !cerrado && !pantalla.operando && estado != EstadoTicket.CERRADO,
                                    label = { Text(etiqueta) }, modifier = Modifier.testTag("atender_estado_${estado.name}"))
                            }
                        }
                        Text("Prioridad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Prioridad.entries.forEach { prioridad ->
                                FilterChip(selected = pantalla.prioridadSeleccionada == prioridad,
                                    onClick = { vm.cambiarPrioridad(prioridad) }, enabled = !cerrado && !pantalla.operando,
                                    label = { Text(prioridad.name) }, modifier = Modifier.weight(1f).testTag("atender_prioridad_${prioridad.name}"))
                            }
                        }
                        if (cerrado) {
                            Text("Ticket cerrado: solo lectura. El cierre y la calificación corresponden al estudiante.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            detalle.ticket.calificacion?.let { Text("Calificación del estudiante: $it / 5") }
                            detalle.ticket.fechaCierre?.let { Text("Cerrado: ${fechaAtencion(it)}", style = MaterialTheme.typography.bodySmall) }
                        } else {
                            if (detalle.ticket.estado == "RESUELTO") Text("Para reabrir, selecciona Abierto y guarda los cambios.", style = MaterialTheme.typography.bodySmall)
                            Button(onClick = { focus.clearFocus(); vm.guardar() }, enabled = !pantalla.operando,
                                modifier = Modifier.fillMaxWidth().testTag("atender_guardar")) { Text("Guardar cambios") }
                        }
                    }
                }
                item { Text("Conversación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                if (detalle.comentarios.isEmpty()) item { Text("Aún no hay comentarios", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(detalle.comentarios.sortedWith(compareBy({ it.fecha }, { it.id })), key = { it.id }) { comentario ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (comentario.autorId == pantalla.autorId) Arrangement.End else Arrangement.Start) {
                        Surface(modifier = Modifier.fillMaxWidth(.92f), shape = RoundedCornerShape(16.dp),
                            color = if (comentario.autorId == pantalla.autorId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${comentario.autorNombre} · ${if (comentario.autorRol == "ADMIN") "Soporte TI" else "Estudiante"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(comentario.texto)
                                Text(fechaAtencion(comentario.fecha), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (!cerrado) item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = pantalla.respuesta, onValueChange = vm::actualizarRespuesta,
                            label = { Text("Responder al estudiante") }, minLines = 2, enabled = !pantalla.operando,
                            modifier = Modifier.fillMaxWidth().testTag("atender_respuesta"), shape = RoundedCornerShape(14.dp))
                        Button(onClick = { focus.clearFocus(); vm.responder() }, enabled = !pantalla.operando,
                            modifier = Modifier.fillMaxWidth().testTag("atender_enviar")) { Text("Enviar respuesta") }
                    }
                }
            }
        }
    }
}
private fun fechaAtencion(fecha: Long) = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale("es", "PE")).format(Date(fecha))
