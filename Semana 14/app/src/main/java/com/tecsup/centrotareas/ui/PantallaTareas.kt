package com.tecsup.centrotareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.PrimosUi
import com.tecsup.centrotareas.ui.theme.Verde
import com.tecsup.centrotareas.ui.theme.VerdeFondo
import com.tecsup.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas") },
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

@Composable
private fun TarjetaProgreso(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    // Prueba de vida: si deja de sumar, la pantalla está congelada
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Tarjeta("Tarea con progreso") {
        Button(onClick = { toques++ }, modifier = Modifier.fillMaxWidth()) {
            Text("Tócame: $toques")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.tareaLarga() },
                enabled = !corriendo,
                modifier = Modifier.weight(1f)
            ) {
                Text("Iniciar")
            }
            OutlinedButton(
                onClick = { vm.cancelar() },
                enabled = corriendo,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier.fillMaxWidth()
        )
        Text("${(progreso * 100).toInt()} %")
        Text(text = mensaje, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TarjetaPrimos(vm: TareasViewModel) {
    val primos by vm.primos.collectAsState()
    val progresoPrimos by vm.progresoPrimos.collectAsState()
    // Ajustado: con 500 000 este emulador tardaba ~0,5 s; con 3 000 000 tarda ~3 s
    var texto by rememberSaveable { mutableStateOf("3000000") }
    // Ignora textos no numéricos o menores que 2
    val numero = texto.toIntOrNull()?.takeIf { it >= 2 }
    val calculando = primos is PrimosUi.Calculando

    Tarjeta("Contar números primos") {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it.filter(Char::isDigit).take(9) },
            label = { Text("Contar primos hasta N") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = numero == null,
            supportingText = if (numero == null) {
                { Text("Ingresa un número mayor o igual a 2") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { numero?.let { vm.contar(it) } },
                enabled = numero != null && !calculando,
                modifier = Modifier.weight(1f)
            ) {
                Text("Contar")
            }
            OutlinedButton(
                onClick = { vm.cancelarPrimos() },
                enabled = calculando,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
        }
        when (val estado = primos) {
            PrimosUi.Inicial -> Text("Escribe N y pulsa Contar")
            // Mejora 2: barra y porcentaje del cálculo
            PrimosUi.Calculando -> {
                LinearProgressIndicator(
                    progress = { progresoPrimos },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Calculando... ${(progresoPrimos * 100).toInt()} %")
            }
            is PrimosUi.Listo -> RecuadroExito(
                "Primos encontrados: ${estado.cantidad}\nTiempo: ${estado.milisegundos} ms"
            )
            PrimosUi.Cancelado -> Text("Cancelado", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun RecuadroError(mensaje: String, onReintentar: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = mensaje, fontWeight = FontWeight.Medium)
            Button(
                onClick = onReintentar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun RecuadroExito(texto: String) {
    Surface(
        color = VerdeFondo,
        contentColor = Verde,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = texto,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun TarjetaCargas(vm: TareasViewModel) {
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    val avisoCarga by vm.avisoCarga.collectAsState()
    val cargando = carga is CargaUi.Cargando

    Tarjeta("Dos cargas") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Simular error", modifier = Modifier.weight(1f))
            Switch(
                checked = simularError,
                onCheckedChange = { vm.cambiarSimularError(it) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { vm.cargarUnaPorUna() },
                enabled = !cargando,
                modifier = Modifier.weight(1f)
            ) {
                Text("Una por una")
            }
            Button(
                onClick = { vm.cargarALaVez() },
                enabled = !cargando,
                modifier = Modifier.weight(1f)
            ) {
                Text("A la vez")
            }
        }
        when (val estado = carga) {
            CargaUi.Inicial -> Text("Elige cómo cargar el perfil y los pedidos")
            CargaUi.Cargando -> Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                // Mejora 3: si hay reintentos en curso, se avisa aquí
                Text(avisoCarga ?: "Cargando...")
            }
            is CargaUi.Exito -> RecuadroExito(
                "Modo: ${estado.modo}\n" +
                    "Perfil: ${estado.perfil}\n" +
                    "Pedidos: ${estado.pedidos}\n" +
                    "Tiempo: ${estado.milisegundos} ms"
            )
            is CargaUi.Error -> RecuadroError(
                mensaje = estado.mensaje,
                onReintentar = { vm.cargarALaVez() }
            )
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}
