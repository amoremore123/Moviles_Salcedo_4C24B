package com.tecsup.avisoscampus.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.avisoscampus.R
import com.tecsup.avisoscampus.model.Aviso
import com.tecsup.avisoscampus.model.FiltroHistorial
import com.tecsup.avisoscampus.model.Origen

// Pantalla de historial. Solo recibe datos y acciones: no conoce el ViewModel.
// Usa Column + forEach (no LazyColumn) porque la pantalla ya tiene scroll.
@Composable
fun HistorialAvisos(
    avisos: List<Aviso>,
    filtro: FiltroHistorial,
    alCambiarFiltro: (FiltroHistorial) -> Unit,
    alBorrar: () -> Unit
) {
    var confirmarBorrado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título con ícono a la izquierda y papelera a la derecha
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Historial de avisos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
            // La papelera solo pide confirmación; borrar lo decide el diálogo
            IconButton(onClick = { confirmarBorrado = true }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Borrar historial",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Filtros dentro de una píldora lila; el elegido va en morado sólido
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FiltroHistorial.entries.forEach { opcion ->
                    FilterChip(
                        selected = opcion == filtro,
                        onClick = { alCambiarFiltro(opcion) },
                        label = {
                            Text(
                                opcion.etiqueta,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        shape = RoundedCornerShape(50),
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Filas dentro de una tarjeta blanca con separadores
        Card(modifier = Modifier.fillMaxWidth()) {
            if (avisos.isEmpty()) {
                Text(
                    if (filtro == FiltroHistorial.TODOS) "Sin avisos todavía"
                    else "Sin avisos en este filtro",
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                Column(Modifier.padding(vertical = 4.dp)) {
                    // El repositorio ya los guarda con lo más reciente arriba
                    avisos.take(20).forEachIndexed { i, aviso ->
                        if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        FilaAviso(aviso)
                    }
                }
            }
        }
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Borrar el historial?") },
            text = { Text("Se quitarán todos los avisos de la lista.") },
            confirmButton = {
                TextButton(onClick = {
                    alBorrar()
                    confirmarBorrado = false
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

// Ícono de la fila según el origen: el aviso del botón muestra el ícono que eligió el usuario
@DrawableRes
private fun iconoDe(aviso: Aviso): Int = when (aviso.origen) {
    Origen.BOTON -> aviso.icono.recurso()
    Origen.RECORDATORIO -> R.drawable.ic_card_calendario
    Origen.MODO_AVION -> R.drawable.ic_card_avion
}

@Composable
private fun FilaAviso(aviso: Aviso) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Image(
                painter = painterResource(iconoDe(aviso)),
                contentDescription = aviso.origen.texto,
                modifier = Modifier.size(30.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    aviso.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (aviso.urgente) {
                    Text(
                        "Urgente",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                aviso.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // El historial vive solo en memoria mientras la app está abierta: todo es de hoy
            Text(
                "Hoy · ${aviso.hora.take(5)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
