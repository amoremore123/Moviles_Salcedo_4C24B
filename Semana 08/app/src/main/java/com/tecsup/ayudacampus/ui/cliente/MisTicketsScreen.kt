package com.tecsup.ayudacampus.ui.cliente

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.ui.components.*
import com.tecsup.ayudacampus.viewmodel.MisTicketsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTicketsScreen(
    vm: MisTicketsViewModel,
    nombre: String,
    onTicket: (Int) -> Unit,
    onNuevo: () -> Unit,
    onCerrarSesion: () -> Unit,
    cerrandoSesion: Boolean = false,
    ticketCreadoId: Long? = null,
    onTicketMostrado: () -> Unit = {},
    onPerfil: () -> Unit = {},
    onNotificaciones: () -> Unit = {},
    mesaAbierta: Boolean? = true
) {
    val tickets by vm.tickets.collectAsStateWithLifecycle()
    val filtro by vm.filtro.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val abiertos by vm.cantidadAbiertos.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val listaState = rememberLazyListState()
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(filtro, busqueda) { listaState.scrollToItem(0) }
    LaunchedEffect(ticketCreadoId, tickets) {
        if (ticketCreadoId != null && tickets.any { it.ticket.id.toLong() == ticketCreadoId }) {
            listaState.scrollToItem(0)
            avisos.showSnackbar("Ticket creado correctamente")
            onTicketMostrado()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().testTag("destino_MisTickets"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                AyudaTopAppBar("Mis tickets") {
                    IconButton(onClick = onNotificaciones, enabled = !cerrandoSesion) { Icon(Icons.Default.Notifications, "Notificaciones") }
                    IconButton(onClick = onPerfil, enabled = !cerrandoSesion) { Icon(Icons.Default.Person, "Mi perfil") }
                    IconButton(onClick = onCerrarSesion, enabled = !cerrandoSesion) { Icon(Icons.Default.ExitToApp, "Cerrar sesión") }
                }
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Perfil: Estudiante", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Hola, $nombre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Estamos para ayudarte", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("$abiertos abiertos", Modifier.testTag("contadorAbiertos"), style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                if (mesaAbierta == false) Text("Mesa de ayuda cerrada", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = busqueda, onValueChange = vm::buscar,
                    modifier = Modifier.fillMaxWidth().testTag("buscarTickets"),
                    placeholder = { Text("Buscar por título o número…", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true, shape = RoundedCornerShape(14.dp)
                )
                Row(Modifier.fillMaxWidth().animateContentSize().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MisTicketsViewModel.Filtro.entries.forEach { opcion ->
                        FilterChip(
                            selected = filtro == opcion, onClick = { vm.seleccionarFiltro(opcion) },
                            label = { Text(opcion.etiqueta) }, modifier = Modifier.testTag("filtro_${opcion.name}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                }
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
        bottomBar = { BarraInferior(!cerrandoSesion) },
        floatingActionButton = {
            if (mesaAbierta == true) FloatingActionButton(onClick = onNuevo, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.testTag("nuevoTicket")) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo ticket")
            }
        }
    ) { padding ->
        when {
            cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            tickets.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EstadoVacio("No tienes tickets aquí", "Prueba con otro filtro o búsqueda.", "sinTickets")
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding).testTag("listaTickets"),
                state = listaState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tickets, key = { it.ticket.id }) { detalle ->
                    TicketCard(detalle, onClick = { onTicket(detalle.ticket.id) })
                }
            }
        }
    }
}
