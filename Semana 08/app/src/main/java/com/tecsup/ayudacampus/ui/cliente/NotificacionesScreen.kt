package com.tecsup.ayudacampus.ui.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.data.local.entity.Notificacion
import com.tecsup.ayudacampus.viewmodel.NotificacionesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    vm: NotificacionesViewModel,
    onVolver: () -> Unit
) {
    val notificaciones by vm.notificaciones.collectAsStateWithLifecycle()
    val noLeidas by vm.cantidadNoLeidas.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Notificaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                if (noLeidas > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            " $noLeidas",
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (noLeidas > 0) {
                FloatingActionButton(
                    onClick = vm::marcarTodoComoLeido,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = "Marcar todo como leido")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                cargando -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            error!!,
                            Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                notificaciones.isEmpty() -> {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.height(300.dp)
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay notificaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Cuando recibas nuevas actualizaciones, apareceran aqui",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(notificaciones, key = { it.id }) { notif ->
                            NotificacionItem(
                                notificacion = notif,
                                onClick = { vm.marcarComoLeida(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificacionItem(
    notificacion: Notificacion,
    onClick: () -> Unit
) {
    val colorTipo = when (notificacion.tipo) {
        "NUEVO_TICKET" -> MaterialTheme.colorScheme.primary
        "COMENTARIO" -> MaterialTheme.colorScheme.secondary
        "TICKET_ACTUALIZADO" -> MaterialTheme.colorScheme.tertiary
        "RESPUESTA_ADMIN" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val fecha = SimpleDateFormat("dd MMM · HH:mm", Locale("es", "PE")).format(Date(notificacion.fecha))

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                Modifier.size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorTipo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (notificacion.leida) Icons.Default.NotificationsNone
                    else Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colorTipo
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        notificacion.origenNombre,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notificacion.leida) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    notificacion.tipo,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorTipo
                )
            }
            if (!notificacion.leida) {
                Box(
                    Modifier.size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
