package com.tecsup.avisoscampus.ui

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tecsup.avisoscampus.notificacion.Notificador
import com.tecsup.avisoscampus.receiver.ModoAvionReceiver
import com.tecsup.avisoscampus.viewmodel.AvisosViewModel

// Nombres de las rutas de navegación
object Rutas {
    const val INICIO = "inicio"
    const val ENVIAR = "enviar"
    const val RECORDATORIO = "recordatorio"
    const val AVION = "avion"
    const val HISTORIAL = "historial"
    const val ACERCA = "acerca"
}

// Destinos de la barra inferior
private data class Destino(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector,
    val iconoElegido: ImageVector
)

private val destinosBarra = listOf(
    Destino(Rutas.INICIO, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    Destino(Rutas.HISTORIAL, "Historial", Icons.Outlined.BarChart, Icons.Filled.BarChart),
    Destino(Rutas.ACERCA, "Acerca de", Icons.Outlined.Info, Icons.Filled.Info)
)

// Raíz de la UI: un solo ViewModel compartido por todas las pantallas.
// Los efectos que deben vivir mientras la app esté abierta (notificar eventos,
// revisar el permiso y el receptor del modo avión) están aquí, no en una pantalla,
// para que sigan funcionando aunque el usuario navegue.
@Composable
fun AppAvisosCampus(vm: AvisosViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    // Si el permiso ya estaba concedido (o no hace falta pedirlo), lo reflejamos al abrir
    LaunchedEffect(Unit) {
        if (Notificador.tienePermiso(context)) vm.permisoRespondido(true)
    }

    // Cada aviso que emite el ViewModel se convierte en notificación
    LaunchedEffect(Unit) {
        vm.eventos.collect { aviso ->
            // mostrar devuelve false si no hay permiso: no falla, solo avisamos
            if (!Notificador.mostrar(context, aviso)) vm.noSePudoMostrar()
        }
    }

    // Mientras el switch esté encendido, el receptor queda registrado;
    // al apagarlo (o cerrarse la app) se da de baja en onDispose
    val escuchando by vm.escuchandoAvion.collectAsState()
    DisposableEffect(escuchando) {
        if (escuchando) {
            val receptor = ModoAvionReceiver { activo -> vm.alCambiarModoAvion(activo) }
            ContextCompat.registerReceiver(
                context,
                receptor,
                IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            onDispose { context.unregisterReceiver(receptor) }
        } else {
            onDispose { }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // La barra solo aparece en las pantallas principales
            if (destinosBarra.any { it.ruta == rutaActual }) {
                BarraInferior(rutaActual) { navController.irA(it) }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.INICIO,
            modifier = Modifier.padding(padding)
        ) {
            val volver: () -> Unit = { navController.popBackStack() }

            composable(Rutas.INICIO) {
                PantallaInicio(vm = vm, alNavegar = { navController.navigate(it) })
            }
            composable(Rutas.ENVIAR) { PantallaEnviar(vm, volver) }
            composable(Rutas.RECORDATORIO) { PantallaRecordatorio(vm, volver) }
            composable(Rutas.AVION) { PantallaAvion(vm, volver) }
            composable(Rutas.HISTORIAL) {
                val avisosFiltrados by vm.avisosFiltrados.collectAsState()
                val filtro by vm.filtro.collectAsState()
                PantallaBase(titulo = "Historial de avisos") {
                    HistorialAvisos(
                        avisos = avisosFiltrados,
                        filtro = filtro,
                        alCambiarFiltro = vm::cambiarFiltro,
                        alBorrar = vm::borrarHistorial
                    )
                }
            }
            composable(Rutas.ACERCA) { PantallaAcerca() }
        }
    }
}

@Composable
private fun BarraInferior(rutaActual: String?, alElegir: (String) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        destinosBarra.forEach { destino ->
            val elegido = destino.ruta == rutaActual
            NavigationBarItem(
                selected = elegido,
                onClick = { alElegir(destino.ruta) },
                icon = {
                    Icon(
                        if (elegido) destino.iconoElegido else destino.icono,
                        contentDescription = null
                    )
                },
                label = { Text(destino.etiqueta) }
            )
        }
    }
}

// Navegación de la barra inferior: no apila la misma pantalla dos veces
// y vuelve a Inicio como base, guardando el estado de cada pestaña
private fun NavHostController.irA(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
