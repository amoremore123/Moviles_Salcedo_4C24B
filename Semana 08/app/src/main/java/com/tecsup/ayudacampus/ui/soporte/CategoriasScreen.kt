package com.tecsup.ayudacampus.ui.soporte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.ui.components.IconoCategoria
import com.tecsup.ayudacampus.viewmodel.CategoriasViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CategoriasScreen(vm: CategoriasViewModel, onVolver: () -> Unit, onNueva: () -> Unit, onEditar: (Int) -> Unit) {
    val pantalla by vm.pantalla.collectAsStateWithLifecycle()
    var eliminar by remember { mutableStateOf<Categoria?>(null) }
    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver, enabled = !pantalla.operando) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver al panel") }
                Text("Categorías", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButton = {
            if (!pantalla.cargando && pantalla.errorCarga == null) FloatingActionButton(onClick = onNueva) { Icon(Icons.Default.Add, contentDescription = "Agregar categoría") }
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
            pantalla.errorCarga != null -> Column(Modifier.padding(padding).padding(24.dp)) {
                Text(pantalla.errorCarga!!)
                OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("categorias_lista"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("Organiza los temas de atención. Las categorías inactivas se conservan en los tickets existentes.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                if (pantalla.categorias.isEmpty()) item { Text("Todavía no hay categorías") }
                items(pantalla.categorias, key = { it.id }) { categoria ->
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { IconoCategoria(categoria.icono, categoria.nombre) }
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(categoria.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(if (categoria.activa) "Activa" else "Inactiva", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = categoria.activa, onCheckedChange = { vm.cambiarActiva(categoria, it) }, enabled = !pantalla.operando,
                                    modifier = Modifier.testTag("categoria_activa_${categoria.id}").semantics { contentDescription = "Activa: ${categoria.nombre}" })
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { onEditar(categoria.id) }, enabled = !pantalla.operando,
                                    modifier = Modifier.testTag("categoria_editar_${categoria.id}")) { Icon(Icons.Default.Edit, contentDescription = "Editar categoría ${categoria.nombre}") }
                                IconButton(onClick = { eliminar = categoria }, enabled = !pantalla.operando,
                                    modifier = Modifier.testTag("categoria_eliminar_${categoria.id}")) { Icon(Icons.Default.Delete, contentDescription = "Eliminar categoría ${categoria.nombre}", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }
    eliminar?.let { categoria ->
        AlertDialog(onDismissRequest = { eliminar = null }, title = { Text("Eliminar categoría") },
            text = { Text("¿Eliminar «${categoria.nombre}»? Solo se puede eliminar si no tiene tickets asociados.") },
            confirmButton = { TextButton(onClick = { vm.eliminar(categoria); eliminar = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { eliminar = null }) { Text("Cancelar") } })
    }
}
