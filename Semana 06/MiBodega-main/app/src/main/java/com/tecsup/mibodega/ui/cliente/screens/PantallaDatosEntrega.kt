package com.tecsup.mibodega.ui.cliente.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.mibodega.ui.componentes.BotonPrimario
import com.tecsup.mibodega.ui.componentes.CampoTexto
import com.tecsup.mibodega.ui.theme.BodegaTheme

@Composable
fun PantallaDatosEntrega(
    onVolver: () -> Unit,
    onConfirmarPedido: () -> Unit
) {
    var nombre by remember { mutableStateOf("Juan Pérez") }
    var telefono by remember { mutableStateOf("987 654 321") }
    var direccion by remember { mutableStateOf("Av. Los Olivos 123") }
    var referencia by remember { mutableStateOf("Frente al parque") }
    var metodoPago by remember { mutableStateOf("Efectivo al entregar") }

    val metodosPago = listOf("Efectivo al entregar", "Yape", "Plin")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Datos de entrega",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        CampoTexto(
            etiqueta = "Nombre",
            valor = nombre,
            onValorCambia = { nombre = it },
            placeholder = "Juan Pérez"
        )
        Spacer(Modifier.height(12.dp))

        CampoTexto(
            etiqueta = "Teléfono",
            valor = telefono,
            onValorCambia = { telefono = it },
            placeholder = "987 654 321",
            teclado = KeyboardType.Phone
        )
        Spacer(Modifier.height(12.dp))

        CampoTexto(
            etiqueta = "Dirección",
            valor = direccion,
            onValorCambia = { direccion = it },
            placeholder = "Av. Los Olivos 123"
        )
        Spacer(Modifier.height(12.dp))

        CampoTexto(
            etiqueta = "Referencia",
            valor = referencia,
            onValorCambia = { referencia = it },
            placeholder = "Frente al parque"
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Método de pago",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        metodosPago.forEach { metodo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = metodoPago == metodo,
                        onClick = { metodoPago = metodo }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = metodoPago == metodo,
                    onClick = { metodoPago = metodo }
                )
                Text(
                    text = metodo,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        BotonPrimario(
            texto = "Confirmar pedido",
            onClick = onConfirmarPedido
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaDatosEntregaPreview() {
    BodegaTheme {
        PantallaDatosEntrega(onVolver = {}, onConfirmarPedido = {})
    }
}
