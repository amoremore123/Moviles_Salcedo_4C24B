package com.tecsup.smartcatalog.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.smartcatalog.AppContainer
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.ui.admin.PanelAdminScreen
import com.tecsup.smartcatalog.ui.admin.PanelAdminViewModel
import com.tecsup.smartcatalog.ui.auth.*
import com.tecsup.smartcatalog.ui.detalle.DetalleScreen
import com.tecsup.smartcatalog.ui.detalle.DetalleViewModel
import com.tecsup.smartcatalog.ui.favoritos.FavoritosScreen
import com.tecsup.smartcatalog.ui.favoritos.FavoritosViewModel
import com.tecsup.smartcatalog.ui.lista.ListaScreen
import com.tecsup.smartcatalog.ui.lista.ListaViewModel

private const val LISTA = "lista"
private const val DETALLE = "detalle/{id}"
private const val FAVORITOS = "favoritos"
private const val ADMIN = "admin"

@Composable
fun AppNav(
    container: AppContainer,
    obtenerSesionViewModel: () -> SesionViewModel,
    registroViewModel: RegistroViewModel,
    listaViewModel: () -> ListaViewModel,
    modifier: Modifier = Modifier
) {
    val estadoRegistro by registroViewModel.estado.collectAsStateWithLifecycle()
    var registro by rememberSaveable { mutableStateOf(false) }
    var confirmacionVista by rememberSaveable { mutableStateOf(false) }
    // Resolver el registro primero: la recuperación normal exige que el perfil ya exista.
    if (registro || estadoRegistro.pendiente || (estadoRegistro.completo && !confirmacionVista)) {
        RegistroScreen(registroViewModel, onVolver = {
            registro = false
            if (estadoRegistro.completo) confirmacionVista = true
        })
        return
    }
    val sesionViewModel = obtenerSesionViewModel()
    val estado by sesionViewModel.estado.collectAsStateWithLifecycle()
    when (val actual = estado) {
        EstadoSesion.Verificando -> VerificandoSesion(modifier)
        is EstadoSesion.Autenticado -> key(actual.sesion.uid, actual.sesion.rol) {
            NavegacionAutenticada(container, actual.sesion, { (sesionViewModel.estado.value as? EstadoSesion.Autenticado)?.sesion }, listaViewModel, sesionViewModel::cerrarSesion, modifier)
        }
        else -> LoginScreen(actual, sesionViewModel::iniciarSesion, modifier, onRegistro = { registro = true })
    }
}

/** El NavController solo existe mientras hay una sesión validada; logout elimina todo su backstack. */
@Composable
private fun NavegacionAutenticada(
    container: AppContainer,
    sesion: Sesion,
    sesionActual: () -> Sesion?,
    listaViewModel: () -> ListaViewModel,
    onCerrarSesion: () -> Unit,
    modifier: Modifier
) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    fun navegar(destino: DestinoProtegido, ruta: String) {
        val actual = navController.currentBackStackEntry
        if (AccesoRutas.permite(sesionActual(), destino) && actual?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navController.navigate(ruta) { launchSingleTop = true }
        }
    }
    Column(modifier.fillMaxSize()) {
        NavHost(navController, startDestination = if (AccesoRutas.inicial(sesion) == DestinoProtegido.ADMIN) ADMIN else LISTA, modifier = Modifier.weight(1f)) {
            composable(ADMIN) { entry ->
                DestinoConPermiso(sesionActual(), DestinoProtegido.ADMIN) {
                    val panel: PanelAdminViewModel = viewModel(viewModelStoreOwner = entry, factory = container.panelAdminFactory)
                    PanelAdminScreen(
                        panel = panel,
                        notificaciones = container.notificacionesCatalogo,
                        sesionActual = sesionActual,
                        onCatalogo = { navegar(DestinoProtegido.CATALOGO, LISTA) },
                        onCerrarSesion = onCerrarSesion
                    )
                }
            }
            composable(LISTA) {
                DestinoConPermiso(sesionActual(), DestinoProtegido.CATALOGO) {
                    ListaScreen(
                        viewModel = listaViewModel(), imageLoader = container.imageLoader,
                        onFavoritos = { if (navController.currentBackStackEntry?.destination?.route == LISTA) navegar(DestinoProtegido.FAVORITOS, FAVORITOS) },
                        onItemClick = { id -> if (navController.currentBackStackEntry?.destination?.route == LISTA) navegar(DestinoProtegido.DETALLE, "detalle/$id") }
                    )
                }
            }
            composable(FAVORITOS) { entry ->
                DestinoConPermiso(sesionActual(), DestinoProtegido.FAVORITOS) {
                    val favoritosViewModel: FavoritosViewModel = viewModel(viewModelStoreOwner = entry, factory = container.favoritosViewModelFactory)
                    FavoritosScreen(
                        viewModel = favoritosViewModel, imageLoader = container.imageLoader,
                        onVolver = {
                            val actual = navController.currentBackStackEntry
                            if (actual?.destination?.route == FAVORITOS && actual.lifecycle.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                        },
                        onItemClick = { id -> if (navController.currentBackStackEntry?.destination?.route == FAVORITOS) navegar(DestinoProtegido.DETALLE, "detalle/$id") }
                    )
                }
            }
            composable(DETALLE, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                DestinoConPermiso(sesionActual(), DestinoProtegido.DETALLE) {
                    val id = entry.arguments?.getInt("id") ?: 0
                    val factory = remember(id, container) { container.detalleViewModelFactory(id) }
                    val detalleViewModel: DetalleViewModel = viewModel(viewModelStoreOwner = entry, key = "detalle-$id", factory = factory)
                    DetalleScreen(
                        viewModel = detalleViewModel, imageLoader = container.imageLoader,
                        descripcionVolver = if (navController.previousBackStackEntry?.destination?.route == FAVORITOS) "Volver a favoritos" else "Volver al catálogo",
                        onVolver = {
                            val actual = navController.currentBackStackEntry
                            if (actual?.destination?.route == DETALLE && actual.lifecycle.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                        }
                    )
                }
            }
        }
        if (entradaActual?.destination?.route != ADMIN) Surface(tonalElevation = 3.dp) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (sesion.rol == Rol.ADMIN) "Administrador" else "Usuario", Modifier.weight(1f).padding(top = 14.dp), style = MaterialTheme.typography.labelLarge)
                    if (AccesoRutas.permite(sesionActual(), DestinoProtegido.ADMIN)) {
                        TextButton(onClick = {
                            if (AccesoRutas.permite(sesionActual(), DestinoProtegido.ADMIN) &&
                                navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
                            ) navController.popBackStack(ADMIN, inclusive = false)
                        }) { Text("Administración") }
                    }
                    TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                }
                Text("Favoritos locales compartidos entre cuentas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Segunda comprobación: una ruta invocada directamente tampoco puede componer contenido administrativo. */
@Composable
fun DestinoConPermiso(sesion: Sesion?, destino: DestinoProtegido, contenido: @Composable () -> Unit) {
    if (AccesoRutas.permite(sesion, destino)) contenido()
    else Surface(Modifier.fillMaxSize()) { Text("Acceso no autorizado", Modifier.safeDrawingPadding().padding(24.dp)) }
}
