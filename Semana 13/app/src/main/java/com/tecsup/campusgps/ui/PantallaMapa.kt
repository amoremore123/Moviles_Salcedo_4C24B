package com.tecsup.campusgps.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.campusgps.BuildConfig
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.util.formatearDistancia
import com.tecsup.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val lugares by vm.lugares.collectAsStateWithLifecycle()
    val seleccionado by vm.seleccionado.collectAsStateWithLifecycle()
    val masCercano by vm.masCercano.collectAsStateWithLifecycle()
    val lugaresVisibles by vm.lugaresVisibles.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val radio by vm.radio.collectAsStateWithLifecycle()
    val idsDentroDelRadio by vm.idsDentroDelRadio.collectAsStateWithLifecycle()
    val ultimoVisitado by vm.ultimoVisitado.collectAsStateWithLifecycle()

    val contexto = LocalContext.current

    // Android dibuja el cuadro de permiso; aquí solo recibimos la respuesta del usuario
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        // Basta con que conceda FINE o COARSE
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    fun alPulsar() {
        val tienePermiso = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).any {
            ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
        }

        if (tienePermiso) {
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

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
                onClick = { alPulsar() },
                enabled = estado !is UbicacionUi.Buscando
            ) {
                Text("Obtener mi ubicación")
            }

            EstadoUbicacion(
                estado = estado,
                onAbrirAjustes = {
                    // Abre la pantalla de permisos de ESTA app en Ajustes
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", contexto.packageName, null)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    contexto.startActivity(intent)
                },
                onReintentar = { alPulsar() }
            )

            // El mapa ocupa todo el espacio libre.
            // Mejora 4: con clave se usa Google Maps; sin clave, OpenStreetMap.
            val modificadorMapa = Modifier
                .fillMaxWidth()
                .weight(1f)
            if (BuildConfig.TIENE_CLAVE_MAPS) {
                MapaUbicacion(
                    estado = estado,
                    lugares = lugaresVisibles,
                    seleccionado = seleccionado,
                    idMasCercano = masCercano?.id,
                    radio = radio,
                    modifier = modificadorMapa
                )
            } else {
                MapaOsm(
                    estado = estado,
                    lugares = lugaresVisibles,
                    seleccionado = seleccionado,
                    idMasCercano = masCercano?.id,
                    radio = radio,
                    modifier = modificadorMapa
                )
            }

            // Mejora 2: selector del radio de cercanía
            SelectorRadio(
                radios = vm.radios,
                actual = radio,
                onCambiar = { vm.cambiarRadio(it) },
                modifier = Modifier.fillMaxWidth()
            )

            // Mejora 1: chips de categoría (filtran mapa y lista)
            FiltroCategorias(
                categorias = vm.categorias,
                activa = categoria,
                onSeleccionar = { vm.filtrarCategoria(it) },
                modifier = Modifier.fillMaxWidth()
            )

            // La lista va debajo con altura fija; el toque se lo pasa al ViewModel
            ListaLugares(
                lugares = lugaresVisibles,
                seleccionado = seleccionado,
                onClick = { id -> vm.seleccionar(id) },
                dentroDelRadio = idsDentroDelRadio,
                radio = radio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            // Botón secundario: lleva la cámara al lugar más cercano
            OutlinedButton(
                onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                enabled = masCercano != null
            ) {
                Text("Ir al más cercano")
            }

            // La distancia ya viene calculada por el ViewModel; aquí solo se formatea
            masCercano?.let { cercano ->
                val metros = lugares.firstOrNull { it.lugar.id == cercano.id }?.metros
                Text(
                    "Más cercano: ${cercano.nombre} (${metros?.let { formatearDistancia(it) } ?: "Sin calcular"})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Mejora 3: último lugar visitado, recuperado de DataStore
            ultimoVisitado?.let {
                Text(
                    "Último visitado: ${it.nombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EstadoUbicacion(
    estado: UbicacionUi,
    onAbrirAjustes: () -> Unit,
    onReintentar: () -> Unit
) {
    when (estado) {
        UbicacionUi.Inicial -> Text("Pulsa el botón para conocer tu ubicación")
        UbicacionUi.Buscando -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Text("Buscando tu ubicación…")
        }
        is UbicacionUi.Lista -> Text(
            "Lat: %.5f   Lon: %.5f".format(estado.posicion.latitud, estado.posicion.longitud)
        )
        is UbicacionUi.Error -> MensajeError(
            error = estado,
            onAbrirAjustes = onAbrirAjustes,
            onReintentar = onReintentar
        )
    }
}
