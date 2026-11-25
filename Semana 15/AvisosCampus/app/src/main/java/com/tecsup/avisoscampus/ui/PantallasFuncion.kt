package com.tecsup.avisoscampus.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tecsup.avisoscampus.R
import com.tecsup.avisoscampus.model.IconoAviso
import com.tecsup.avisoscampus.receiver.ModoAvionReceiver
import com.tecsup.avisoscampus.ui.theme.MoradoOscuro
import com.tecsup.avisoscampus.ui.theme.TarjetaLila
import com.tecsup.avisoscampus.ui.theme.TarjetaRosa
import com.tecsup.avisoscampus.ui.theme.TarjetaVerde
import com.tecsup.avisoscampus.viewmodel.AvisosViewModel

// ---------------------------------------------------------------- Enviar aviso

@Composable
fun PantallaEnviar(vm: AvisosViewModel, alVolver: () -> Unit) {
    val mensajeAviso by vm.mensajeAviso.collectAsState()
    val iconoAviso by vm.iconoAviso.collectAsState()

    PantallaIlustrada(titulo = "Enviar aviso", alVolver = alVolver) {
        Ilustracion(R.drawable.ilus_enviar)
        Titulos(
            titulo = "Envía un aviso al instante",
            subtitulo = "Recibe una notificación en tu celular con un mensaje personalizado."
        )
        Column {
            OutlinedTextField(
                value = mensajeAviso,
                onValueChange = { vm.cambiarMensajeAviso(it) },
                placeholder = { Text("Escribe tu mensaje") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${mensajeAviso.length}/100",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 4.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 20.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text("Personalizar ícono", fontWeight = FontWeight.SemiBold)
        }
        SelectorIcono(elegido = iconoAviso, alElegir = { vm.cambiarIcono(it) })
        Button(
            onClick = { vm.avisarAhora() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Enviar aviso", style = MaterialTheme.typography.titleMedium)
        }
        MensajeSinPermiso(vm)
    }
}

// Los 5 íconos como círculos; el elegido lleva fondo lila y borde morado
@Composable
private fun SelectorIcono(elegido: IconoAviso, alElegir: (IconoAviso) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconoAviso.entries.forEach { icono ->
            val seleccionado = icono == elegido
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (seleccionado) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = if (seleccionado) 3.dp else 1.dp,
                        color = if (seleccionado) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { alElegir(icono) }
            ) {
                Image(
                    painter = painterResource(icono.recurso()),
                    contentDescription = icono.etiqueta,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------- Recordatorio

@Composable
fun PantallaRecordatorio(vm: AvisosViewModel, alVolver: () -> Unit) {
    val faltan by vm.faltan.collectAsState()

    PantallaIlustrada(titulo = "Recordatorio", alVolver = alVolver) {
        Ilustracion(R.drawable.ilus_recordatorio)
        Titulos(titulo = "Programa un recordatorio", subtitulo = "Recibe un aviso en 10 segundos")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TarjetaRosa, contentColor = MoradoOscuro)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painterResource(R.drawable.ic_bombilla),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
                Text("Ideal para recordatorios rápidos como: una tarea o una reunión.")
            }
        }

        // Píldora con el tiempo: 10 s en reposo, la cuenta regresiva mientras corre
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        "${faltan ?: 10} s",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Button(
            onClick = { vm.recordarEn(10) },
            enabled = faltan == null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Programar", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(
            onClick = { vm.cancelarRecordatorio() },
            enabled = faltan != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Cancelar")
        }
        MensajeSinPermiso(vm)
    }
}

// ---------------------------------------------------------------- Modo avión

@Composable
fun PantallaAvion(vm: AvisosViewModel, alVolver: () -> Unit) {
    val context = LocalContext.current
    val escuchando by vm.escuchandoAvion.collectAsState()

    // Estado real del modo avión: se lee aquí (la pantalla sí tiene Context)
    var avionActivo by remember { mutableStateOf(leerModoAvion(context)) }

    // Receptor solo para refrescar lo que muestra esta pantalla (no crea avisos).
    // Se quita en onDispose al salir de la pantalla.
    DisposableEffect(Unit) {
        val receptor = ModoAvionReceiver { activo -> avionActivo = activo }
        ContextCompat.registerReceiver(
            context,
            receptor,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { context.unregisterReceiver(receptor) }
    }

    val textoEstado = if (avionActivo) "Activado" else "Desactivado"

    PantallaIlustrada(titulo = "Modo avión", alVolver = alVolver) {
        // Ilustración con la etiqueta del estado encima
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF4FF))
        ) {
            Box(Modifier.fillMaxWidth()) {
                Image(
                    painterResource(R.drawable.ilus_avion),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .padding(12.dp)
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Flight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("Modo avión", style = MaterialTheme.typography.labelMedium)
                            Text(textoEstado, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Text(
            "Recibe avisos cuando cambie el estado del modo avión",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Escuchar el modo avión",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = escuchando, onCheckedChange = { vm.cambiarEscucha(it) })
            }
        }

        TarjetaInfo(
            color = TarjetaVerde,
            icono = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E9E5B),
                    modifier = Modifier.size(36.dp)
                )
            }
        ) {
            Text("Estado actual:", style = MaterialTheme.typography.bodyMedium)
            Text("Modo avión ${textoEstado.lowercase()}", fontWeight = FontWeight.Bold)
        }

        TarjetaInfo(
            color = TarjetaLila,
            icono = {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        ) {
            Text(
                when {
                    !escuchando -> "Enciende \"Escuchar el modo avión\" para recibir avisos."
                    avionActivo -> "Te notificaremos cuando se desactive el modo avión."
                    else -> "Te notificaremos cuando se active el modo avión."
                }
            )
        }
        MensajeSinPermiso(vm)
    }
}

private fun leerModoAvion(context: Context): Boolean =
    Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

// ---------------------------------------------------------------- Piezas comunes

// Fondo lila con formas redondeadas suaves detrás del contenido
@Composable
private fun PantallaIlustrada(
    titulo: String,
    alVolver: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = MaterialTheme.colorScheme.primaryContainer
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(forma.copy(alpha = 0.7f), size.width * 0.42f, Offset(size.width * 1.02f, size.height * 0.06f))
            drawCircle(forma.copy(alpha = 0.45f), size.width * 0.32f, Offset(-size.width * 0.06f, size.height * 0.48f))
            drawCircle(forma.copy(alpha = 0.55f), size.width * 0.36f, Offset(size.width * 0.98f, size.height * 0.98f))
        }
        PantallaBase(titulo = titulo, alVolver = alVolver, contenido = contenido)
    }
}

// Ilustración grande y centrada sobre un círculo lila suave
@Composable
private fun Ilustracion(@DrawableRes recurso: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(190.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), CircleShape)
        )
        Image(
            painterResource(recurso),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
private fun Titulos(titulo: String, subtitulo: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

// Tarjeta de color con ícono a la izquierda y texto a la derecha
@Composable
private fun TarjetaInfo(
    color: Color,
    icono: @Composable () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = MoradoOscuro)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            icono()
            Column(content = contenido)
        }
    }
}
