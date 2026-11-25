package com.tecsup.avisoscampus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tecsup.avisoscampus.model.IconoAviso
import com.tecsup.avisoscampus.viewmodel.AvisosViewModel

// Pantallas de función (con flecha de volver). Por ahora reutilizan las tarjetas
// que ya existían; su diseño final llega en los siguientes commits.

@Composable
fun PantallaEnviar(vm: AvisosViewModel, alVolver: () -> Unit) {
    val mensajeAviso by vm.mensajeAviso.collectAsState()
    val iconoAviso by vm.iconoAviso.collectAsState()

    PantallaBase(titulo = "Enviar aviso", alVolver = alVolver) {
        Tarjeta("Avisos") {
            OutlinedTextField(
                value = mensajeAviso,
                onValueChange = { vm.cambiarMensajeAviso(it) },
                label = { Text("Mensaje") },
                supportingText = { Text("${mensajeAviso.length}/100") },
                modifier = Modifier.fillMaxWidth()
            )
            SelectorIcono(
                elegido = iconoAviso,
                alElegir = { vm.cambiarIcono(it) }
            )
            Button(onClick = { vm.avisarAhora() }) {
                Text("Mostrar aviso")
            }
        }
        MensajeSinPermiso(vm)
    }
}

@Composable
fun PantallaRecordatorio(vm: AvisosViewModel, alVolver: () -> Unit) {
    val faltan by vm.faltan.collectAsState()

    PantallaBase(titulo = "Recordatorio", alVolver = alVolver) {
        Tarjeta("Recordatorio") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.recordarEn(10) },
                    enabled = faltan == null
                ) {
                    Text("Recordar en 10 s")
                }
                OutlinedButton(
                    onClick = { vm.cancelarRecordatorio() },
                    enabled = faltan != null
                ) {
                    Text("Cancelar")
                }
            }
            faltan?.let { Text("Faltan $it s") }
        }
        MensajeSinPermiso(vm)
    }
}

@Composable
fun PantallaAvion(vm: AvisosViewModel, alVolver: () -> Unit) {
    val escuchando by vm.escuchandoAvion.collectAsState()

    PantallaBase(titulo = "Modo avión", alVolver = alVolver) {
        Tarjeta("Receptor de anuncios") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Escuchar el modo avión")
                Switch(
                    checked = escuchando,
                    onCheckedChange = { vm.cambiarEscucha(it) }
                )
            }
        }
        MensajeSinPermiso(vm)
    }
}

// Los 5 íconos como círculos; el elegido lleva borde morado
@Composable
private fun SelectorIcono(elegido: IconoAviso, alElegir: (IconoAviso) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        IconoAviso.entries.forEach { icono ->
            val seleccionado = icono == elegido
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = if (seleccionado) 3.dp else 1.dp,
                        color = if (seleccionado) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape
                    )
                    .clickable { alElegir(icono) }
            ) {
                Image(
                    painter = painterResource(icono.recurso()),
                    contentDescription = icono.etiqueta,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
