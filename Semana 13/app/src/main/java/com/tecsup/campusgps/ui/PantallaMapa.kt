package com.tecsup.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    // TODO Paso 2: lanzador de permisos con rememberLauncherForActivityResult(
    //   ActivityResultContracts.RequestMultiplePermissions()) y fun alPulsar()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { vm.buscar() }, // TODO Paso 2: cambiar por alPulsar()
                enabled = estado !is UbicacionUi.Buscando
            ) {
                Text("Obtener mi ubicación")
            }

            // TODO Paso 3: MapaUbicacion(..., modifier = Modifier.weight(1f))
            // TODO Paso 6: ListaLugares(...) con altura fija
            // TODO Paso 7: botón "Ir al más cercano" y texto "Más cercano: ..."

            EstadoUbicacion(estado)
        }
    }
}

@Composable
private fun EstadoUbicacion(estado: UbicacionUi) {
    when (estado) {
        UbicacionUi.Inicial -> Text("Pulsa el botón para conocer tu ubicación")
        UbicacionUi.Buscando -> {
            CircularProgressIndicator()
            Text("Buscando tu ubicación…")
        }
        is UbicacionUi.Lista -> {
            Text("Latitud: ${estado.posicion.latitud}")
            Text("Longitud: ${estado.posicion.longitud}")
        }
        // TODO Paso 8: reemplazar por MensajeError(estado, ...)
        is UbicacionUi.Error -> Text(
            text = estado.mensaje,
            color = MaterialTheme.colorScheme.error
        )
    }
    Spacer(Modifier.height(4.dp))
}
