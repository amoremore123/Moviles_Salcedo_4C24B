package com.tecsup.avisoscampus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.avisoscampus.model.Aviso
import com.tecsup.avisoscampus.model.FiltroHistorial

// Solo recibe la lista: no conoce el ViewModel.
// Usa Column + forEach (no LazyColumn) porque la pantalla ya tiene scroll.
@Composable
fun HistorialAvisos(
    avisos: List<Aviso>,
    filtro: FiltroHistorial,
    alCambiarFiltro: (FiltroHistorial) -> Unit,
    alBorrar: () -> Unit
) {
    var confirmarBorrado by remember { mutableStateOf(false) }

    Tarjeta("Historial") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FiltroHistorial.entries.forEach { opcion ->
                FilterChip(
                    selected = opcion == filtro,
                    onClick = { alCambiarFiltro(opcion) },
                    label = { Text(opcion.etiqueta) }
                )
            }
            Spacer(Modifier.weight(1f))
            // La papelera solo pide confirmación; borrar lo decide el diálogo
            IconButton(onClick = { confirmarBorrado = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Borrar historial")
            }
        }

        if (avisos.isEmpty()) {
            Text(
                if (filtro == FiltroHistorial.TODOS) "Sin avisos todavía"
                else "Sin avisos en este filtro"
            )
        } else {
            // El repositorio ya los guarda con lo más reciente arriba
            avisos.take(20).forEachIndexed { i, aviso ->
                if (i > 0) HorizontalDivider()
                FilaAviso(aviso)
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

@Composable
private fun FilaAviso(aviso: Aviso) {
    // Ícono pequeño a la izquierda y el contenido del aviso a la derecha
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            painter = painterResource(aviso.icono.recurso()),
            contentDescription = aviso.icono.etiqueta,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        )
        ContenidoAviso(aviso)
    }
}

@Composable
private fun ContenidoAviso(aviso: Aviso) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    aviso.origen.texto,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
                if (aviso.urgente) {
                    Text(
                        "Urgente",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                aviso.hora,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(aviso.titulo, fontWeight = FontWeight.Bold)
        Text(aviso.texto, style = MaterialTheme.typography.bodyMedium)
    }
}
