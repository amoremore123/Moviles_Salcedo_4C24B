package com.tecsup.campusgps.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.campusgps.BuildConfig
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.ui.theme.AzulBorde
import com.tecsup.campusgps.ui.theme.AzulCielo
import com.tecsup.campusgps.ui.theme.AzulNoche
import com.tecsup.campusgps.ui.theme.AzulRey
import com.tecsup.campusgps.ui.theme.FondoApp
import com.tecsup.campusgps.ui.theme.GrisTexto
import com.tecsup.campusgps.viewmodel.MapaViewModel
import java.util.Locale

@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val seleccionado by vm.seleccionado.collectAsStateWithLifecycle()
    val masCercano by vm.masCercano.collectAsStateWithLifecycle()
    val lugaresVisibles by vm.lugaresVisibles.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val radio by vm.radio.collectAsStateWithLifecycle()
    val idsDentroDelRadio by vm.idsDentroDelRadio.collectAsStateWithLifecycle()
    val ultimoVisitado by vm.ultimoVisitado.collectAsStateWithLifecycle()

    // Estado puramente visual (abrir/cerrar el panel): se queda en la View
    var filtrosAbiertos by rememberSaveable { mutableStateOf(false) }

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

    fun abrirAjustes() {
        // Abre la pantalla de permisos de ESTA app en Ajustes
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", contexto.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }

    // Mejora 6: rediseño (cabecera, mapa en tarjeta, lista con íconos, botones y pie)
    Column(
        Modifier
            .fillMaxSize()
            .background(FondoApp)
    ) {
        CabeceraCampus(estado = estado, onLocalizar = { alPulsar() })

        val actual = estado
        if (actual is UbicacionUi.Error) {
            MensajeError(
                error = actual,
                onAbrirAjustes = { abrirAjustes() },
                onReintentar = { alPulsar() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        TarjetaMapa(
            estado = estado,
            lugares = lugaresVisibles,
            seleccionado = seleccionado,
            idMasCercano = masCercano?.id,
            radio = radio,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        TarjetaLugares(
            hayUbicacion = estado is UbicacionUi.Lista,
            filtrosAbiertos = filtrosAbiertos,
            onAlternarFiltros = { filtrosAbiertos = !filtrosAbiertos },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (filtrosAbiertos) 420.dp else 310.dp)
                .padding(horizontal = 16.dp),
            filtros = {
                // Mejoras 1, 2 y 3 dentro del panel desplegable
                SelectorRadio(
                    radios = vm.radios,
                    actual = radio,
                    onCambiar = { vm.cambiarRadio(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                FiltroCategorias(
                    categorias = vm.categorias,
                    activa = categoria,
                    onSeleccionar = { vm.filtrarCategoria(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                ultimoVisitado?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, null, tint = GrisTexto, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Último visitado: ${it.nombre}", fontSize = 13.sp, color = GrisTexto)
                    }
                }
            },
            lista = { mod ->
                ListaLugares(
                    lugares = lugaresVisibles,
                    seleccionado = seleccionado,
                    onClick = { id -> vm.seleccionar(id) },
                    dentroDelRadio = idsDentroDelRadio,
                    radio = radio,
                    idMasCercano = masCercano?.id,
                    modifier = mod
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        BotonesAccion(
            buscando = estado is UbicacionUi.Buscando,
            hayMasCercano = masCercano != null,
            onUbicar = { alPulsar() },
            onIrAlMasCercano = { masCercano?.let { vm.seleccionar(it.id) } },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(6.dp))
        PieCampus()
    }
}

/** El mapa dentro de una tarjeta redondeada, con la latitud/longitud encima. */
@Composable
private fun TarjetaMapa(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    idMasCercano: Int?,
    radio: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, AzulBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Mejora 4: con clave se usa Google Maps; sin clave, OpenStreetMap
            if (BuildConfig.TIENE_CLAVE_MAPS) {
                MapaUbicacion(
                    estado = estado,
                    lugares = lugares,
                    seleccionado = seleccionado,
                    idMasCercano = idMasCercano,
                    radio = radio,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                MapaOsm(
                    estado = estado,
                    lugares = lugares,
                    seleccionado = seleccionado,
                    idMasCercano = idMasCercano,
                    radio = radio,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (estado is UbicacionUi.Lista) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        "Lat %.5f · Lon %.5f".format(
                            Locale.US, estado.posicion.latitud, estado.posicion.longitud
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AzulNoche
                    )
                }
            }
        }
    }
}

/** Tarjeta "Lugares cercanos" con el panel de filtros desplegable. */
@Composable
private fun TarjetaLugares(
    hayUbicacion: Boolean,
    filtrosAbiertos: Boolean,
    onAlternarFiltros: () -> Unit,
    filtros: @Composable () -> Unit,
    lista: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = AzulRey, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Lugares cercanos",
                    fontSize = 19.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.ExtraBold,
                    color = AzulNoche,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = onAlternarFiltros,
                    shape = RoundedCornerShape(50),
                    color = AzulCielo
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (hayUbicacion) "POR DISTANCIA" else "FILTROS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulRey
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            if (filtrosAbiertos) Icons.Filled.Close else Icons.Filled.FilterList,
                            contentDescription = if (filtrosAbiertos) "Cerrar filtros" else "Abrir filtros",
                            tint = AzulRey,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = filtrosAbiertos) {
                Column(
                    Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) { filtros() }
            }

            Spacer(Modifier.height(10.dp))
            lista(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

/** "Obtener mi ubicación" (relleno) e "Ir al más cercano" (con borde), lado a lado. */
@Composable
private fun BotonesAccion(
    buscando: Boolean,
    hayMasCercano: Boolean,
    onUbicar: () -> Unit,
    onIrAlMasCercano: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onUbicar,
            enabled = !buscando,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = AzulNoche),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Icon(Icons.Filled.MyLocation, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Obtener mi ubicación", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        OutlinedButton(
            onClick = onIrAlMasCercano,
            enabled = hayMasCercano,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(2.dp, if (hayMasCercano) AzulRey else AzulBorde),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulRey),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Icon(Icons.Filled.NearMe, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Ir al más cercano", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}
