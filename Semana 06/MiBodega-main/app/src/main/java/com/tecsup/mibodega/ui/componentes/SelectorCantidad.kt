package com.tecsup.mibodega.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.mibodega.ui.theme.GrisBorde
import com.tecsup.mibodega.ui.theme.VerdeBodega

/**
 * El "− cantidad +" reutilizable. Se usa en: Detalle del producto
 * y en cada fila del Carrito.
 *
 * No sabe nada de productos ni de carrito: solo recibe un número
 * y avisa cuándo debe subir o bajar (así no se acopla a ningún modelo
 * de datos específico, y se puede reusar en cualquier pantalla).
 *
 * @param minimo cantidad mínima permitida (por defecto 1: no deja bajar de ahí)
 */
@Composable
fun SelectorCantidad(
    cantidad: Int,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    modifier: Modifier = Modifier,
    minimo: Int = 1
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonCirculo(
            icono = Icons.Default.Remove,
            habilitado = cantidad > minimo,
            relleno = false,
            onClick = onDecrementar
        )

        Box(
            modifier = Modifier.size(width = 36.dp, height = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$cantidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        BotonCirculo(
            icono = Icons.Default.Add,
            habilitado = true,
            relleno = true,
            onClick = onIncrementar
        )
    }
}

@Composable
private fun BotonCirculo(
    icono: ImageVector,
    habilitado: Boolean,
    relleno: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (relleno) VerdeBodega else MaterialTheme.colorScheme.surface,
                shape = CircleShape
            )
    ) {
        val colorIcono = when {
            relleno -> MaterialTheme.colorScheme.onPrimary
            habilitado -> VerdeBodega
            else -> GrisBorde
        }
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(18.dp)
        )
    }
}

