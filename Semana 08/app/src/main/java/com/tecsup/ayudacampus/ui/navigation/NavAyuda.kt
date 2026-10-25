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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tecsup.ayudacampus.ui.components.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tecsup.ayudacampus.ui.soporte.AtenderTicketScreen
import com.tecsup.ayudacampus.ui.soporte.CategoriasScreen
import com.tecsup.ayudacampus.ui.soporte.FormularioCategoriaScreen
import com.tecsup.ayudacampus.viewmodel.AtenderViewModel
import com.tecsup.ayudacampus.viewmodel.CategoriasViewModel
import com.tecsup.ayudacampus.ui.soporte.PanelScreen
import com.tecsup.ayudacampus.viewmodel.PanelViewModel
import com.tecsup.ayudacampus.ui.cliente.LoginScreen
import com.tecsup.ayudacampus.ui.cliente.MisTicketsScreen
import com.tecsup.ayudacampus.ui.cliente.DetalleTicketScreen
import com.tecsup.ayudacampus.ui.cliente.NuevoTicketScreen
import com.tecsup.ayudacampus.AyudaApp
import com.tecsup.ayudacampus.viewmodel.MisTicketsViewModel
import com.tecsup.ayudacampus.viewmodel.DetalleTicketViewModel
import com.tecsup.ayudacampus.viewmodel.NuevoTicketViewModel
import com.tecsup.ayudacampus.ui.cliente.PerfilScreen
import com.tecsup.ayudacampus.ui.soporte.AjustesScreen
import com.tecsup.ayudacampus.viewmodel.PerfilViewModel
import com.tecsup.ayudacampus.viewmodel.SesionViewModel
import com.tecsup.ayudacampus.viewmodel.RegistroViewModel
import com.tecsup.ayudacampus.viewmodel.NotificacionesViewModel
import com.tecsup.ayudacampus.ui.cliente.RegistroScreen
import com.tecsup.ayudacampus.ui.cliente.NotificacionesScreen

