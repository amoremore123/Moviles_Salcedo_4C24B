package com.tecsup.smartcatalog.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.domain.model.NOTA_PRECIOS
import com.tecsup.smartcatalog.domain.model.OrigenDetalle
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.components.BotonPrincipal
import com.tecsup.smartcatalog.ui.components.CabeceraMarca
import com.tecsup.smartcatalog.ui.components.Cargando
import com.tecsup.smartcatalog.ui.components.ErrorView
import com.tecsup.smartcatalog.ui.components.BotonFavorito
import com.tecsup.smartcatalog.ui.components.AvisoDatosGuardados
import com.tecsup.smartcatalog.ui.components.ImagenProducto
import com.tecsup.smartcatalog.ui.components.precioFormateado
import java.util.Locale

@Composable
fun DetalleScreen(viewModel: DetalleViewModel, imageLoader: ImageLoader, onVolver: () -> Unit, descripcionVolver: String = "Volver al catálogo") {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeFavorito.collectAsStateWithLifecycle()
    val habilitado by viewModel.puedeAlternarFavorito.collectAsStateWithLifecycle()
    val origen by viewModel.origen.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CabeceraMarca(
                "Detalle del smartphone",
                navegacion = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = descripcionVolver)
                    }
                },
                acciones = {
                    (estado as? UiState.Exito<Item>)?.datos?.let { item ->
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                            BotonFavorito(item.favorito, item.nombre, viewModel::alternarFavorito, habilitado)
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (origen == OrigenDetalle.LOCAL && estado is UiState.Exito) {
                AvisoDatosGuardados("Detalle guardado. No se pudo consultar la versión actual.", viewModel::reintentar)
            }
            mensaje?.let { texto ->
                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(texto, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                when (val actual = estado) {
                    UiState.Cargando -> Cargando(mensaje = "Cargando detalle...")
                    is UiState.Error -> ErrorView(mensaje = actual.mensaje, onReintentar = viewModel::reintentar)
                    is UiState.Exito -> DetalleContenido(actual.datos, imageLoader, habilitado, viewModel::alternarFavorito)
                }
            }
        }
    }
}

@Composable
private fun DetalleContenido(item: Item, imageLoader: ImageLoader, habilitado: Boolean, onAlternarFavorito: () -> Unit) {
    val precio = precioFormateado(item.precio)
    Column(
        Modifier.widthIn(max = 840.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            ImagenProducto(
                item, imageLoader,
                Modifier.fillMaxWidth().height(260.dp).padding(10.dp).clip(MaterialTheme.shapes.large)
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                    Text(
                        item.categoria.uppercase(Locale.ROOT),
                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(item.nombre, style = MaterialTheme.typography.headlineMedium)
                Column {
                    Text("Precio de referencia", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(precio, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(item.descripcion.ifBlank { "Descripción no disponible." }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(NOTA_PRECIOS, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        BotonPrincipal(onAlternarFavorito, Modifier.fillMaxWidth(), enabled = habilitado) {
            Icon(
                painterResource(if (item.favorito) R.drawable.ic_favorito else R.drawable.ic_favorito_vacio),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (item.favorito) "Quitar de favoritos" else "Agregar a favoritos")
        }
    }
}
