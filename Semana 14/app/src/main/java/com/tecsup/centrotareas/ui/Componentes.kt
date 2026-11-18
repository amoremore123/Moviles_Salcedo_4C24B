package com.tecsup.centrotareas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.centrotareas.ui.theme.AzulPrimario
import com.tecsup.centrotareas.ui.theme.AzulSuave
import com.tecsup.centrotareas.ui.theme.GrisTexto

// Bordes redondeados que comparten todas las tarjetas del rediseño
val FormaTarjeta = RoundedCornerShape(22.dp)
val FormaPildora = RoundedCornerShape(50)

/** Tarjeta blanca con ícono, título azul y subtítulo, como en el diseño. */
@Composable
fun TarjetaTarea(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconoEnCirculo(icono)
                Column {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTexto
                    )
                }
            }
            contenido()
        }
    }
}

@Composable
fun IconoEnCirculo(icono: ImageVector, fondo: Color = AzulSuave, tinte: Color = AzulPrimario) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(26.dp))
    }
}

/** Píldora de estado: Listo, Trabajando..., Terminó, Cancelado. */
@Composable
fun ChipEstado(texto: String, icono: ImageVector, fondo: Color, color: Color) {
    Surface(
        color = fondo,
        contentColor = color,
        shape = FormaPildora,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(texto, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Recuadro de color con ícono a la izquierda (resultado o error). */
@Composable
fun RecuadroConIcono(
    icono: ImageVector,
    fondo: Color,
    colorIcono: Color,
    colorTexto: Color,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = fondo,
        contentColor = colorTexto,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(30.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = contenido)
        }
    }
}
