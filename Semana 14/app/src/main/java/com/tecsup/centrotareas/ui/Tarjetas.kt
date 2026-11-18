package com.tecsup.centrotareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.ComparacionTiempos
import com.tecsup.centrotareas.model.PrimosUi
import com.tecsup.centrotareas.ui.theme.AzulOscuro
import com.tecsup.centrotareas.ui.theme.AzulSuave
import com.tecsup.centrotareas.ui.theme.Dorado
import com.tecsup.centrotareas.ui.theme.GrisPista
import com.tecsup.centrotareas.ui.theme.GrisTexto
import com.tecsup.centrotareas.ui.theme.Morado
import com.tecsup.centrotareas.ui.theme.RojoError
import com.tecsup.centrotareas.ui.theme.RojoFondo
import com.tecsup.centrotareas.ui.theme.RojoTexto
import com.tecsup.centrotareas.ui.theme.VerdeExito
import com.tecsup.centrotareas.ui.theme.VerdeFondo
import com.tecsup.centrotareas.viewmodel.TareasViewModel

// ---------------------------------------------------------------- Prueba de respuesta
/** Botón Tócame: si deja de sumar, la pantalla está congelada (prueba de vida). */
@Composable
fun TarjetaRespuesta(toques: Int, onTocar: () -> Unit) {
    TarjetaTarea(Icons.Rounded.TouchApp, "Prueba de respuesta", "Toca el botón varias veces") {
        Button(
            onClick = onTocar,
            shape = FormaPildora,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Tócame: $toques", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Si el contador deja de sumar, la pantalla está congelada",
            style = MaterialTheme.typography.bodySmall,
            color = GrisTexto,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------- Tarea con progreso (pasos 2 a 4)
@Composable
fun TarjetaProgreso(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    val cancelado = mensaje == "Cancelado"
    val porcentaje = (progreso * 100).toInt()

    TarjetaTarea(Icons.Rounded.RocketLaunch, "Tarea con progreso", "Realiza una tarea larga") {
        // Anillo grande con el porcentaje en el centro
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            CircularProgressIndicator(
                progress = { progreso },
                modifier = Modifier.size(118.dp),
                strokeWidth = 11.dp,
                color = if (cancelado) Morado else MaterialTheme.colorScheme.primary,
                trackColor = GrisPista,
                strokeCap = StrokeCap.Round
            )
            Text("$porcentaje%", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        // Barra lineal y porcentaje debajo (lo que pide la guía en el Paso 3)
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            trackColor = GrisPista,
            strokeCap = StrokeCap.Round
        )
        Text(
            "$porcentaje % · $mensaje",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        when {
            corriendo -> ChipEstado(
                "Por favor, espera un momento", Icons.Rounded.HourglassTop, AzulSuave, AzulOscuro
            )
            cancelado -> ChipEstado("Cancelado", Icons.Rounded.Cancel, RojoFondo, RojoError)
            mensaje == "Terminó" -> ChipEstado(
                "Terminó", Icons.Rounded.CheckCircle, VerdeFondo, VerdeExito
            )
            else -> ChipEstado("Listo", Icons.Rounded.CheckCircle, VerdeFondo, VerdeExito)
        }
        // Iniciar deshabilitado mientras corre; Cancelar solo mientras corre
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { vm.tareaLarga() },
                enabled = !corriendo,
                shape = FormaPildora,
                modifier = Modifier.weight(1f)
            ) { Text("Iniciar") }
            OutlinedButton(
                onClick = { vm.cancelar() },
                enabled = corriendo,
                shape = FormaPildora,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }
        }
    }
}

// ---------------------------------------------------------------- Contar números primos (paso 5)
@Composable
fun TarjetaPrimos(vm: TareasViewModel) {
    val primos by vm.primos.collectAsState()
    val progresoPrimos by vm.progresoPrimos.collectAsState()
    // Con 500 000 este emulador tardaba ~0,5 s; con 3 000 000 tarda ~3 s (guía: 2 a 5 s)
    var texto by rememberSaveable { mutableStateOf("3000000") }
    var ultimoN by rememberSaveable { mutableIntStateOf(0) }
    // Ignora textos no numéricos o menores que 2
    val numero = texto.toIntOrNull()?.takeIf { it >= 2 }
    val calculando = primos is PrimosUi.Calculando

    TarjetaTarea(Icons.Rounded.Calculate, "Contar números primos", "Ingresa un número y calcula") {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it.filter(Char::isDigit).take(9) },
            label = { Text("Contar primos hasta N") },
            singleLine = true,
            shape = FormaTarjeta,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = numero == null,
            supportingText = if (numero == null) {
                { Text("Ingresa un número mayor o igual a 2") }
            } else null,
            trailingIcon = {
                if (texto.isNotEmpty() && !calculando) {
                    IconButton(onClick = { texto = "" }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Borrar número")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { numero?.let { ultimoN = it; vm.contar(it) } },
                enabled = numero != null && !calculando,
                shape = FormaPildora,
                modifier = Modifier.weight(1f)
            ) { Text("Contar") }
            OutlinedButton(
                onClick = { vm.cancelarPrimos() },
                enabled = calculando,
                shape = FormaPildora,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }
        }
        when (val estado = primos) {
            PrimosUi.Inicial -> Text(
                "Escribe N y pulsa Contar",
                color = GrisTexto,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            // Mejora 2: barra y porcentaje del cálculo
            PrimosUi.Calculando -> {
                LinearProgressIndicator(
                    progress = { progresoPrimos },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = GrisPista,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "Calculando... ${(progresoPrimos * 100).toInt()} %",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is PrimosUi.Listo -> ResultadoPrimos(ultimoN, estado.cantidad, estado.milisegundos)
            PrimosUi.Cancelado -> ChipEstado("Cancelado", Icons.Rounded.Cancel, RojoFondo, RojoError)
        }
    }
}

@Composable
private fun ResultadoPrimos(hasta: Int, cantidad: Int, ms: Long) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Rounded.EmojiEvents,
            contentDescription = null,
            tint = Dorado,
            modifier = Modifier.size(64.dp)
        )
        RecuadroConIcono(Icons.Rounded.CheckCircle, VerdeFondo, VerdeExito, TextoResultado) {
            Text("Primos hasta $hasta:", fontWeight = FontWeight.SemiBold)
            Text("$cantidad", fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Tiempo: $ms ms", fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ---------------------------------------------------------------- Dos cargas (pasos 6 y 7 + mejora 3)
@Composable
fun TarjetaCargas(vm: TareasViewModel) {
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    val avisoCarga by vm.avisoCarga.collectAsState()
    val cargando = carga is CargaUi.Cargando

    TarjetaTarea(Icons.Rounded.CloudDownload, "Dos cargas", "Carga dos datos simulados") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Simular error",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = simularError, onCheckedChange = { vm.cambiarSimularError(it) })
        }
        // Mientras carga se deshabilitan: no se lanza una segunda carga
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { vm.cargarUnaPorUna() },
                enabled = !cargando,
                shape = FormaPildora,
                modifier = Modifier.weight(1f)
            ) { Text("Una por una") }
            Button(
                onClick = { vm.cargarALaVez() },
                enabled = !cargando,
                shape = FormaPildora,
                modifier = Modifier.weight(1f)
            ) { Text("A la vez") }
        }
        when (val estado = carga) {
            CargaUi.Inicial -> Text(
                "Elige cómo cargar el perfil y los pedidos",
                color = GrisTexto,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            CargaUi.Cargando -> Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
                // Mejora 3: si hay reintentos en curso, se avisa aquí
                Text(avisoCarga ?: "Cargando...")
            }
            is CargaUi.Exito -> RecuadroConIcono(
                Icons.Rounded.CheckCircle, VerdeFondo, VerdeExito, TextoResultado
            ) {
                Text("Modo: ${estado.modo}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Perfil: ${estado.perfil}")
                Text("Pedidos: ${estado.pedidos}")
                Text("Tiempo: ${estado.milisegundos} ms", fontWeight = FontWeight.SemiBold)
            }
            is CargaUi.Error -> RecuadroConIcono(
                Icons.Rounded.Warning, RojoFondo, RojoError, RojoTexto
            ) {
                Text(estado.mensaje, fontWeight = FontWeight.SemiBold)
                Button(onClick = { vm.cargarALaVez() }, shape = FormaPildora) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Text("Reintentar", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Comparación de tiempos (mejora 4)
@Composable
fun TarjetaComparacion(vm: TareasViewModel) {
    val datos by vm.comparacion.collectAsState()
    val vacia = datos.unaPorUna.isEmpty() && datos.aLaVez.isEmpty()

    TarjetaTarea(Icons.Rounded.BarChart, "Comparación de tiempos", "Una por una contra A la vez") {
        if (vacia) {
            Text(
                "Haz cargas en Inicio y aquí verás sus tiempos",
                color = GrisTexto,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TablaComparacion(datos)
            TextButton(onClick = { vm.limpiarComparacion() }) { Text("Limpiar tabla") }
        }
    }
}

@Composable
private fun TablaComparacion(datos: ComparacionTiempos) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FilaTabla("Modo", "Veces", "Último", "Promedio", encabezado = true)
        HorizontalDivider(color = GrisPista)
        FilaModo("Una por una", datos.unaPorUna)
        FilaModo("A la vez", datos.aLaVez)
        if (datos.unaPorUna.isNotEmpty() && datos.aLaVez.isNotEmpty()) {
            val ahorro = datos.unaPorUna.average() - datos.aLaVez.average()
            val porcentaje = ahorro / datos.unaPorUna.average() * 100
            ChipEstado(
                "A la vez ahorra ${ahorro.toLong()} ms (${porcentaje.toInt()} %)",
                Icons.Rounded.CheckCircle, VerdeFondo, VerdeExito
            )
        }
        Text(
            "Solo cuenta cargas sin reintentos",
            style = MaterialTheme.typography.bodySmall,
            color = GrisTexto
        )
    }
}

@Composable
private fun FilaModo(modo: String, tiempos: List<Long>) {
    FilaTabla(
        modo,
        tiempos.size.toString(),
        tiempos.lastOrNull()?.let { "$it ms" } ?: "—",
        if (tiempos.isEmpty()) "—" else "${tiempos.average().toLong()} ms"
    )
}

@Composable
private fun FilaTabla(c1: String, c2: String, c3: String, c4: String, encabezado: Boolean = false) {
    val peso = if (encabezado) FontWeight.Bold else FontWeight.Normal
    val color = if (encabezado) GrisTexto else MaterialTheme.colorScheme.onSurface
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(c1, fontWeight = peso, color = color, modifier = Modifier.weight(1.4f))
        Text(c2, fontWeight = peso, color = color, modifier = Modifier.weight(0.8f))
        Text(c3, fontWeight = peso, color = color, modifier = Modifier.weight(1f))
        Text(c4, fontWeight = peso, color = color, modifier = Modifier.weight(1.1f))
    }
}

// Verde oscuro para el texto sobre los recuadros de éxito
private val TextoResultado = Color(0xFF14432A)
