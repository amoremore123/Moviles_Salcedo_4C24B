package com.tecsup.smartcatalog.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.tecsup.smartcatalog.R

/** El estado siempre viene del ViewModel; el componente solo emite la intención del usuario. */
@Composable
fun BotonFavorito(favorito: Boolean, nombre: String, onAlternar: () -> Unit, habilitado: Boolean = true) {
    IconToggleButton(checked = favorito, onCheckedChange = { onAlternar() }, enabled = habilitado) {
        Icon(
            painter = painterResource(if (favorito) R.drawable.ic_favorito else R.drawable.ic_favorito_vacio),
            contentDescription = if (favorito) "Quitar de favoritos: $nombre" else "Agregar a favoritos: $nombre",
            tint = if (favorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
