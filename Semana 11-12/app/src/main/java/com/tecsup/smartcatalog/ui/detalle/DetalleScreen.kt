package com.tecsup.smartcatalog.ui.detalle

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.domain.model.OrigenDetalle
import com.tecsup.smartcatalog.ui.UiState
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
            Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = descripcionVolver, tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("Detalle del smartphone", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    (estado as? UiState.Exito<Item>)?.datos?.let { item ->
                        BotonFavorito(item.favorito, item.nombre, viewModel::alternarFavorito, habilitado)
                    }
                }
            }
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
                    is UiState.Exito -> DetalleContenido(actual.datos, imageLoader)
                }
            }
        }
    }
}

@Composable
private fun DetalleContenido(item: Item, imageLoader: ImageLoader) {
    val precio = precioFormateado(item.precio)
    Column(Modifier.widthIn(max = 840.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        ImagenProducto(
            item, imageLoader,
            Modifier.fillMaxWidth().height(216.dp).clip(MaterialTheme.shapes.extraLarge)
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(item.categoria.uppercase(Locale.ROOT), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(item.nombre, style = MaterialTheme.typography.headlineMedium)
                Column {
                    Text("Precio de referencia", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(precio, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("Descripción", style = MaterialTheme.typography.titleMedium)
                Text(item.descripcion.ifBlank { "Descripción no disponible." }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Importe original de la API · Sin conversión de moneda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