@Composable
fun NavAyuda(vm: SesionViewModel, rolSeleccionado: String, onRolSeleccionado: (String) -> Unit, nav: NavHostController = rememberNavController()) {
    var confirmarSalida by remember { mutableStateOf(false) }
    var confirmarCambio by remember { mutableStateOf(false) }
    val usuario by vm.usuarioActual.collectAsStateWithLifecycle()
    val recuperando by vm.recuperandoSesion.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val mesaAbierta by vm.recibirTickets.collectAsStateWithLifecycle()
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
    val entradaActual by nav.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    CompositionLocalProvider(LocalBarraInferior provides { habilitado ->
        if (usuario?.rol == "CLIENTE") {
            val seleccionado = when (rutaActual) { Rutas.NuevoTicket -> 1; Rutas.Perfil -> 2; else -> 0 }
            AyudaNavigationBar(false, seleccionado, habilitado) { indice ->
                val destino = listOf(Rutas.MisTickets, Rutas.NuevoTicket, Rutas.Perfil)[indice]
                if (destino != rutaActual) nav.navigate(destino) {
                    popUpTo(Rutas.MisTickets) { inclusive = false }
                    launchSingleTop = true
                }
            }
        } else if (usuario?.rol == "ADMIN") {
            val seleccionado = when (rutaActual) { Rutas.Categorias, Rutas.FormularioCategoria -> 2; Rutas.Ajustes -> 3; Rutas.AtenderTicket -> 1; else -> 0 }
            AyudaNavigationBar(true, seleccionado, habilitado) { indice ->
                if (indice < 2) {
                    nav.getBackStackEntry(Rutas.Panel).savedStateHandle["seccionSoporte"] = indice
                    nav.popBackStack(Rutas.Panel, false)
                } else {
                    val destino = if (indice == 2) Rutas.Categorias else Rutas.Ajustes
                    if (rutaActual != destino) nav.navigate(destino) { popUpTo(Rutas.Panel); launchSingleTop = true }
                }
            }
        }
    }) {
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
                    onCerrarSesion = { confirmarSalida = true }, cerrandoSesion = cargando,
                    ticketCreadoId = ticketCreado,
                    onTicketMostrado = { entrada.savedStateHandle["ticketCreado"] = null },
                    onPerfil = { nav.navigate(Rutas.Perfil) { launchSingleTop = true } },
                    onNotificaciones = { nav.navigate(Rutas.Notificaciones) { launchSingleTop = true } },
                    mesaAbierta = mesaAbierta
                )
            }
        }
        composable(Rutas.NuevoTicket) {
            if (!recuperando) usuario?.takeIf { it.rol == "CLIENTE" && it.id > 0 }?.let { actual ->
                val context = LocalContext.current
                val app = context.applicationContext as AyudaApp
                val nuevoVm: NuevoTicketViewModel = viewModel(
                    key = "nuevoTicket_${actual.id}",
                    factory = NuevoTicketViewModel.Factory(app.repository, vm.usuarioActual, app.preferencias.recibirTickets)
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
        composable(Rutas.DetalleTicket, arguments = listOf(navArgument(Rutas.TicketId) { type = NavType.IntType })) { entrada ->
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "CLIENTE" && it.id > 0 }?.let { actual ->
                val id = entrada.arguments?.getInt(Rutas.TicketId) ?: return@composable
                val app = LocalContext.current.applicationContext as AyudaApp
                val detalleVm: DetalleTicketViewModel = viewModel(
                    key = "detalle_${actual.id}_$id",
                    factory = DetalleTicketViewModel.Factory(id, app.repository, vm.usuarioActual)
                )
                DetalleTicketScreen(detalleVm, onVolver = { nav.popBackStack() })
            }
        }
        composable(Rutas.Panel) { entrada ->
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "ADMIN" && it.id > 0 }?.let { actual ->
                val app = LocalContext.current.applicationContext as AyudaApp
                val panelVm: PanelViewModel = viewModel(
                    key = "panel_${actual.id}",
                    factory = PanelViewModel.Factory(app.repository, vm.usuarioActual)
                )
                val seccionSolicitada by entrada.savedStateHandle.getStateFlow<Int?>("seccionSoporte", null).collectAsStateWithLifecycle()
                PanelScreen(panelVm, seccionSolicitada = seccionSolicitada,
                    onSeccionMostrada = { entrada.savedStateHandle["seccionSoporte"] = null },
                    onTicket = { nav.navigate(Rutas.atenderTicket(it)) { launchSingleTop = true } },
                    onCerrarSesion = { confirmarSalida = true }, cerrandoSesion = cargando, errorSesion = error,
                    onCategorias = { nav.navigate(Rutas.Categorias) { launchSingleTop = true } },
                    onPerfil = { nav.navigate(Rutas.Ajustes) { launchSingleTop = true } })
            }
        }
        composable(Rutas.AtenderTicket, arguments = listOf(navArgument(Rutas.TicketId) { type = NavType.IntType })) { entrada ->
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "ADMIN" && it.id > 0 }?.let { actual ->
                val id = entrada.arguments?.getInt(Rutas.TicketId) ?: return@composable
                val app = LocalContext.current.applicationContext as AyudaApp
                val atenderVm: AtenderViewModel = viewModel(key = "atender_${actual.id}_$id",
                    factory = AtenderViewModel.Factory(id, app.repository, vm.usuarioActual))
                AtenderTicketScreen(atenderVm, onVolver = { nav.popBackStack() })
            }
        }
        composable(Rutas.Categorias) {
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "ADMIN" && it.id > 0 }?.let { actual ->
                val app = LocalContext.current.applicationContext as AyudaApp
                val categoriasVm: CategoriasViewModel = viewModel(key = "categorias_${actual.id}",
                    factory = CategoriasViewModel.Factory(app.repository, vm.usuarioActual))
                CategoriasScreen(categoriasVm, onVolver = { nav.popBackStack() },
                    onNueva = { nav.navigate(Rutas.formularioCategoria()) { launchSingleTop = true } },
                    onEditar = { id -> nav.navigate(Rutas.formularioCategoria(id)) { launchSingleTop = true } })
            }
        }
        composable(Rutas.FormularioCategoria, arguments = listOf(navArgument(Rutas.CategoriaId) { type = NavType.IntType; defaultValue = 0 })) { entrada ->
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "ADMIN" && it.id > 0 }?.let { actual ->
                val id = entrada.arguments?.getInt(Rutas.CategoriaId)?.takeUnless { it == 0 }
                val context = LocalContext.current
                val app = context.applicationContext as AyudaApp
                val formularioVm: CategoriasViewModel = viewModel(key = "formulario_${actual.id}_$id",
                    factory = CategoriasViewModel.Factory(app.repository, vm.usuarioActual, id))
                FormularioCategoriaScreen(formularioVm, onVolver = { nav.popBackStack() }, onGuardado = {
                    Toast.makeText(context, "Categoría guardada", Toast.LENGTH_SHORT).show()
                    if (!nav.popBackStack(Rutas.Categorias, false)) {
                        nav.navigate(Rutas.Categorias) { popUpTo(Rutas.FormularioCategoria) { inclusive = true }; launchSingleTop = true }
                    }
                })
            }
        }
        composable(Rutas.Perfil) {
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "CLIENTE" && it.id > 0 }?.let { actual ->
                val app = LocalContext.current.applicationContext as AyudaApp
                val perfilVm: PerfilViewModel = viewModel(key = "perfil_${actual.id}",
                    factory = PerfilViewModel.Factory(app.repository, app.preferencias, vm.usuarioActual))
                PerfilScreen(perfilVm, { nav.popBackStack() }, { confirmarSalida = true },
                    { confirmarCambio = true }, cargando, error)
            }
        }
        composable(Rutas.Registro) {
            if (recuperando) PantallaEspera()
            else {
                val app = LocalContext.current.applicationContext as AyudaApp
                val registroVm: RegistroViewModel = viewModel(
                    factory = RegistroViewModel.Factory(app.repository)
                )
                RegistroScreen(
                    vm = registroVm,
                    onVolver = { nav.popBackStack(Rutas.Login, false) },
                    onRegistroExitoso = { nav.popBackStack(Rutas.Login, false) }
                )
            }
        }
        composable(Rutas.Notificaciones) {
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "CLIENTE" && it.id > 0 }?.let { actual ->
                val app = LocalContext.current.applicationContext as AyudaApp
                val notifVm: NotificacionesViewModel = viewModel(
                    key = "notif_${actual.id}",
                    factory = NotificacionesViewModel.Factory(app.repository, actual.id)
                )
                NotificacionesScreen(notifVm, onVolver = { nav.popBackStack() })
            }
        }
        composable(Rutas.Ajustes) {
            if (recuperando) PantallaEspera()
            else usuario?.takeIf { it.rol == "ADMIN" && it.id > 0 }?.let { actual ->
                val app = LocalContext.current.applicationContext as AyudaApp
                val ajustesVm: PerfilViewModel = viewModel(key = "ajustes_${actual.id}",
                    factory = PerfilViewModel.Factory(app.repository, app.preferencias, vm.usuarioActual))
                AjustesScreen(ajustesVm, { nav.popBackStack() }, { confirmarSalida = true },
                    { confirmarCambio = true }, cargando, error)
            }
        }
    }
    }
    if (confirmarSalida && usuario != null) AlertDialog(
        onDismissRequest = { confirmarSalida = false }, title = { Text("¿Cerrar sesión?") },
        text = { Text("Tus preferencias y tickets se conservarán.") },
        confirmButton = { TextButton(onClick = { confirmarSalida = false; vm.cerrarSesion() }) { Text("Cerrar sesión") } },
        dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } })
    if (confirmarCambio && usuario != null) AlertDialog(
        onDismissRequest = { confirmarCambio = false }, title = { Text("Cambiar de perfil de práctica") },
        text = { Text("Entrarás con el usuario de demostración del otro perfil. Se cerrará la navegación del perfil actual.") },
        confirmButton = { TextButton(onClick = { confirmarCambio = false; vm.cambiarPerfil() }) { Text("Cambiar perfil") } },
        dismissButton = { TextButton(onClick = { confirmarCambio = false }) { Text("Cancelar") } })
}

@Composable
private fun PantallaEspera() {
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 420.dp).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            TarjetaBienvenida("AyudaCampus", "Tu campus, siempre conectado.")
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconoCategoria("pc", "Equipos")
                IconoCategoria("wifi", "Conexión")
                IconoCategoria("printer", "Impresión")
            }
            CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
            Text("Recuperando tu sesión…", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
