package com.tecsup.ayudacampus.ui.soporte

import com.tecsup.ayudacampus.ui.components.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.tecsup.ayudacampus.ui.components.IconoCategoria
import com.tecsup.ayudacampus.viewmodel.CategoriasViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun FormularioCategoriaScreen(vm: CategoriasViewModel, onVolver: () -> Unit, onGuardado: () -> Unit) {
    val pantalla by vm.pantalla.collectAsStateWithLifecycle()
    val categoria = pantalla.formulario
    val focus = LocalFocusManager.current
    LaunchedEffect(pantalla.guardado) { if (pantalla.guardado) onGuardado() }
    BackHandler(enabled = pantalla.operando) { }
    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AyudaTopAppBar(if (categoria.id == 0) "Nueva categoría" else "Editar categoría", onVolver, "Volver a categorías", !pantalla.operando)
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pantalla.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                if (pantalla.operando) LinearProgressIndicator(Modifier.fillMaxWidth())
                Button(onClick = { focus.clearFocus(); vm.guardar() }, enabled = !pantalla.operando && !pantalla.cargando && pantalla.errorCarga == null && !pantalla.guardado,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("categoria_guardar")) { Text("Guardar categoría") }
            }
        }
    ) { padding ->
        when {
            pantalla.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            pantalla.errorCarga != null -> Column(Modifier.padding(padding).padding(24.dp)) {
                Text(pantalla.errorCarga!!)
                OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
            }
            else -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("Define cómo se mostrará esta categoría al crear y consultar tickets.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = categoria.nombre, onValueChange = vm::actualizarNombre,
                    label = { Text("Nombre de la categoría") }, singleLine = true, enabled = !pantalla.operando,
                    modifier = Modifier.fillMaxWidth().testTag("categoria_nombre"), shape = RoundedCornerShape(14.dp))
                Text("Icono", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                val opciones = listOf("pc" to "Equipo", "code" to "Software", "wifi" to "Wi-Fi", "key" to "Cuenta", "printer" to "Impresora", "tag" to "Etiqueta")
                opciones.chunked(3).forEach { fila ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { (codigo, etiqueta) ->
                            FilterChip(selected = categoria.icono == codigo, onClick = { vm.seleccionarIcono(codigo) }, enabled = !pantalla.operando,
                                label = { Text(etiqueta) }, modifier = Modifier.weight(1f).testTag("categoria_icono_$codigo"))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Activa", style = MaterialTheme.typography.titleMedium)
                        Text("Disponible para tickets nuevos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = categoria.activa, onCheckedChange = vm::establecerActiva, enabled = !pantalla.operando, modifier = Modifier.testTag("categoria_form_activa"))
                }
                Text("Vista previa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconoCategoria(categoria.icono, categoria.nombre.ifBlank { "Nombre de categoría" })
                        Column {
                            Text(categoria.nombre.ifBlank { "Nombre de categoría" }, style = MaterialTheme.typography.titleMedium)
                            Text(if (categoria.activa) "Activa" else "Inactiva", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
