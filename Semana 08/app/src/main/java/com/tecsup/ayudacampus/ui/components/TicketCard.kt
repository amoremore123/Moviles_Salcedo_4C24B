package com.tecsup.ayudacampus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCard(detalle: TicketConDetalle, onClick: () -> Unit, modifier: Modifier = Modifier, mostrarCliente: Boolean = false) {
    val ticket = detalle.ticket
    val fecha = remember(ticket.fecha) {
        SimpleDateFormat("dd MMM · HH:mm", Locale("es", "PE")).format(Date(ticket.fecha))
    }
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth().testTag("ticket_${ticket.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(colorPrioridad(ticket.prioridad)))
            Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("#${ticket.id.toString().padStart(4, '0')}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { IconoCategoria(detalle.categoria.icono, detalle.categoria.nombre) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(ticket.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (mostrarCliente) Text("Estudiante: ${detalle.cliente.nombre}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Text(detalle.categoria.nombre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(ticket.ubicacion, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoChip(ticket.estado)
                    PrioridadChip(ticket.prioridad)
                }
            }
        }
    }
}

@Composable
internal fun IconoCategoria(tipo: String, nombre: String) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(24.dp).semantics { contentDescription = "Categoría: $nombre" }) {
        val u = size.width / 24f
        val trazo = Stroke(1.8f * u)
        fun linea(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(color, Offset(x1 * u, y1 * u), Offset(x2 * u, y2 * u), 1.8f * u)
        when (tipo) {
            "wifi" -> {
                drawArc(color, 215f, 110f, false, Offset(2*u, 4*u), Size(20*u, 20*u), style = trazo)
                drawArc(color, 215f, 110f, false, Offset(6*u, 9*u), Size(12*u, 12*u), style = trazo)
                drawCircle(color, 1.6f*u, Offset(12*u, 19*u))
            }
            "code" -> { linea(8f,6f,2f,12f); linea(2f,12f,8f,18f); linea(16f,6f,22f,12f); linea(22f,12f,16f,18f); linea(14f,4f,10f,20f) }
            "key" -> {
                drawCircle(color, 4*u, Offset(7*u, 9*u), style = trazo)
                linea(10f,12f,20f,22f); linea(15f,17f,18f,14f); linea(18f,20f,21f,17f)
            }
            "printer" -> {
                drawRect(color, Offset(3*u, 8*u), Size(18*u, 10*u), style = trazo)
                drawRect(color, Offset(7*u, 2*u), Size(10*u, 6*u), style = trazo)
                drawRect(color, Offset(7*u, 14*u), Size(10*u, 8*u), style = trazo)
            }
            "pc" -> {
                drawRect(color, Offset(2*u, 3*u), Size(20*u, 14*u), style = trazo)
                linea(12f,17f,12f,21f); linea(7f,21f,17f,21f)
            }
            "tag" -> {
                val etiqueta = androidx.compose.ui.graphics.Path().apply {
                    moveTo(3*u, 3*u); lineTo(13*u, 3*u); lineTo(22*u, 12*u)
                    lineTo(12*u, 22*u); lineTo(3*u, 13*u); close()
                }
                drawPath(etiqueta, color, style = trazo)
                drawCircle(color, 1.5f*u, Offset(7*u, 7*u), style = trazo)
            }
            else -> {
                drawRect(color, Offset(4*u, 4*u), Size(16*u, 16*u), style = trazo)
                linea(8f,9f,16f,9f); linea(8f,14f,16f,14f)
            }
        }
    }
}
