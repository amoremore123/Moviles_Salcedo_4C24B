package com.tecsup.centrotareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.ComparacionTiempos
import com.tecsup.centrotareas.model.PrimosUi
import com.tecsup.centrotareas.ui.theme.AzulOscuro
import com.tecsup.centrotareas.ui.theme.AzulSuave
import com.tecsup.centrotareas.ui.theme.GrisPista
import com.tecsup.centrotareas.ui.theme.GrisTexto
import com.tecsup.centrotareas.ui.theme.RojoFondo
import com.tecsup.centrotareas.ui.theme.RojoTexto
import com.tecsup.centrotareas.ui.theme.TextoVerde
import com.tecsup.centrotareas.ui.theme.VerdeFondo
import com.tecsup.centrotareas.viewmodel.TareasViewModel

private val FormaTarjeta = RoundedCornerShape(20.dp)
private val FormaRecuadro = RoundedCornerShape(14.dp)

/**
 * Pantalla única, como en la sección 7 de la guía:
 * barra superior y tres tarjetas en orden (Tarea con progreso, Contar números primos, Dos cargas).
 * La View solo muestra el estado del ViewModel y le avisa los eventos: no lanza corrutinas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaProgreso(vm)
            TarjetaPrimos(vm)
            TarjetaCargas(vm)
        }
    }
}

// ---------------------------------------------------------------- Pasos 1 a 4 (+ mejora 1)
@Composable
private fun TarjetaProgreso(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    // Prueba de vida: si deja de sumar, la pantalla está congelada
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Tarjeta(Icons.Rounded.RocketLaunch, "Tarea con progreso") {
        Button(onClick = { toques++ }, modifier = Modifier.fillMaxWidth()) {
            Text("Tócame: $toques", fontWeight = FontWeight.Bold)
        }
        // Iniciar se deshabilita mientras corre; Cancelar solo se habilita mientras corre
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { vm.tareaLarga() },
                enabled = !corriendo,
                modifier = Modifier.weight(1f)
            ) { Text("Iniciar") }
            OutlinedButton(
                onClick = { vm.cancelar() },
                enabled = corriendo,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }
        }
        // LinearProgressIndicator y el porcentaje debajo
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            trackColor = GrisPista,
            strokeCap = StrokeCap.Round
        )
        Text(
            "${(progreso * 100).toInt()} %",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = colorDelMensaje(mensaje),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun colorDelMensaje(mensaje: String): Color = when (mensaje) {
    "Terminó" -> TextoVerde
    "Cancelado" -> MaterialTheme.colorScheme.error
    "Trabajando..." -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
}

// ---------------------------------------------------------------- Paso 5 (+ mejora 2)
@Composable
private fun TarjetaPrimos(vm: TareasViewModel) {
    val primos by vm.primos.collectAsState()
    val progresoPrimos by vm.progresoPrimos.collectAsState()
    // La guía sugiere 500 000, pero pide ajustarlo si tarda menos de 1 s:
    // en este emulador 500 000 tardaba ~0,5 s y 3 000 000 tarda ~3 s (rango pedido: 2 a 5 s)
    var texto by rememberSaveable { mutableStateOf("3000000") }
    var ultimoN by rememberSaveable { mutableIntStateOf(0) }
    // Ignora textos no numéricos o menores que 2
    val numero = texto.toIntOrNull()?.takeIf { it >= 2 }
    val calculando = primos is PrimosUi.Calculando

    Tarjeta(Icons.Rounded.Calculate, "Contar números primos") {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it.filter(Char::isDigit).take(9) },
            label = { Text("Hasta el número") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = numero == null,
            supportingText = if (numero == null) {
                { Text("Ingresa un número mayor o igual a 2") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { numero?.let { ultimoN = it; vm.contar(it) } },
                enabled = numero != null && !calculando,
                modifier = Modifier.weight(1f)
            ) { Text("Contar") }
            OutlinedButton(
                onClick = { vm.cancelarPrimos() },
                enabled = calculando,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }
        }
        when (val estado = primos) {
            PrimosUi.Inicial -> TextoAyuda("Escribe un número y pulsa Contar")
            // Mejora 2: porcentaje del cálculo mientras avanza
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
            is PrimosUi.Listo -> RecuadroExito {
                Text("Primos hasta $ultimoN: ${estado.cantidad}", fontWeight = FontWeight.Bold)
                Text("Tiempo: ${estado.milisegundos} ms")
            }
            PrimosUi.Cancelado -> Text(
                "Cancelado",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------- Pasos 6 y 7 (+ mejoras 3 y 4)
@Composable
private fun TarjetaCargas(vm: TareasViewModel) {
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    val avisoCarga by vm.avisoCarga.collectAsState()
    val comparacion by vm.comparacion.collectAsState()
    val cargando = carga is CargaUi.Cargando

    Tarjeta(Icons.Rounded.CloudDownload, "Dos cargas") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = simularError, onCheckedChange = { vm.cambiarSimularError(it) })
            Text("Simular error", modifier = Modifier.padding(start = 10.dp))
        }
        // Deshabilitados mientras carga: no se lanza una segunda carga
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { vm.cargarUnaPorUna() },
                enabled = !cargando,
                modifier = Modifier.weight(1f)
            ) { Text("Una por una") }
            Button(
                onClick = { vm.cargarALaVez() },
                enabled = !cargando,
                modifier = Modifier.weight(1f)
            ) { Text("A la vez") }
        }
        when (val estado = carga) {
            CargaUi.Inicial -> TextoAyuda("«Una por una» tarda ≈ 5000 ms y «A la vez» ≈ 3000 ms")
            CargaUi.Cargando -> Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
                // Mejora 3: si hay reintentos automáticos en curso, se avisan aquí
                Text(avisoCarga ?: "Cargando...")
            }
            is CargaUi.Exito -> RecuadroExito {
                Text("Modo: ${estado.modo}", fontWeight = FontWeight.Bold)
                Text("Perfil: ${estado.perfil}")
                Text("Pedidos: ${estado.pedidos}")
                Text("Tiempo: ${estado.milisegundos} ms")
            }
            is CargaUi.Error -> {
                Surface(
                    color = RojoFondo,
                    contentColor = RojoTexto,
                    shape = FormaRecuadro,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        estado.mensaje,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                // Reintentar vuelve a ejecutar la carga «A la vez»
                OutlinedButton(
                    onClick = { vm.cargarALaVez() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("Reintentar") }
            }
        }
        // Mejora 4: tabla que acumula los tiempos de cada modo
        if (comparacion.unaPorUna.isNotEmpty() || comparacion.aLaVez.isNotEmpty()) {
            TablaComparacion(comparacion, onLimpiar = { vm.limpiarComparacion() })
        }
    }
}

@Composable
private fun TablaComparacion(datos: ComparacionTiempos, onLimpiar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider(color = GrisPista, modifier = Modifier.padding(vertical = 4.dp))
        Text(
            "Comparación de tiempos",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FilaTabla("Modo", "Veces", "Último", "Promedio", encabezado = true)
        FilaModo("Una por una", datos.unaPorUna)
        FilaModo("A la vez", datos.aLaVez)
        if (datos.unaPorUna.isNotEmpty() && datos.aLaVez.isNotEmpty()) {
            val ahorro = datos.unaPorUna.average() - datos.aLaVez.average()
            val porcentaje = ahorro / datos.unaPorUna.average() * 100
            Text(
                "A la vez ahorra ${ahorro.toLong()} ms (${porcentaje.toInt()} %) en promedio",
                color = TextoVerde,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            "Solo cuenta cargas sin reintentos",
            style = MaterialTheme.typography.bodySmall,
            color = GrisTexto
        )
        TextButton(onClick = onLimpiar) { Text("Limpiar tabla") }
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

// ---------------------------------------------------------------- Piezas comunes
/** Card con el título en azul (y un ícono que acompaña al título). */
@Composable
private fun Tarjeta(icono: ImageVector, titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(color = AzulSuave, contentColor = AzulOscuro, shape = FormaRecuadro) {
                    Icon(icono, contentDescription = null, modifier = Modifier.padding(6.dp))
                }
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            contenido()
        }
    }
}

/** Recuadro verde con el resultado y el tiempo en milisegundos. */
@Composable
private fun RecuadroExito(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = VerdeFondo,
        contentColor = TextoVerde,
        shape = FormaRecuadro,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = contenido
        )
    }
}

@Composable
private fun TextoAyuda(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = GrisTexto,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
