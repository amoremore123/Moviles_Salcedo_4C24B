package com.tecsup.mibodega.ui.cliente.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.mibodega.ui.componentes.BotonPrimario
import com.tecsup.mibodega.ui.componentes.BotonSecundario
import com.tecsup.mibodega.ui.theme.BodegaTheme
import com.tecsup.mibodega.ui.theme.VerdeBodega

@Composable
fun PantallaConfirmacion(
    onVerEstado: () -> Unit,
    onVolverInicio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(VerdeBodega, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "¡Pedido realizado!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = VerdeBodega
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Tu pedido está siendo preparado y será entregado pronto.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pedido #1024", fontWeight = FontWeight.SemiBold)
                Text("S/ 25.90", fontWeight = FontWeight.Bold, color = VerdeBodega)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Dirección: Av. Los Olivos 123 (Frente al parque)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(32.dp))

        BotonPrimario(
            texto = "Ver estado del pedido",
            icono = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Phone),
            onClick = onVerEstado
        )

        Spacer(Modifier.height(12.dp))

        BotonSecundario(
            texto = "Volver al inicio",
            onClick = onVolverInicio
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaConfirmacionPreview() {
    BodegaTheme {
        PantallaConfirmacion(onVerEstado = {}, onVolverInicio = {})
    }
}
