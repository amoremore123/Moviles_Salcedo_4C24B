package com.tecsup.smartcatalog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.tecsup.smartcatalog.domain.model.precioEnSoles
import com.tecsup.smartcatalog.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ItemCard(item: Item, imageLoader: ImageLoader, onClick: () -> Unit, onAlternarFavorito: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).clickable(role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenProducto(item, imageLoader, Modifier.width(84.dp).height(84.dp).clip(MaterialTheme.shapes.medium))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(item.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(
                    precioFormateado(item.precio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            BotonFavorito(item.favorito, item.nombre, onAlternarFavorito)
        }
    }
}

/** Tarjeta vertical para la cuadrícula del catálogo: imagen arriba, corazón en la esquina, nombre y precio. */
@Composable
fun TarjetaCatalogo(item: Item, imageLoader: ImageLoader, onClick: () -> Unit, onAlternarFavorito: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).clickable(role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) { Column {
        Box {
            ImagenProducto(
                item, imageLoader,
                Modifier.fillMaxWidth().height(140.dp).padding(8.dp).clip(MaterialTheme.shapes.medium)
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
            ) {
                BotonFavorito(item.favorito, item.nombre, onAlternarFavorito)
            }
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp)) {
            Text(item.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(
                precioFormateado(item.precio),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    } }
}

@Composable
internal fun ImagenProducto(item: Item, imageLoader: ImageLoader, modifier: Modifier) {
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
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {
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
internal fun precioFormateado(precio: Double): String = remember(precio) {
    val importe = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(precioEnSoles(precio))
    "S/ $importe"
}

