package com.tecsup.ayudacampus.ui.soporte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.ui.components.*
import com.tecsup.ayudacampus.ui.components.graficos.GraficoBarras
import com.tecsup.ayudacampus.ui.components.graficos.GraficoDona
import com.tecsup.ayudacampus.viewmodel.PanelViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PanelScreen(vm: PanelViewModel, onTicket: (Int) -> Unit, onCerrarSesion: () -> Unit,
                cerrandoSesion: Boolean = false, errorSesion: String? = null, onCategorias: () -> Unit, onPerfil: () -> Unit, seccionSolicitada: Int? = null, onSeccionMostrada: () -> Unit = {}) {
    val administrador by vm.administrador.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val datos by vm.indicadores.collectAsStateWithLifecycle()
    val cargandoDatos by vm.cargandoIndicadores.collectAsStateWithLifecycle()
    val errorDatos by vm.errorIndicadores.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(seccionSolicitada) {
        seccionSolicitada?.let { pestana = it; onSeccionMostrada() }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().semantics { testTagsAsResourceId = true },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                AyudaTopAppBar(if (pestana == 0) "Panel de soporte" else "Bandeja de tickets") {
                    IconButton(onClick = onPerfil, enabled = !cerrandoSesion) { Icon(Icons.Default.Person, "Perfil y ajustes") }
                    IconButton(onClick = onCerrarSesion, enabled = !cerrandoSesion) { Icon(Icons.Default.ExitToApp, "Cerrar sesión") }
                }
                administrador?.let {
                    Text("Hola, ${it.nombre}", Modifier.padding(start = 20.dp, top = 10.dp), style = MaterialTheme.typography.titleMedium)
                    Text("Perfil: Soporte TI", Modifier.padding(start = 20.dp, bottom = 8.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            }
        },
        bottomBar = {
            if (administrador != null) AyudaNavigationBar(true, pestana, !cerrandoSesion) { indice ->
                when (indice) { 0 -> pestana = 0; 1 -> pestana = 1; 2 -> onCategorias(); 3 -> onPerfil() }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            errorSesion?.let { Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) }
            when {
                administrador == null && cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                administrador == null -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(error ?: "Acceso exclusivo para Soporte TI", color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
                }
                pestana == 1 -> BandejaScreen(vm, onTicket)
                else -> LazyColumn(Modifier.fillMaxSize().testTag("panel_resumen"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (cargandoDatos) item { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Cargando indicadores…") }
                    else if (errorDatos != null) item {
                        Text(errorDatos!!, color = MaterialTheme.colorScheme.error)
                        OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
                    } else {
                        item {
                            TarjetaBienvenida("Tu mesa de ayuda, al día", "Supervisa incidencias y prioriza lo importante.")
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    TarjetaIndicador("Abiertos", datos.abiertos, Color(0xFFD97706), Modifier.weight(1f))
                                    TarjetaIndicador("En proceso", datos.enProceso, Color(0xFF2563EB), Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    TarjetaIndicador("Resueltos", datos.resueltos, Color(0xFF16A34A), Modifier.weight(1f))
                                    TarjetaIndicador("Cerrados", datos.cerrados, Color(0xFF6B7280), Modifier.weight(1f))
                                }
                            }
                        }
                        if (datos.altasSinAtender > 0) item {
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(14.dp)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("${datos.altasSinAtender} ${if (datos.altasSinAtender == 1) "ticket" else "tickets"} ALTA sin atender", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                                        Text("Más de una hora en estado abierto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                    TextButton(onClick = { vm.verAltasAbiertas(); pestana = 1 }) { Text("Ver") }
                                }
                            }
                        }
                        item {
                            TarjetaGrafico("Tickets por estado") { GraficoDona(datos.porEstado) }
                        }
                        item {
                            TarjetaGrafico("Tickets por categoría") { GraficoBarras(datos.porCategoria) }
                        }
                        item {
                            OutlinedButton(onClick = { pestana = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Ver bandeja de tickets") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaGrafico(titulo: String, contenido: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            contenido()
        }
    }
}
