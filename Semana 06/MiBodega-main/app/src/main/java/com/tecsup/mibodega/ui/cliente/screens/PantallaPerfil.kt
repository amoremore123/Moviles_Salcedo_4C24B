package com.tecsup.mibodega.ui.cliente.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.mibodega.ui.componentes.BotonSecundario
import com.tecsup.mibodega.ui.theme.BodegaTheme
import com.tecsup.mibodega.ui.theme.GrisClaro
import com.tecsup.mibodega.ui.theme.VerdeBodega

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    onNavigateInicio: () -> Unit,
    onNavigateCategorias: () -> Unit,
    onNavigatePedidos: () -> Unit,
    onNavigatePerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            BarraInferiorNavegacion(
                selectedIndex = 3,
                onInicio = onNavigateInicio,
                onCategorias = onNavigateCategorias,
                onPedidos = onNavigatePedidos,
                onPerfil = onNavigatePerfil
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    tint = VerdeBodega,
                    modifier = Modifier
                        .size(96.dp)
                        .background(GrisClaro, CircleShape)
                        .padding(4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Juan Pérez",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "+51 987 654 321",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            BotonSecundario(
                texto = "Cerrar sesión",
                onClick = onCerrarSesion
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PantallaPerfilPreview() {
    BodegaTheme {
        PantallaPerfil({}, {}, {}, {}, {})
    }
}
