package com.tecsup.smartcatalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.ui.theme.ColoresMarca

/** Cabecera con degradado fucsia → morado, esquinas inferiores redondeadas y contenido en blanco. */
@Composable
fun CabeceraMarca(
    titulo: String,
    modifier: Modifier = Modifier,
    navegacion: (@Composable () -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(ColoresMarca.degradado)
            .statusBarsPadding()
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Row(
                Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navegacion != null) navegacion() else Spacer(Modifier.width(8.dp))
                Text(
                    titulo,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                acciones()
            }
        }
    }
}

/** Botón principal en forma de píldora con el degradado de marca. */
@Composable
fun BotonPrincipal(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val apagado = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent)
    ) {
        Box(
            Modifier.fillMaxWidth().heightIn(min = 52.dp)
                .background(if (enabled) ColoresMarca.degradado else SolidColor(apagado))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, content = content)
        }
    }
}

/** Colores de los campos de texto: fondo blanco y borde rosado suave. */
@Composable
fun coloresCampo(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary
)

/** Fondo claro con ondas suaves arriba y abajo, como en las pantallas de bienvenida y estados. */
@Composable
fun FondoOndas(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val fondo = MaterialTheme.colorScheme.background
    val onda1 = MaterialTheme.colorScheme.primaryContainer
    val onda2 = MaterialTheme.colorScheme.secondaryContainer
    Box(
        modifier
            .background(fondo)
            .drawBehind {
                val w = size.width
                val h = size.height
                val arriba = Path().apply {
                    moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, h * 0.16f)
                    cubicTo(w * 0.7f, h * 0.24f, w * 0.35f, h * 0.08f, 0f, h * 0.2f); close()
                }
                val arriba2 = Path().apply {
                    moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, h * 0.1f)
                    cubicTo(w * 0.6f, h * 0.02f, w * 0.3f, h * 0.18f, 0f, h * 0.1f); close()
                }
                val abajo = Path().apply {
                    moveTo(0f, h); lineTo(w, h); lineTo(w, h * 0.84f)
                    cubicTo(w * 0.7f, h * 0.92f, w * 0.3f, h * 0.78f, 0f, h * 0.88f); close()
                }
                drawPath(arriba, onda1.copy(alpha = 0.8f))
                drawPath(arriba2, onda2.copy(alpha = 0.7f))
                drawPath(abajo, onda1.copy(alpha = 0.8f))
            },
        content = content
    )
}
