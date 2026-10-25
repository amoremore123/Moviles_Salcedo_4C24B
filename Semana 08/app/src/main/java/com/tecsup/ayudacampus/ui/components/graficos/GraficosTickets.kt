package com.tecsup.ayudacampus.ui.components.graficos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tecsup.ayudacampus.model.DatoGrafico

private val coloresEstado = listOf(Color(0xFFD97706), Color(0xFF2563EB), Color(0xFF16A34A), Color(0xFF6B7280))

@Composable
fun GraficoDona(datos: List<DatoGrafico>, modifier: Modifier = Modifier) {
    val total = datos.sumOf { it.cantidad.coerceAtLeast(0) }
    val fondo = MaterialTheme.colorScheme.outlineVariant
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(Modifier.size(112.dp).semantics { contentDescription = "Total: $total tickets" }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(10.dp)) {
                val grosor = 15.dp.toPx()
                val area = Size(size.width - grosor, size.height - grosor)
                val inicio = Offset(grosor / 2, grosor / 2)
                if (total == 0) drawArc(fondo, 0f, 360f, false, inicio, area, style = Stroke(grosor))
                else {
                    var angulo = -90f
                    datos.forEachIndexed { index, dato ->
                        val tramo = 360f * dato.cantidad.coerceAtLeast(0) / total
                        if (tramo > 0f) drawArc(coloresEstado[index % coloresEstado.size], angulo, tramo, false, inicio, area, style = Stroke(grosor))
                        angulo += tramo
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(total.toString(), style = MaterialTheme.typography.headlineSmall)
                Text("tickets", style = MaterialTheme.typography.labelSmall)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            datos.forEachIndexed { index, dato ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Canvas(Modifier.size(8.dp)) { drawCircle(coloresEstado[index % coloresEstado.size]) }
                    Text("${dato.etiqueta}: ${dato.cantidad}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun GraficoBarras(datos: List<DatoGrafico>, modifier: Modifier = Modifier) {
    val maximo = (datos.maxOfOrNull { it.cantidad } ?: 0).coerceAtLeast(1)
    val principal = MaterialTheme.colorScheme.primary
    val fondo = MaterialTheme.colorScheme.primaryContainer
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        if (datos.isEmpty()) Text("Sin categorías registradas", style = MaterialTheme.typography.bodySmall)
        datos.forEach { dato ->
            val proporcion by animateFloatAsState(dato.cantidad.coerceAtLeast(0).toFloat() / maximo, label = "barraCategoria")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${dato.etiqueta}: ${dato.cantidad}", style = MaterialTheme.typography.bodySmall)
                Canvas(Modifier.fillMaxWidth().height(10.dp)) {
                    drawRoundRect(fondo, cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))
                    val ancho = size.width * proporcion
                    if (ancho > 0f) drawRoundRect(principal, size = Size(ancho, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))
                }
            }
        }
    }
}
