package com.tecsup.avisoscampus.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.avisoscampus.R
import com.tecsup.avisoscampus.model.PermisoUi
import com.tecsup.avisoscampus.notificacion.Notificador
import com.tecsup.avisoscampus.ui.theme.Amarillo
import com.tecsup.avisoscampus.ui.theme.MoradoOscuro
import com.tecsup.avisoscampus.ui.theme.TarjetaAmarilla
import com.tecsup.avisoscampus.ui.theme.TarjetaLila
import com.tecsup.avisoscampus.ui.theme.TarjetaRosa
import com.tecsup.avisoscampus.ui.theme.TarjetaVerde
import com.tecsup.avisoscampus.viewmodel.AvisosViewModel

@Composable
fun PantallaInicio(vm: AvisosViewModel, alNavegar: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Encabezado()
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaBienvenida()

            Text(
                "Funciones principales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaFuncion(
                    titulo = "Enviar aviso\nal instante",
                    icono = R.drawable.ic_card_campana,
                    color = TarjetaLila,
                    modifier = Modifier.weight(1f),
                    alTocar = { alNavegar(Rutas.ENVIAR) }
                )
                TarjetaFuncion(
                    titulo = "Programar recordatorio\n(en 10 s)",
                    icono = R.drawable.ic_card_calendario,
                    color = TarjetaVerde,
                    modifier = Modifier.weight(1f),
                    alTocar = { alNavegar(Rutas.RECORDATORIO) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaFuncion(
                    titulo = "Detectar\nmodo avión",
                    icono = R.drawable.ic_card_avion,
                    color = TarjetaAmarilla,
                    modifier = Modifier.weight(1f),
                    alTocar = { alNavegar(Rutas.AVION) }
                )
                TarjetaFuncion(
                    titulo = "Ver historial\nde avisos",
                    icono = R.drawable.ic_card_historial,
                    color = TarjetaRosa,
                    modifier = Modifier.weight(1f),
                    alTocar = { alNavegar(Rutas.HISTORIAL) }
                )
            }

            TarjetaPermiso(vm)
            Spacer(Modifier.height(8.dp))
        }
    }
}

// Encabezado: campana, título y subtítulo arriba; el campus de fondo abajo,
// con un degradado hacia el color de la pantalla para que no corte de golpe
@Composable
private fun Encabezado() {
    val fondo = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, fondo))
            )
    ) {
        Image(
            painterResource(R.drawable.ilus_campus),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(190.dp)
                .padding(horizontal = 24.dp)
        )
        // Degradado lila abajo: funde la ilustración con el resto de la pantalla
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, fondo)))
        )
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painterResource(R.drawable.ic_card_campana),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
            Column {
                Text(
                    "AvisosCampus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Tu celular, siempre informado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaBienvenida() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "¡Hola!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Amarillo,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "Mantente al día con tus avisos y descubre cómo funcionan " +
                        "las notificaciones en Android.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Image(
                painterResource(R.drawable.ilus_mascota),
                contentDescription = "Mascota de AvisosCampus",
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

// Tarjeta de color que lleva a una función de la app
@Composable
private fun TarjetaFuncion(
    titulo: String,
    @DrawableRes icono: Int,
    color: Color,
    modifier: Modifier = Modifier,
    alTocar: () -> Unit
) {
    Card(
        onClick = alTocar,
        modifier = modifier.height(150.dp),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = MoradoOscuro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painterResource(icono),
                contentDescription = null,
                modifier = Modifier.size(52.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Tarjeta compacta del permiso: estado, botón para pedirlo y error con acceso a Ajustes
@Composable
private fun TarjetaPermiso(vm: AvisosViewModel) {
    val context = LocalContext.current
    val permiso by vm.permiso.collectAsState()

    // Muestra el diálogo del sistema y entrega la respuesta al ViewModel
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> vm.permisoRespondido(concedido) }

    Tarjeta("Permiso de notificaciones") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EtiquetaPermiso(permiso)
            Button(onClick = {
                if (Notificador.tienePermiso(context)) {
                    // Ya concedido (por ejemplo, al volver de Ajustes) o Android < 13:
                    // no hace falta el diálogo
                    vm.permisoRespondido(true)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }) {
                Text("Permitir notificaciones")
            }
        }
        // Mensaje de error y "Abrir ajustes" (tras negar dos veces solo queda Ajustes)
        val mensaje by vm.mensaje.collectAsState()
        if (mensaje == null && permiso == PermisoUi.Denegado) {
            OutlinedButton(
                onClick = { context.startActivity(intentAjustes(context)) }
            ) { Text("Abrir ajustes") }
        }
        MensajeSinPermiso(vm)
    }
}

// Etiqueta de color: gris = sin pedir, verde = permitido, rojo = denegado
@Composable
private fun EtiquetaPermiso(permiso: PermisoUi) {
    val (texto, color) = when (permiso) {
        PermisoUi.SinPedir -> "Sin pedir" to Color(0xFF757575)
        PermisoUi.Concedido -> "Permitido" to Color(0xFF2E7D32)
        PermisoUi.Denegado -> "Denegado" to Color(0xFFC62828)
    }
    Text(
        texto,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
