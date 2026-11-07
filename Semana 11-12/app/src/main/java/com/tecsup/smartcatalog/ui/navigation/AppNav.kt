package com.tecsup.smartcatalog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.smartcatalog.AppContainer
import com.tecsup.smartcatalog.ui.detalle.DetalleScreen
import com.tecsup.smartcatalog.ui.detalle.DetalleViewModel
import com.tecsup.smartcatalog.ui.lista.ListaScreen
import com.tecsup.smartcatalog.ui.lista.ListaViewModel

private const val LISTA = "lista"
private const val DETALLE = "detalle/{id}"

@Composable
fun AppNav(container: AppContainer, listaViewModel: ListaViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LISTA, modifier = modifier) {
        composable(LISTA) {
            ListaScreen(
                viewModel = listaViewModel,
                imageLoader = container.imageLoader,
                onItemClick = { id ->
                    val actual = navController.currentBackStackEntry
                    if (actual?.destination?.route == LISTA && actual?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate("detalle/$id") { launchSingleTop = true }
                    }
                }
            )
        }
        composable(DETALLE, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            val factory = remember(id, container) { container.detalleViewModelFactory(id) }
            val detalleViewModel: DetalleViewModel = viewModel(
                viewModelStoreOwner = entry,
                key = "detalle-$id",
                factory = factory
            )
            DetalleScreen(
                viewModel = detalleViewModel,
                imageLoader = container.imageLoader,
                onVolver = {
                    val actual = navController.currentBackStackEntry
                    if (actual?.destination?.route == DETALLE && actual?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
