package com.tecsup.smartcatalog.ui.detalle

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.components.Cargando
import com.tecsup.smartcatalog.ui.components.ErrorView
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DetalleScreen(viewModel: DetalleViewModel, imageLoader: ImageLoader, onVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver al catálogo", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("Detalle del smartphone", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when (val actual = estado) {
                UiState.Cargando -> Cargando(mensaje = "Cargando detalle...")
                is UiState.Error -> ErrorView(mensaje = actual.mensaje, onReintentar = viewModel::reintentar)
                is UiState.Exito -> DetalleContenido(actual.datos, imageLoader)
            }
        }
    }
}

@Composable
private fun DetalleContenido(item: Item, imageLoader: ImageLoader) {
    val context = LocalContext.current
    val request = remember(item.imagen, context) {
        ImageRequest.Builder(context).data(item.imagen.takeIf { it.isNotBlank() })
            .diskCachePolicy(CachePolicy.DISABLED).crossfade(true).build()
    }
    val precio = remember(item.precio) {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(item.precio)
    }
    Column(Modifier.widthIn(max = 840.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ) {
            SubcomposeAsyncImage(
                model = request,
                imageLoader = imageLoader,
                contentDescription = "Imagen de ${item.nombre}",
                modifier = Modifier.fillMaxSize().padding(20.dp),
                contentScale = ContentScale.Fit,
                loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(36.dp)) } },
                error = {
                    Image(painterResource(R.drawable.ic_smartcatalog), contentDescription = "Imagen no disponible", modifier = Modifier.fillMaxSize().padding(40.dp), contentScale = ContentScale.Fit)
                }
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                Text(item.categoria.uppercase(Locale.ROOT), modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text("ID ${item.id}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(14.dp))
        Text(item.nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp))
        Text("Precio de referencia", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(precio, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(24.dp))
        Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(item.descripcion.ifBlank { "Descripción no disponible." }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Text("Importe original de la API · Sin conversión de moneda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
    }
}
