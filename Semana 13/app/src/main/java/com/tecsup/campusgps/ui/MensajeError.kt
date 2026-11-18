package com.tecsup.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.campusgps.model.TipoError
import com.tecsup.campusgps.model.UbicacionUi

/**
 * Recuadro de error: muestra el mensaje y, según el tipo, la acción útil.
 * No decide nada por su cuenta: avisa con onAbrirAjustes / onReintentar.
 */
@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onAbrirAjustes: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colores.errorContainer,
            contentColor = colores.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(error.mensaje, textAlign = TextAlign.Center)

            val botonError = ButtonDefaults.buttonColors(
                containerColor = colores.error,
                contentColor = colores.onError
            )
            when (error.tipo) {
                TipoError.SIN_PERMISO ->
                    Button(onClick = onAbrirAjustes, colors = botonError) { Text("Abrir ajustes") }
                TipoError.GPS_APAGADO, TipoError.OTRO ->
                    Button(onClick = onReintentar, colors = botonError) { Text("Reintentar") }
                // Sin Play Services no hay nada que reintentar
                TipoError.SIN_PLAY_SERVICES -> Unit
            }
        }
    }
}
