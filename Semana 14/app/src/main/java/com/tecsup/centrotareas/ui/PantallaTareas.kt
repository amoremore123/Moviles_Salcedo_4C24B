package com.tecsup.centrotareas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.ComparacionTiempos
import com.tecsup.centrotareas.model.PrimosUi
import com.tecsup.centrotareas.ui.theme.Dorado
import com.tecsup.centrotareas.ui.theme.FondoApp
import com.tecsup.centrotareas.ui.theme.FondoCabecera
import com.tecsup.centrotareas.ui.theme.GrisTexto
import com.tecsup.centrotareas.ui.theme.Indigo
import com.tecsup.centrotareas.ui.theme.IndigoOscuro
import com.tecsup.centrotareas.ui.theme.LavandaBorde
import com.tecsup.centrotareas.ui.theme.LavandaSuave
import com.tecsup.centrotareas.ui.theme.PistaBarra
import com.tecsup.centrotareas.ui.theme.RojoError
import com.tecsup.centrotareas.ui.theme.RojoFondo
import com.tecsup.centrotareas.ui.theme.RojoTexto
import com.tecsup.centrotareas.ui.theme.TextoVerde
import com.tecsup.centrotareas.ui.theme.VerdeFondo
import com.tecsup.centrotareas.ui.theme.Violeta
import com.tecsup.centrotareas.viewmodel.TareasViewModel
import java.text.NumberFormat
import java.util.Locale

private val FormaTarjeta = RoundedCornerShape(28.dp)
private val FormaPildora = RoundedCornerShape(50)
private val FormaRecuadro = RoundedCornerShape(18.dp)
private val DegradadoPrincipal = Brush.linearGradient(listOf(Indigo, Violeta))
private val Miles: NumberFormat = NumberFormat.getIntegerInstance(Locale("es", "PE"))

/**
 * Pantalla única, como en la sección 7 de la guía: arriba el título CentroTareas y,
 * debajo, las tres tarjetas en orden (Tarea con progreso, Contar números primos, Dos cargas).
 * La View solo muestra el estado del ViewModel y le avisa los eventos: no lanza corrutinas.
 */
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCabecera)
            .statusBarsPadding() // el contenido nunca pasa por debajo de la barra de estado
            .background(FondoApp)
            .verticalScroll(rememberScrollState())
    ) {
        Cabecera()
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TarjetaProgreso(vm)
            TarjetaPrimos(vm)
            TarjetaCargas(vm)
        }
        PieDeAnimo()
    }
}

// ---------------------------------------------------------------- Cabecera y pie
@Composable
private fun Cabecera() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.verticalGradient(listOf(FondoCabecera, FondoApp)))
            .padding(start = 20.dp, end = 16.dp, top = 18.dp, bottom = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Ícono de la app: portapapeles con check sobre el degradado del tema
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DegradadoPrincipal)
            ) {
                Icon(
                    Icons.Rounded.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    "CentroTareas",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = IndigoOscuro
                )
                Text("Que la app nunca se congele ♡", fontSize = 14.sp, color = IndigoOscuro)
            }
            Box {
                Text("😺", fontSize = 46.sp)
                Text("✦", color = Dorado, fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
private fun PieDeAnimo() {
    Box(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            .background(Brush.verticalGradient(listOf(FondoCabecera, Violeta.copy(alpha = 0.75f), Indigo)))
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("😸", fontSize = 54.sp)
            Text("♥", color = Violeta, fontSize = 18.sp, modifier = Modifier.padding(bottom = 36.dp))
            Spacer(Modifier.weight(1f))
            Text(
                "¡Tú puedes! ♡",
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color.White,
                modifier = Modifier.rotate(-8f)
            )
        }
    }
}

// ---------------------------------------------------------------- Pasos 1 a 4 (+ mejora 1)
@Composable
private fun TarjetaProgreso(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    val porcentaje = (progreso * 100).toInt()
    // Prueba de vida: si deja de sumar, la pantalla está congelada
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Tarjeta(
        Icons.Rounded.RocketLaunch,
        "Tarea con progreso",
        "Realiza una tarea larga y observa el avance",
        decoracion = { Decoracion("🚀") }
    ) {
        Button(
            onClick = { toques++ },
            shape = FormaPildora,
            colors = ButtonDefaults.buttonColors(containerColor = LavandaSuave, contentColor = Indigo),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Rounded.TouchApp, contentDescription = null)
            Text("Tócame: $toques", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        // Panel con el anillo y la barra
        Surface(color = LavandaSuave.copy(alpha = 0.55f), shape = FormaRecuadro) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(14.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.size(104.dp),
                        strokeWidth = 10.dp,
                        color = Indigo,
                        trackColor = PistaBarra,
                        strokeCap = StrokeCap.Round
                    )
                    Text("$porcentaje %", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = IndigoOscuro)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "$mensaje ${emojiDelMensaje(mensaje)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colorDelMensaje(mensaje)
                    )
                    // LinearProgressIndicator y el porcentaje debajo (Paso 3)
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = Indigo,
                        trackColor = PistaBarra,
                        strokeCap = StrokeCap.Round
                    )
                    Text("$porcentaje %", color = GrisTexto, fontSize = 13.sp)
                }
            }
        }
        // Iniciar se deshabilita mientras corre; Cancelar solo se habilita mientras corre
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonPrincipal("Iniciar", Icons.Rounded.PlayArrow, enabled = !corriendo, modifier = Modifier.weight(1f)) {
                vm.tareaLarga()
            }
            BotonContorno("Cancelar", Icons.Rounded.Close, enabled = corriendo, modifier = Modifier.weight(1f)) {
                vm.cancelar()
            }
        }
    }
}

