package com.tecsup.smartcatalog.ui.lista

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.components.Cargando
import com.tecsup.smartcatalog.ui.components.ErrorView
import com.tecsup.smartcatalog.ui.components.EstadoVacio
import com.tecsup.smartcatalog.ui.components.ItemCard
import com.tecsup.smartcatalog.ui.components.AvisoDatosGuardados

@Composable
fun ListaScreen(viewModel: ListaViewModel, imageLoader: ImageLoader, onItemClick: (Int) -> Unit, onFavoritos: () -> Unit, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val actualizando by viewModel.actualizando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeActualizacion.collectAsStateWithLifecycle()
    val sincronizacion by viewModel.sincronizacion.collectAsStateWithLifecycle()
    ListaContenido(estado, actualizando, mensaje, sincronizacion, viewModel::actualizar, viewModel::reintentar, imageLoader, onItemClick, viewModel::alternarFavorito, onFavoritos, modifier)
}

@Composable
private fun ListaContenido(
    estado: UiState<List<Item>>,
    actualizando: Boolean,
    mensaje: String?,
    sincronizacion: EstadoSincronizacion,
    onActualizar: () -> Unit,
    onReintentar: () -> Unit,
    imageLoader: ImageLoader,
    onItemClick: (Int) -> Unit,
    onAlternarFavorito: (Int) -> Unit,
    onFavoritos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoCatalogo(actualizando, onActualizar, onFavoritos) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (actualizando && estado is UiState.Exito) LinearProgressIndicator(Modifier.fillMaxWidth())
            val fallo = (sincronizacion as? EstadoSincronizacion.Fallo)?.mensaje
                ?: (sincronizacion as? EstadoSincronizacion.Actualizando)?.falloAnterior
            val mostrarGuardados = fallo != null && estado is UiState.Exito && estado.datos.isNotEmpty()
            if (mostrarGuardados) {
                AvisoDatosGuardados("No se pudo actualizar el catálogo. Mostrando productos guardados.", onActualizar, habilitado = !actualizando)
            }
            if (mensaje != null && (!mostrarGuardados || mensaje != fallo)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(mensaje, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onActualizar, enabled = !actualizando) { Text("Reintentar") }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                when (estado) {
                    UiState.Cargando -> Cargando(mensaje = "Consultando smartphones...")
                    is UiState.Error -> ErrorView(
                        mensaje = "${estado.mensaje}\n\nNecesitas Internet para descargar los productos por primera vez.",
                        onReintentar = onReintentar
                    )
                    is UiState.Exito -> {
                        if (estado.datos.isEmpty()) {
                            EstadoVacio("Sin smartphones por ahora", "El servicio no devolvió productos en esta categoría.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item(key = "encabezado") {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Nuestra colección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                                            val cantidad = estado.datos.size
                                            Text(if (cantidad == 1) "1 modelo" else "$cantidad modelos", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }
                                items(estado.datos, key = { it.id }) { item ->
                                    ItemCard(item, imageLoader, onClick = { onItemClick(item.id) }, onAlternarFavorito = { onAlternarFavorito(item.id) })
                                }
                                item(key = "nota") {
                                    Text("Importes de la API · Sin conversión de moneda", modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoCatalogo(actualizando: Boolean, onActualizar: () -> Unit, onFavoritos: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.size(44.dp)) {
                    Image(painterResource(R.drawable.ic_smartcatalog), contentDescription = null, modifier = Modifier.padding(5.dp))
                }
                Text(stringResource(R.string.app_name), modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = onActualizar, enabled = !actualizando) {
                    if (actualizando) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    else Icon(painterResource(R.drawable.ic_actualizar), contentDescription = "Actualizar catálogo", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text("Explora nuestros smartphones", style = MaterialTheme.typography.titleLarge)
                    FilledTonalButton(onClick = onFavoritos, modifier = Modifier.align(Alignment.End)) {
                        Icon(painterResource(R.drawable.ic_favorito), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Mis favoritos")
                    }
                }
            }
        }
    }
}
