package com.tecsup.campusgps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.campusgps.model.Campus
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.ui.theme.AzulGps
import com.tecsup.campusgps.ui.theme.AzulNoche
import com.tecsup.campusgps.ui.theme.AzulRey

/*
 * Mejora 6: cabecera y pie del rediseño. Solo dibujan; el estado llega por parámetro.
 */

// Borde inferior en forma de onda
private val OndaInferior = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.84f)
    cubicTo(
        size.width * 0.72f, size.height * 1.04f,
        size.width * 0.32f, size.height * 0.72f,
        0f, size.height * 0.92f
    )
    close()
}

// Borde superior en forma de onda (pie)
private val OndaSuperior = GenericShape { size, _ ->
    moveTo(0f, size.height * 0.38f)
    cubicTo(
        size.width * 0.30f, 0f,
        size.width * 0.68f, size.height * 0.62f,
        size.width, size.height * 0.18f
    )
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

private val Degradado = Brush.linearGradient(listOf(AzulNoche, AzulRey))

@Composable
fun CabeceraCampus(
    estado: UbicacionUi,
    onLocalizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(OndaInferior)
            .background(Degradado)
    ) {
        // Edificio decorativo (versión simple de la ilustración)
        Icon(
            Icons.Filled.AccountBalance,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.16f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 6.dp, end = 10.dp)
                .size(118.dp)
        )

        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                LogoCampus()
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White)) { append("Campus") }
                            withStyle(SpanStyle(color = AzulGps)) { append("GPS") }
                        },
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Explora ${Campus.NOMBRE} ",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic
                        )
                        Icon(
                            Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            PildoraEstado(estado, onLocalizar)
        }
    }
}

@Composable
private fun LogoCampus() {
    Box(Modifier.size(58.dp)) {
        Icon(
            Icons.Filled.LocationOn,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            Icons.Filled.School,
            contentDescription = null,
            tint = AzulRey,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 11.dp)
                .size(21.dp)
        )
    }
}

/** Píldora "Ubicación activa" que refleja el estado real y permite volver a localizar. */
@Composable
private fun PildoraEstado(estado: UbicacionUi, onLocalizar: () -> Unit) {
    val (texto, color) = when (estado) {
        UbicacionUi.Inicial -> "Ubicación inactiva" to Color(0xFF9AA3B5)
        UbicacionUi.Buscando -> "Buscando ubicación…" to Color(0xFFF9A825)
        is UbicacionUi.Lista -> "Ubicación activa" to Color(0xFF2E9E4A)
        is UbicacionUi.Error -> "Sin ubicación" to Color(0xFFD93025)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFEFF7F1),
        shadowElevation = 3.dp
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(11.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(texto, color = AzulNoche, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(Color(0x33000000))
            )
            IconButton(onClick = onLocalizar, enabled = estado !is UbicacionUi.Buscando) {
                if (estado is UbicacionUi.Buscando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = AzulNoche)
                } else {
                    Icon(Icons.Filled.GpsFixed, contentDescription = "Obtener mi ubicación", tint = AzulNoche)
                }
            }
        }
    }
}

@Composable
fun PieCampus(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(OndaSuperior)
            .background(Degradado)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(46.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.School,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(top = 10.dp)
                .size(28.dp)
        )
    }
}
