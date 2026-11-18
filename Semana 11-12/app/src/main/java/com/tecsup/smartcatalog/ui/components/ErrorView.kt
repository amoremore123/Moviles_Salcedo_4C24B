package com.tecsup.smartcatalog.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.R

@Composable
fun ErrorView(
    mensaje: String,
    modifier: Modifier = Modifier,
    onReintentar: (() -> Unit)? = null
) {
    EstadoConGatito(R.drawable.gatito_sin_conexion, modifier) {
        Text(
            "¡Ups! Algo salió mal",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onReintentar != null) {
            Spacer(Modifier.height(4.dp))
            BotonPrincipal(onReintentar, Modifier.fillMaxWidth()) {
                Icon(painterResource(R.drawable.ic_actualizar), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}
