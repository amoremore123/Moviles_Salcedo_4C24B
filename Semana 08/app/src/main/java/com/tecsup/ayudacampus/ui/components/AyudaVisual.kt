package com.tecsup.ayudacampus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyudaTopAppBar(titulo: String, onVolver: (() -> Unit)? = null,
                   descripcionVolver: String = "Volver", habilitado: Boolean = true,
                   actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = {
            Column {
                Text("AYUDACAMPUS", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f))
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            if (onVolver != null) IconButton(onClick = onVolver, enabled = habilitado) {
                Icon(Icons.Default.ArrowBack, descripcionVolver)
            } else Icon(Icons.Default.Info, null, Modifier.padding(start = 20.dp, end = 8.dp).size(26.dp))
        },
        actions = actions,
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun EncabezadoSeccion(titulo: String, detalle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        detalle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun TarjetaBienvenida(titulo: String, detalle: String, icono: ImageVector = Icons.Default.Person) {
    Box(Modifier.fillMaxWidth().background(
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)),
        RoundedCornerShape(24.dp)).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primary) {
                Icon(icono, null, Modifier.padding(14.dp).size(28.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                detalle.lines().forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun TarjetaIndicador(titulo: String, cantidad: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier.semantics(mergeDescendants = true) { contentDescription = "$titulo: $cantidad" },
        shape = RoundedCornerShape(18.dp), color = color.copy(alpha = .12f), border = BorderStroke(1.dp, color.copy(alpha = .22f))) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(cantidad.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                color = colorAccesible(color))
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun EstadoVacio(titulo: String, detalle: String, tag: String = "") {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Default.Search, null, Modifier.padding(24.dp).size(36.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(titulo, Modifier.testTag(tag), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BotonPrincipal(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = habilitado,
        shape = RoundedCornerShape(16.dp)) { Text(texto, fontWeight = FontWeight.SemiBold) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AyudaNavigationBar(soporte: Boolean, seleccionado: Int, habilitado: Boolean = true, onSeleccionar: (Int) -> Unit) {
    val etiquetas = if (soporte) listOf("Panel", "Tickets", "Categorías", "Ajustes") else listOf("Tickets", "Nuevo", "Perfil")
    val iconos = if (soporte) listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Edit, Icons.Default.Settings)
        else listOf(Icons.Default.List, Icons.Default.Add, Icons.Default.Person)
    NavigationBar(modifier = Modifier.semantics { testTagsAsResourceId = true }, containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, windowInsets = WindowInsets(0)) {
        etiquetas.forEachIndexed { i, texto ->
            NavigationBarItem(enabled = habilitado, selected = seleccionado == i, onClick = { onSeleccionar(i) },
                icon = { Icon(iconos[i], null) }, label = { Text(texto) }, modifier = Modifier.testTag("nav_${if (soporte) "admin" else "cliente"}_$i"),
                colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer))
        }
    }
}

@Composable
fun colorAccesible(color: Color): Color = if (MaterialTheme.colorScheme.background.luminance() < .5f)
    androidx.compose.ui.graphics.lerp(color, Color.White, .4f) else color

// The route owner supplies navigation; forms block it while saving.
val LocalBarraInferior = staticCompositionLocalOf<@Composable (Boolean) -> Unit> { {} }
@Composable
fun BarraInferior(habilitado: Boolean = true) { LocalBarraInferior.current(habilitado) }
