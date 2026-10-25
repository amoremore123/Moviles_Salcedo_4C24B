package com.tecsup.ayudacampus.ui.cliente

import com.tecsup.ayudacampus.ui.components.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.data.local.entity.Comentario
import com.tecsup.ayudacampus.ui.components.EstadoChip
import com.tecsup.ayudacampus.ui.components.PrioridadChip
import com.tecsup.ayudacampus.viewmodel.DetalleTicketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DetalleTicketScreen(vm: DetalleTicketViewModel, onVolver: () -> Unit) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val detalle = estado.detalle
    var dialogo by rememberSaveable { mutableStateOf(false) }
    var estrellas by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(detalle?.ticket?.estado) {
        if (detalle?.ticket?.estado != "RESUELTO") dialogo = false
    }
    BackHandler(enabled = estado.operando) { }
    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BarraInferior(!estado.operando) },
        topBar = {
            AyudaTopAppBar("Detalle del ticket #${detalle?.ticket?.id?.toString()?.padStart(4, '0') ?: "----"}", onVolver, "Volver a mis tickets", !estado.operando)
        }
    ) { padding ->
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            detalle == null -> Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text(estado.errorCarga ?: "Ticket no disponible", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding).testTag("detalle_lista"),
                contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(detalle.ticket.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstadoChip(detalle.ticket.estado)
                            PrioridadChip(detalle.ticket.prioridad)
                        }
                        Text("${detalle.categoria.nombre} · ${detalle.ticket.ubicacion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Estudiante: ${detalle.cliente.nombre}", style = MaterialTheme.typography.bodyMedium)
                        Text("Creado: ${fechaDetalle(detalle.ticket.fecha)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                            Text(detalle.ticket.descripcion, Modifier.fillMaxWidth().padding(16.dp))
                        }
                        StepperTicket(detalle.ticket.estado)
                    }
                    }
                }
                if (detalle.ticket.estado == "RESUELTO") item {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF16A34A).copy(alpha = .14f)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Tu incidencia está resuelta", color = colorAccesible(Color(0xFF16A34A)), fontWeight = FontWeight.Bold)
                            Text("Puedes dejar un comentario final, calificar la atención y cerrar, o reabrir si el problema continúa.", color = colorAccesible(Color(0xFF16A34A)))
                            Button(onClick = { estrellas = 0; dialogo = true }, enabled = !estado.operando) { Text("Calificar y cerrar") }
                            OutlinedButton(onClick = vm::reabrir, enabled = !estado.operando) { Text("Reabrir ticket") }
                        }
                    }
                }
                if (detalle.ticket.estado == "CERRADO") item {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Ticket cerrado", fontWeight = FontWeight.Bold)
                            Text("Calificación: ${detalle.ticket.calificacion ?: "Sin calificación"} / 5")
                            detalle.ticket.fechaCierre?.let { Text("Cerrado: ${fechaDetalle(it)}", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
                item { Text("Conversación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                if (detalle.comentarios.isEmpty()) item { Text("Aún no hay comentarios", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(detalle.comentarios.sortedWith(compareBy<Comentario> { it.fecha }.thenBy { it.id }), key = { it.id }) { comentario ->
                    BurbujaComentario(comentario, propia = comentario.autorId == detalle.cliente.id)
                }
                if (detalle.ticket.estado != "CERRADO") item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = estado.texto, onValueChange = vm::actualizarComentario,
                            modifier = Modifier.fillMaxWidth().testTag("detalle_comentario"),
                            label = { Text("Escribe un comentario") }, minLines = 2,
                            enabled = !estado.operando, shape = RoundedCornerShape(14.dp)
                        )
                        Button(onClick = { vm.comentar() }, enabled = !estado.operando,
                            modifier = Modifier.fillMaxWidth().testTag("detalle_enviar")) { Text("Enviar comentario") }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (estado.operando) LinearProgressIndicator(Modifier.fillMaxWidth())
                        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        estado.confirmacion?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
        }
    }
    if (dialogo && detalle?.ticket?.estado == "RESUELTO") {
        AlertDialog(
            onDismissRequest = { if (!estado.operando) dialogo = false },
            title = { Text("Calificar y cerrar ticket") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("¿Cómo fue la atención? Selecciona de 1 a 5 estrellas para confirmar el cierre.")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        (1..5).forEach { valor ->
                            IconButton(onClick = { estrellas = valor }, enabled = !estado.operando, modifier = Modifier.weight(1f).semantics(mergeDescendants = true) { contentDescription = "$valor estrellas"; selected = valor == estrellas }) {
                                Text(if (valor <= estrellas) "★" else "☆", style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text("Calificación seleccionada: $estrellas / 5")
                    estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.cerrar(estrellas) }, enabled = estrellas in 1..5 && !estado.operando) {
                    Text(if (estado.operando) "Cerrando…" else "Confirmar y cerrar")
                }
            },
            dismissButton = { TextButton(onClick = { dialogo = false }, enabled = !estado.operando) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun StepperTicket(estado: String) {
    val pasos = listOf("ABIERTO" to "Abierto", "EN_PROCESO" to "En proceso", "RESUELTO" to "Resuelto", "CERRADO" to "Cerrado")
    val actual = pasos.indexOfFirst { it.first == estado }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        pasos.forEachIndexed { indice, paso ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val progreso by animateFloatAsState(if (indice <= actual) 1f else 0f, label = "progresoTicket")
                LinearProgressIndicator(progress = progreso, modifier = Modifier.fillMaxWidth())
                Text(paso.second, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelSmall,
                    color = if (indice <= actual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun BurbujaComentario(comentario: Comentario, propia: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (propia) Arrangement.End else Arrangement.Start) {
        Surface(modifier = Modifier.fillMaxWidth(.92f), shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = if (propia) 4.dp else 18.dp, bottomStart = if (propia) 18.dp else 4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = if (propia) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color(0xFF7C3AED).copy(alpha = .1f)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${comentario.autorNombre} · ${if (comentario.autorRol == "ADMIN") "Soporte TI" else "Estudiante"}",
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(comentario.texto)
                Text(fechaDetalle(comentario.fecha), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun fechaDetalle(fecha: Long) = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale("es", "PE")).format(Date(fecha))
