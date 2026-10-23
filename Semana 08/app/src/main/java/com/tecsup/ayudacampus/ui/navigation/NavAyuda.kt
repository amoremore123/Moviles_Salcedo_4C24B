package com.tecsup.ayudacampus.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.ayudacampus.ui.cliente.LoginScreen
import com.tecsup.ayudacampus.ui.cliente.MisTicketsScreen
import com.tecsup.ayudacampus.ui.cliente.NuevoTicketScreen
import com.tecsup.ayudacampus.AyudaApp
import com.tecsup.ayudacampus.viewmodel.MisTicketsViewModel
import com.tecsup.ayudacampus.viewmodel.NuevoTicketViewModel
import com.tecsup.ayudacampus.viewmodel.SesionViewModel

@Composable
fun NavAyuda(vm: SesionViewModel, rolSeleccionado: String, onRolSeleccionado: (String) -> Unit) {
    val nav = rememberNavController()
    val usuario by vm.usuarioActual.collectAsStateWithLifecycle()
    val recuperando by vm.recuperandoSesion.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    LaunchedEffect(recuperando, usuario) {
        if (!recuperando) {
            val destino = when (usuario?.rol) {
                "CLIENTE" -> Rutas.MisTickets
                "ADMIN" -> Rutas.Panel
                else -> Rutas.Login
            }
            if (nav.currentDestination?.route != destino) {
                nav.navigate(destino) {
                    popUpTo(nav.graph.id) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }
    NavHost(navController = nav, startDestination = Rutas.Splash) {
        composable(Rutas.Splash) {
            PantallaEspera()
        }
        composable(Rutas.Login) {
            if (recuperando) PantallaEspera()
            else LoginScreen(rolSeleccionado, onRolSeleccionado, cargando, error, vm::limpiarError, vm::login)
        }
        composable(Rutas.MisTickets) { entrada ->
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "CLIENTE" && it.id > 0 }?.let { actual ->
                val app = LocalContext.current.applicationContext as AyudaApp
                val ticketsVm: MisTicketsViewModel = viewModel(
                    key = "misTickets_${actual.id}",
                    factory = MisTicketsViewModel.Factory(app.repository, actual.id)
                )
                val ticketCreado by entrada.savedStateHandle.getStateFlow<Long?>("ticketCreado", null).collectAsStateWithLifecycle()
                LaunchedEffect(ticketCreado) {
                    if (ticketCreado != null) {
                        ticketsVm.buscar("")
                        ticketsVm.seleccionarFiltro(MisTicketsViewModel.Filtro.TODOS)
                    }
                }
                MisTicketsScreen(
                    vm = ticketsVm, nombre = actual.nombre,
                    onTicket = { nav.navigate(Rutas.detalleTicket(it)) },
                    onNuevo = { nav.navigate(Rutas.NuevoTicket) { launchSingleTop = true } },
                    onCerrarSesion = vm::cerrarSesion, cerrandoSesion = cargando,
                    ticketCreadoId = ticketCreado,
                    onTicketMostrado = { entrada.savedStateHandle["ticketCreado"] = null }
                )
            }
        }
        composable(Rutas.NuevoTicket) {
            if (!recuperando) usuario?.takeIf { it.rol == "CLIENTE" && it.id > 0 }?.let { actual ->
                val context = LocalContext.current
                val app = context.applicationContext as AyudaApp
                val nuevoVm: NuevoTicketViewModel = viewModel(
                    key = "nuevoTicket_${actual.id}",
                    factory = NuevoTicketViewModel.Factory(app.repository, vm.usuarioActual)
                )
                NuevoTicketScreen(
                    vm = nuevoVm,
                    onVolver = { nav.popBackStack() },
                    onGuardado = { id ->
                        nav.getBackStackEntry(Rutas.MisTickets).savedStateHandle["ticketCreado"] = id
                        Toast.makeText(context, "Ticket creado correctamente", Toast.LENGTH_SHORT).show()
                        nav.popBackStack(Rutas.MisTickets, inclusive = false)
                    }
                )
            }
        }
        composable(Rutas.DetalleTicket, arguments = listOf(navArgument("ticketId") { type = NavType.IntType })) { entrada ->
            if (!recuperando && usuario?.rol == "CLIENTE") {
                val id = entrada.arguments?.getInt("ticketId") ?: return@composable
                DestinoPendiente("Detalle del ticket #${id.toString().padStart(4, '0')}", "El detalle estará disponible próximamente.") { nav.popBackStack() }
            }
        }
        listOf(Rutas.Panel).forEach { ruta ->
            composable(ruta) {
                if (recuperando) PantallaEspera()
                else usuario?.let { actual ->
                    Column(
                        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp).testTag("destino_$ruta"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                    ) {
                        Text(if (ruta == Rutas.MisTickets) "Mis tickets" else "Panel de soporte", style = MaterialTheme.typography.headlineMedium)
                        Text("Hola, ${actual.nombre}", style = MaterialTheme.typography.titleLarge)
                        Text(if (actual.rol == "CLIENTE") "Perfil: Estudiante" else "Perfil: Soporte TI")
                        Text(actual.correo)
                        Text("Destino temporal · Paso 6", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = vm::cerrarSesion, enabled = !cargando, modifier = Modifier.testTag("cerrarSesion")) {
                            Text("Cerrar sesión")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinoPendiente(titulo: String, mensaje: String, onVolver: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Text(mensaje)
        OutlinedButton(onClick = onVolver) { Text("Volver a mis tickets") }
    }
}

@Composable
private fun PantallaEspera() {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("AyudaCampus", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Recuperando tu sesión…", style = MaterialTheme.typography.bodyMedium)
    }
}
