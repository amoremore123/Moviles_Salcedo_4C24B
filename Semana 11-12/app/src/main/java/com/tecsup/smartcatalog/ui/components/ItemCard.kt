package com.tecsup.smartcatalog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ItemCard(item: Item, imageLoader: ImageLoader, onClick: () -> Unit, onAlternarFavorito: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        BoxWithConstraints {
            val anchoImagen = if (maxWidth < 360.dp) 112.dp else 136.dp
            if (maxWidth < 300.dp) {
                Column {
                    ImagenProducto(item, imageLoader, Modifier.fillMaxWidth().height(180.dp))
                    InformacionProducto(item, onAlternarFavorito, Modifier.fillMaxWidth())
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ImagenProducto(item, imageLoader, Modifier.width(anchoImagen).height(206.dp))
                    InformacionProducto(item, onAlternarFavorito, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ImagenProducto(item: Item, imageLoader: ImageLoader, modifier: Modifier) {
    val recurso = when (item.imagen.substringAfterLast('/')) {
        "smartphone_aurora" -> R.drawable.smartphone_aurora
        "smartphone_andino" -> R.drawable.smartphone_andino
        "smartphone_costa" -> R.drawable.smartphone_costa
        "smartphone_sierra" -> R.drawable.smartphone_sierra
        else -> R.drawable.ic_smartcatalog
    }
    val context = LocalContext.current
    val request = remember(item.imagen, context) {
        val modelo: Any = if (item.imagen.startsWith("https://") || item.imagen.startsWith("http://")) item.imagen else recurso
        ImageRequest.Builder(context).data(modelo).diskCachePolicy(CachePolicy.DISABLED).crossfade(true).build()
    }
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = request,
                imageLoader = imageLoader,
                placeholder = painterResource(recurso),
                error = painterResource(recurso),
                fallback = painterResource(recurso),
                contentDescription = "Imagen de ${item.nombre}",
                modifier = Modifier.fillMaxSize().padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun InformacionProducto(item: Item, onAlternarFavorito: () -> Unit, modifier: Modifier) {
    val precio = remember(item.precio) {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(item.precio)
    }
    Column(modifier = modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.categoria.uppercase(Locale.ROOT),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            BotonFavorito(item.favorito, item.nombre, onAlternarFavorito)
        }
        Spacer(Modifier.height(7.dp))
        Text(item.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text(
            text = item.descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(12.dp))
        Text("Precio de referencia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = precio,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
