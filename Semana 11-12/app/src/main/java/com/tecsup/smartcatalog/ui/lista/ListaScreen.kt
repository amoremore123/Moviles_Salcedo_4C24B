package com.tecsup.smartcatalog.ui.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.NOTA_PRECIOS
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.components.AvisoDatosGuardados
import com.tecsup.smartcatalog.ui.components.CabeceraMarca
import com.tecsup.smartcatalog.ui.components.Cargando
import com.tecsup.smartcatalog.ui.components.ErrorView
import com.tecsup.smartcatalog.ui.components.EstadoVacio
import com.tecsup.smartcatalog.ui.components.TarjetaCatalogo

@Composable
fun ListaScreen(viewModel: ListaViewModel, imageLoader: ImageLoader, onItemClick: (Int) -> Unit, onFavoritos: () -> Unit, modifier: Modifier = Modifier) {
    val vista by viewModel.vistaCatalogo.collectAsStateWithLifecycle()
    val actualizando by viewModel.actualizando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeActualizacion.collectAsStateWithLifecycle()
    val sincronizacion by viewModel.sincronizacion.collectAsStateWithLifecycle()
    ListaContenido(vista, viewModel::actualizarCriterios, actualizando, mensaje, sincronizacion, viewModel::actualizar, viewModel::reintentar, imageLoader, onItemClick, viewModel::alternarFavorito, modifier)
}

@Composable
private fun ListaContenido(
    vista: VistaCatalogo,
    cambiarCriterios: ((CriteriosCatalogo) -> CriteriosCatalogo) -> Unit,
    actualizando: Boolean,
    mensaje: String?,
    sincronizacion: EstadoSincronizacion,
    onActualizar: () -> Unit,
    onReintentar: () -> Unit,
    imageLoader: ImageLoader,
    onItemClick: (Int) -> Unit,
    onAlternarFavorito: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = vista.estado
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CabeceraMarca("Catálogo", acciones = {
                IconButton(onClick = onActualizar, enabled = !actualizando) {
                    if (actualizando) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    else Icon(painterResource(R.drawable.ic_actualizar), contentDescription = "Actualizar catálogo")
                }
            })
        }
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                modifier = Modifier.widthIn(max = 840.dp).fillMaxSize().testTag("lista-catalogo"),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ancho("filtros") { ControlesCatalogo(vista.criterios, cambiarCriterios) }
                                ancho("encabezado") {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Nuestra colección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                                            val cantidad = vista.coincidencias.size
                                            Text(if (cantidad == 1) "1 coincidencia" else "$cantidad coincidencias", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }
                                if (vista.coincidencias.isEmpty() && vista.criterios.errorPrecio == null) {
                                    ancho("sin-resultados") {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = MaterialTheme.shapes.large,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("No hay productos que coincidan con tus filtros.", Modifier.padding(20.dp))
                                        }
                                    }
                                }
                                items(vista.coincidencias, key = { it.id }) { item ->
                                    TarjetaCatalogo(item, imageLoader, onClick = { onItemClick(item.id) }, onAlternarFavorito = { onAlternarFavorito(item.id) })
                                }
                                if (vista.coincidencias.isNotEmpty()) {
                                    ancho("nota") {
                                        Text(NOTA_PRECIOS, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Elemento que ocupa todo el ancho de la cuadrícula. */
private fun LazyGridScope.ancho(clave: String, contenido: @Composable () -> Unit) {
    item(key = clave, span = { GridItemSpan(maxLineSpan) }) { contenido() }
}
