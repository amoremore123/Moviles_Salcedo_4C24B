package com.tecsup.ayudacampus.ui.soporte

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.ui.components.TicketCard
import com.tecsup.ayudacampus.viewmodel.BandejaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaScreen(vm: BandejaViewModel, onTicket: (Int) -> Unit, modifier: Modifier = Modifier) {
    val tickets by vm.tickets.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val estado by vm.filtroEstado.collectAsStateWithLifecycle()
    val prioridad by vm.filtroPrioridad.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = busqueda, onValueChange = vm::buscar,
            label = { Text("Buscar tickets") }, placeholder = { Text("Título, número o estudiante") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("panel_busqueda")
        )
        Row(Modifier.fillMaxWidth().animateContentSize().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BandejaViewModel.FiltroEstado.entries.forEach { opcion ->
                FilterChip(selected = estado == opcion, onClick = { vm.seleccionarEstado(opcion) },
                    shape = RoundedCornerShape(20.dp), colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                    label = { Text(opcion.etiqueta) }, modifier = Modifier.testTag("panel_estado_${opcion.name}"))
            }
        }
        Text("Prioridad", style = MaterialTheme.typography.labelMedium)
        Row(Modifier.fillMaxWidth().animateContentSize().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BandejaViewModel.FiltroPrioridad.entries.forEach { opcion ->
                FilterChip(selected = prioridad == opcion, onClick = { vm.seleccionarPrioridad(opcion) },
                    shape = RoundedCornerShape(20.dp), colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                    label = { Text(opcion.etiqueta) }, modifier = Modifier.testTag("panel_prioridad_${opcion.name}"))
            }
        }
        Text("${tickets.size} ${if (tickets.size == 1) "resultado" else "resultados"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Column(Modifier.padding(16.dp)) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = vm::reintentar) { Text("Reintentar") }
            }
            tickets.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No hay tickets con estos filtros", style = MaterialTheme.typography.titleMedium)
                    Text("Prueba otra búsqueda, estado o prioridad.", style = MaterialTheme.typography.bodySmall)
                }
            }
            else -> LazyColumn(
                Modifier.fillMaxWidth().weight(1f).testTag("panel_bandeja"),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tickets, key = { it.ticket.id }) { detalle ->
                    TicketCard(detalle, onClick = { onTicket(detalle.ticket.id) }, mostrarCliente = true)
                }
            }
        }
    }
}
