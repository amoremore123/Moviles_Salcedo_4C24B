package com.tecsup.campusgps.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector
import com.tecsup.campusgps.model.Lugar

/*
 * Mejora 6: cómo se ve cada lugar (ícono, emoji del marcador y etiqueta corta).
 * Es solo presentación, por eso vive en ui/ y no en el modelo.
 */

fun iconoLugar(lugar: Lugar): ImageVector = when {
    lugar.nombre.contains("Biblioteca", ignoreCase = true) -> Icons.AutoMirrored.Filled.MenuBook
    lugar.nombre.contains("Cafeter", ignoreCase = true) -> Icons.Filled.LocalCafe
    lugar.nombre.contains("Laboratorio", ignoreCase = true) -> Icons.Filled.Science
    lugar.nombre.contains("Auditorio", ignoreCase = true) -> Icons.Filled.TheaterComedy
    lugar.nombre.contains("Estacionamiento", ignoreCase = true) -> Icons.Filled.DirectionsCar
    else -> iconoCategoria(lugar.categoria)
}

fun iconoCategoria(categoria: String): ImageVector = when (categoria) {
    "Estudio" -> Icons.Filled.School
    "Comida" -> Icons.Filled.Restaurant
    "Eventos" -> Icons.Filled.Event
    "Servicios" -> Icons.Filled.MiscellaneousServices
    else -> Icons.Filled.Place
}

/** Emoji que se dibuja dentro del pin en el mapa de OpenStreetMap. */
fun emojiLugar(lugar: Lugar): String = when {
    lugar.nombre.contains("Biblioteca", ignoreCase = true) -> "📖"
    lugar.nombre.contains("Cafeter", ignoreCase = true) -> "☕"
    lugar.nombre.contains("Laboratorio", ignoreCase = true) -> "🧪"
    lugar.nombre.contains("Auditorio", ignoreCase = true) -> "🎭"
    lugar.nombre.contains("Estacionamiento", ignoreCase = true) -> "🚗"
    else -> "📍"
}

/** "Biblioteca Central" -> "Biblioteca" (para la etiqueta del marcador). */
fun etiquetaCorta(lugar: Lugar): String = lugar.nombre.substringBefore(' ')
