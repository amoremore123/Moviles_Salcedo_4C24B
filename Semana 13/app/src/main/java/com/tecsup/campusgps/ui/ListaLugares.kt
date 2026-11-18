package com.tecsup.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.ui.theme.AzulBorde
import com.tecsup.campusgps.ui.theme.AzulCielo
import com.tecsup.campusgps.ui.theme.AzulNoche
import com.tecsup.campusgps.ui.theme.AzulRey
import com.tecsup.campusgps.ui.theme.GrisTexto
import com.tecsup.campusgps.ui.theme.VerdeCerca
import com.tecsup.campusgps.ui.theme.VerdeCercaFondo
import com.tecsup.campusgps.util.formatearDistancia

/**
 * Solo dibuja la lista que recibe. No conoce al ViewModel:
 * avisa del toque con onClick(id) y la pantalla decide qué hacer.
 */
@Composable
fun ListaLugares(
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    // Mejora 2: ids dentro del radio (ya calculados por el ViewModel) y el radio actual
    dentroDelRadio: Set<Int> = emptySet(),
    radio: Int? = null,
    // Mejora 6: el más cercano se pinta en verde con la insignia "MÁS CERCA"
    idMasCercano: Int? = null
) {
    // Al reordenarse (nueva búsqueda), LazyColumn se queda anclada al item que estaba primero
    // y el más cercano queda escondido arriba. Por eso vuelve al inicio cuando cambia el orden.
    val estadoLista = rememberLazyListState()
    val orden = lugares.map { it.lugar.id }
    LaunchedEffect(orden) { estadoLista.scrollToItem(0) }

    LazyColumn(
        modifier = modifier,
        state = estadoLista,
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(lugares, key = { it.lugar.id }) { item ->
            TarjetaLugar(
                item = item,
                esSeleccionado = item.lugar.id == seleccionado,
                esMasCercano = item.lugar.id == idMasCercano,
                dentro = if (item.lugar.id in dentroDelRadio) radio else null,
                onClick = { onClick(item.lugar.id) }
            )
        }
    }
}

@Composable
private fun TarjetaLugar(
    item: LugarConDistancia,
    esSeleccionado: Boolean,
    esMasCercano: Boolean,
    dentro: Int?, // radio en metros si el lugar está dentro; null si no
    onClick: () -> Unit
) {
    val borde = when {
        esSeleccionado -> BorderStroke(2.dp, AzulRey)
        esMasCercano -> BorderStroke(1.5.dp, VerdeCerca.copy(alpha = 0.55f))
        else -> BorderStroke(1.dp, AzulBorde.copy(alpha = 0.7f))
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esMasCercano) VerdeCercaFondo else Color.White
        ),
        border = borde,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono del lugar en un círculo
            Box(
                Modifier
                    .size(50.dp)
                    .background(if (esMasCercano) Color.White else AzulCielo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    iconoLugar(item.lugar),
                    contentDescription = null,
                    tint = if (esMasCercano) VerdeCerca else AzulRey,
                    modifier = Modifier.size(27.dp)
                )
            }
            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.lugar.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AzulNoche,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        iconoCategoria(item.lugar.categoria),
                        contentDescription = null,
                        tint = GrisTexto,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(item.lugar.categoria, fontSize = 14.sp, color = GrisTexto)
                }
                if (dentro != null) {
                    Text(
                        "Dentro de $dentro m",
                        fontSize = 11.sp,
                        color = VerdeCerca,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Separador vertical
            Box(
                Modifier
                    .padding(horizontal = 10.dp)
                    .width(1.dp)
                    .height(40.dp)
                    .background(AzulBorde)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.metros?.let { formatearDistancia(it) } ?: "Sin calcular",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (item.metros != null) 22.sp else 13.sp,
                    color = AzulNoche
                )
                if (esMasCercano) {
                    Spacer(Modifier.height(2.dp))
                    Surface(shape = RoundedCornerShape(50), color = VerdeCerca) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text("MÁS CERCA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
