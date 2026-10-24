package com.tecsup.ayudacampus.ui.cliente

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.model.Prioridad
import com.tecsup.ayudacampus.viewmodel.NuevoTicketViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NuevoTicketScreen(vm: NuevoTicketViewModel, onVolver: () -> Unit, onGuardado: (Long) -> Unit) {
    val datos by vm.formulario.collectAsStateWithLifecycle()
    val errores by vm.errores.collectAsStateWithLifecycle()
    val categorias by vm.categorias.collectAsStateWithLifecycle()
    val cargando by vm.cargandoCategorias.collectAsStateWithLifecycle()
    val guardando by vm.guardando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val recibiendo by vm.recibirTickets.collectAsStateWithLifecycle()
    val creado by vm.ticketCreado.collectAsStateWithLifecycle()
    var desplegado by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    LaunchedEffect(creado) { creado?.let(onGuardado) }
    BackHandler(enabled = guardando) { /* Esperar a que Room confirme el guardado. */ }
    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver, enabled = !guardando) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver a mis tickets")
                }
                Text("Nuevo ticket", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 2.dp) {
                Button(
                    onClick = { focus.clearFocus(); vm.enviar() },
                    enabled = !guardando && !cargando && creado == null && recibiendo == true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).heightIn(min = 52.dp).testTag("crearTicket"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(if (guardando) "Creando ticket…" else "Crear ticket", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (recibiendo == false) Text("Mesa de ayuda cerrada", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            Text("Cuéntanos qué sucede", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Describe la incidencia para que Soporte TI pueda ayudarte.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ExposedDropdownMenuBox(
                expanded = desplegado,
                onExpandedChange = { if (!guardando && !cargando) desplegado = !desplegado }
            ) {
                OutlinedTextField(
                    value = categorias.firstOrNull { it.id == datos.categoriaId }?.nombre ?: "",
                    onValueChange = {}, readOnly = true, enabled = !guardando && !cargando,
                    label = { Text("Categoría") }, placeholder = { Text("Selecciona una categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().testTag("nuevo_categoria"),
                    shape = RoundedCornerShape(14.dp), isError = errores.categoria != null,
                    supportingText = errores.categoria?.let { mensaje -> { Text(mensaje) } }
                )
                ExposedDropdownMenu(expanded = desplegado, onDismissRequest = { desplegado = false }) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(text = { Text(categoria.nombre) }, onClick = {
                            vm.seleccionarCategoria(categoria.id)
                            desplegado = false
                        })
                    }
                }
            }
            if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
            else if (categorias.isEmpty() && error == null) {
                Text("No hay categorías activas disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CampoTicket("Título", datos.titulo, vm::actualizarTitulo, errores.titulo, "Ej. Wi-Fi sin conexión", !guardando, "nuevo_titulo")
            CampoTicket("Descripción", datos.descripcion, vm::actualizarDescripcion, errores.descripcion, "¿Qué ocurre y desde cuándo?", !guardando, "nuevo_descripcion", multilinea = true)
            CampoTicket("Ubicación", datos.ubicacion, vm::actualizarUbicacion, errores.ubicacion, "Ej. Laboratorio 3 · Pabellón B", !guardando, "nuevo_ubicacion")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Prioridad", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Prioridad.entries.forEach { prioridad ->
                        FilterChip(
                            selected = datos.prioridad == prioridad,
                            onClick = { vm.seleccionarPrioridad(prioridad) }, enabled = !guardando,
                            label = { Text(prioridad.name) }, modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
            if (error != null) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(error!!, Modifier.fillMaxWidth().padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CampoTicket(
    etiqueta: String, valor: String, onCambio: (String) -> Unit, error: String?,
    ayuda: String, habilitado: Boolean, tag: String, multilinea: Boolean = false
) {
    OutlinedTextField(
        value = valor, onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth().testTag(tag),
        label = { Text(etiqueta) }, placeholder = { Text(ayuda) },
        enabled = habilitado, singleLine = !multilinea, minLines = if (multilinea) 3 else 1,
        shape = RoundedCornerShape(14.dp), isError = error != null,
        supportingText = error?.let { mensaje -> { Text(mensaje) } }
    )
}
