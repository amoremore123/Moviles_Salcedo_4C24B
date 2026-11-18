package com.tecsup.centrotareas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Rule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.centrotareas.ui.theme.AzulOscuro
import com.tecsup.centrotareas.ui.theme.AzulSuave
import com.tecsup.centrotareas.ui.theme.GrisTexto
import com.tecsup.centrotareas.viewmodel.TareasViewModel

private enum class Pestana(val titulo: String, val icono: ImageVector) {
    Inicio("Inicio", Icons.Rounded.Home),
    Resultados("Resultados", Icons.Rounded.BarChart),
    AcercaDe("Acerca de", Icons.Rounded.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    // Estado solo de la vista: pestaña elegida y contador de la prueba de vida.
    // rememberSaveable los conserva al rotar; las tareas viven en el ViewModel.
    var pestana by rememberSaveable { mutableIntStateOf(Pestana.Inicio.ordinal) }
    var toques by rememberSaveable { mutableIntStateOf(0) }
    val estadoPestanas = rememberSaveableStateHolder()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { pestana = Pestana.AcercaDe.ordinal }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Acerca de")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Pestana.entries.forEach { p ->
                    NavigationBarItem(
                        selected = pestana == p.ordinal,
                        onClick = { pestana = p.ordinal },
                        icon = { Icon(p.icono, contentDescription = null) },
                        label = { Text(p.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = AzulSuave
                        )
                    )
                }
            }
        }
    ) { padding ->
        // Cada pestaña guarda su propio estado (scroll, número de primos) al cambiar de pestaña
        estadoPestanas.SaveableStateProvider(pestana) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (Pestana.entries[pestana]) {
                    Pestana.Inicio -> {
                        TarjetaSaludo()
                        TarjetaRespuesta(toques) { toques++ }
                        TarjetaProgreso(vm)
                        TarjetaCargas(vm)
                    }
                    Pestana.Resultados -> {
                        // Prueba de vida también aquí: debe sumar mientras se cuentan primos
                        TarjetaRespuesta(toques) { toques++ }
                        TarjetaPrimos(vm)
                        TarjetaComparacion(vm)
                    }
                    Pestana.AcercaDe -> PantallaAcercaDe()
                }
            }
        }
    }
}

@Composable
private fun TarjetaSaludo() {
    Card(
        shape = FormaTarjeta,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(AzulSuave)
            ) {
                Text("🐱", fontSize = 34.sp)
            }
            Surface(color = AzulSuave, shape = FormaTarjeta, modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text("¡Hola! 👋", fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text(
                        "Explora las tareas de este laboratorio",
                        color = AzulOscuro,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PantallaAcercaDe() {
    TarjetaTarea(Icons.Rounded.AssignmentTurnedIn, "CentroTareas", "Que la pantalla nunca se congele") {
        Text(
            "Laboratorio de la Semana 14 (Hilos en Android). Tres trabajos que tardan " +
                "corren en segundo plano con corrutinas, sin bloquear nunca el hilo principal.",
            color = GrisTexto
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiqueta("Corrutinas")
            Etiqueta("MVVM")
            Etiqueta("Compose")
        }
    }
    TarjetaTarea(Icons.Rounded.CheckCircle, "Qué demuestra cada tarea", "Lo que se aprende") {
        Punto("Tarea con progreso", "viewModelScope, Flow con flowOn, Job y cancelación")
        Punto("Contar primos", "Dispatchers.Default y ensureActive() en un cálculo pesado")
        Punto("Dos cargas", "async/await en paralelo, try/catch y reintento automático")
        Punto("Tócame", "Prueba de vida: si deja de sumar, la pantalla está congelada")
    }
    TarjetaTarea(Icons.Rounded.Rule, "Reglas de oro", "Buenas prácticas del laboratorio") {
        Punto("Prohibido", "Bloquear el hilo principal, usar un scope global o dormir el hilo")
        Punto("Cancelación", "Siempre se relanza la CancellationException")
        Punto("Datos", "Todos son ficticios: Ana Torres y los pedidos 101, 102 y 103")
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Surface(color = AzulSuave, contentColor = AzulOscuro, shape = FormaPildora) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Punto(titulo: String, detalle: String) {
    Column {
        Text(titulo, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text(detalle, color = GrisTexto, style = MaterialTheme.typography.bodyMedium)
    }
}