private fun emojiDelMensaje(mensaje: String): String = when (mensaje) {
    "Trabajando..." -> "💪"
    "Terminó" -> "🎉"
    "Cancelado" -> "✋"
    else -> "✨"
}

@Composable
private fun colorDelMensaje(mensaje: String): Color = when (mensaje) {
    "Terminó" -> TextoVerde
    "Cancelado" -> RojoError
    else -> IndigoOscuro
}

// ---------------------------------------------------------------- Paso 5 (+ mejoras 2 y 5)
@Composable
private fun TarjetaPrimos(vm: TareasViewModel) {
    val primos by vm.primos.collectAsState()
    val progresoPrimos by vm.progresoPrimos.collectAsState()
    // La guía sugiere 500 000, pero pide ajustarlo si tarda menos de 1 s:
    // en este emulador 500 000 tardaba ~0,5 s y 3 000 000 tarda ~2-3 s (rango pedido: 2 a 5 s)
    var texto by rememberSaveable { mutableStateOf("3000000") }
    var ultimoN by rememberSaveable { mutableIntStateOf(0) }
    // Ignora textos no numéricos o menores que 2
    val numero = texto.toIntOrNull()?.takeIf { it >= 2 }
    val calculando = primos is PrimosUi.Calculando

    Tarjeta(
        Icons.Rounded.Calculate,
        "Contar números primos",
        "Cuántos primos hay hasta N y cuánto suman",
        decoracion = { Decoracion("🧮") }
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it.filter(Char::isDigit).take(9) },
            label = { Text("Hasta el número") },
            singleLine = true,
            shape = FormaPildora,
            leadingIcon = { Icon(Icons.Rounded.Tag, contentDescription = null, tint = Indigo) },
            trailingIcon = {
                if (texto.isNotEmpty() && !calculando) {
                    IconButton(onClick = { texto = "" }) {
                        Icon(Icons.Rounded.Cancel, contentDescription = "Borrar número", tint = GrisTexto)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = LavandaBorde),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = numero == null,
            supportingText = if (numero == null) {
                { Text("Ingresa un número mayor o igual a 2") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonPrincipal(
                "Contar", Icons.Rounded.Calculate,
                enabled = numero != null && !calculando,
                modifier = Modifier.weight(1f)
            ) { numero?.let { ultimoN = it; vm.contar(it) } }
            BotonContorno("Cancelar", Icons.Rounded.Close, enabled = calculando, modifier = Modifier.weight(1f)) {
                vm.cancelarPrimos()
            }
        }
        when (val estado = primos) {
            PrimosUi.Inicial -> TextoAyuda("Escribe un número y pulsa Contar")
            // Mejora 2: porcentaje del cálculo mientras avanza
            PrimosUi.Calculando -> {
                LinearProgressIndicator(
                    progress = { progresoPrimos },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = Indigo,
                    trackColor = PistaBarra,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "Calculando... ${(progresoPrimos * 100).toInt()} %",
                    color = IndigoOscuro,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is PrimosUi.Listo -> RecuadroExito {
                Text("Primos hasta ${Miles.format(ultimoN)}:", fontWeight = FontWeight.SemiBold)
                Text(Miles.format(estado.cantidad), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                // Mejora 5: suma de todos esos primos
                Text("Suma de esos primos: ${Miles.format(estado.suma)}", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Tiempo: ${estado.milisegundos} ms")
                }
            }
            PrimosUi.Cancelado -> Chip("Cancelado", Icons.Rounded.Cancel, RojoFondo, RojoError)
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

    Tarjeta(
        Icons.Rounded.CloudSync,
        "Dos cargas",
        "Simula cargas de datos y compara",
        decoracion = { Decoracion("☁️") }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = simularError, onCheckedChange = { vm.cambiarSimularError(it) })
            Text("Simular error", fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 12.dp))
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = RojoError,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(22.dp)
            )
        }
        // Deshabilitados mientras carga: no se lanza una segunda carga
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonContorno("Una por una", Icons.Rounded.Layers, enabled = !cargando, modifier = Modifier.weight(1f)) {
                vm.cargarUnaPorUna()
            }
            BotonPrincipal("A la vez", Icons.Rounded.Bolt, enabled = !cargando, modifier = Modifier.weight(1f)) {
                vm.cargarALaVez()
            }
        }
        when (val estado = carga) {
            CargaUi.Inicial -> TextoAyuda("«Una por una» tarda ≈ 5000 ms y «A la vez» ≈ 3000 ms")
            CargaUi.Cargando -> Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 3.dp, color = Indigo)
                // Mejora 3: si hay reintentos automáticos en curso, se avisan aquí
                Text(avisoCarga ?: "Cargando...", color = IndigoOscuro)
            }
            is CargaUi.Exito -> RecuadroExito {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                    Text(" Modo: ${estado.modo}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Text("Perfil: ${estado.perfil}")
                Text("Pedidos: ${estado.pedidos}")
                Text("Tiempo: ${estado.milisegundos} ms", fontWeight = FontWeight.SemiBold)
            }
            is CargaUi.Error -> {
                // Recuadro de color de error y, debajo, Reintentar (diseño del Paso 7)
                Surface(color = RojoFondo, contentColor = RojoTexto, shape = FormaRecuadro) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = RojoError)
                        Text(
                            estado.mensaje,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
                BotonContorno(
                    "Reintentar", Icons.Rounded.Refresh, enabled = true,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { vm.cargarALaVez() }
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
        HorizontalDivider(color = LavandaBorde, modifier = Modifier.padding(vertical = 4.dp))
        Text("Comparación de tiempos", fontWeight = FontWeight.Bold, color = Indigo)
        FilaTabla("Modo", "Veces", "Último", "Promedio", encabezado = true)
        FilaModo("Una por una", datos.unaPorUna)
        FilaModo("A la vez", datos.aLaVez)
        if (datos.unaPorUna.isNotEmpty() && datos.aLaVez.isNotEmpty()) {
            val ahorro = datos.unaPorUna.average() - datos.aLaVez.average()
            val porcentaje = ahorro / datos.unaPorUna.average() * 100
            Chip(
                "A la vez ahorra ${ahorro.toLong()} ms (${porcentaje.toInt()} %)",
                Icons.Rounded.Bolt, VerdeFondo, TextoVerde
            )
        }
        Text("Solo cuenta cargas sin reintentos", style = MaterialTheme.typography.bodySmall, color = GrisTexto)
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
/** Card blanca con ícono en círculo, título (color del tema) y subtítulo. */
@Composable
private fun Tarjeta(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    decoracion: (@Composable () -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaTarjeta,
        border = BorderStroke(1.dp, LavandaBorde),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(LavandaSuave)
                ) {
                    Icon(icono, contentDescription = null, tint = Indigo, modifier = Modifier.size(30.dp))
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(titulo, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Indigo)
                    Text(subtitulo, fontSize = 13.sp, color = GrisTexto)
                }
                decoracion?.invoke()
            }
            contenido()
        }
    }
}

/** Dibujo pequeño a la derecha del título (emoji sobre degradado lavanda), con estrellita. */
@Composable
private fun Decoracion(emoji: String) {
    Box(modifier = Modifier.padding(start = 6.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(LavandaSuave, LavandaBorde)))
        ) {
            Text(emoji, fontSize = 30.sp)
        }
        Text("✦", color = Dorado, fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun BotonPrincipal(
    texto: String,
    icono: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = FormaPildora,
        contentPadding = PaddingValues(horizontal = 14.dp),
        modifier = modifier.height(52.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun BotonContorno(
    texto: String,
    icono: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = FormaPildora,
        border = BorderStroke(1.5.dp, if (enabled) LavandaBorde else PistaBarra),
        contentPadding = PaddingValues(horizontal = 14.dp),
        modifier = modifier.height(52.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = contenido
        )
    }
}

@Composable
private fun Chip(texto: String, icono: ImageVector, fondo: Color, color: Color) {
    Surface(color = fondo, contentColor = color, shape = FormaPildora, modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(texto, fontWeight = FontWeight.SemiBold)
        }
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
