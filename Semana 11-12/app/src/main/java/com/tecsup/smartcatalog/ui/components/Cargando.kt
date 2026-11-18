package com.tecsup.smartcatalog.ui.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.R

@Composable
fun Cargando(
    modifier: Modifier = Modifier,
    mensaje: String? = "Cargando productos..."
) {
    EstadoConGatito(R.drawable.gatito_saludo, modifier, tamano = 130.dp) {
        CircularProgressIndicator()
        if (!mensaje.isNullOrBlank()) {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
