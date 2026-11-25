package com.tecsup.avisoscampus.ui

import androidx.annotation.DrawableRes
import com.tecsup.avisoscampus.R
import com.tecsup.avisoscampus.model.IconoAviso

// Traduce cada ícono del modelo a su vector en res/drawable.
// Lo usan la pantalla, el historial y el Notificador (ícono grande).
@DrawableRes
fun IconoAviso.recurso(): Int = when (this) {
    IconoAviso.ESTRELLA -> R.drawable.ic_aviso_estrella
    IconoAviso.CORAZON -> R.drawable.ic_aviso_corazon
    IconoAviso.REGALO -> R.drawable.ic_aviso_regalo
    IconoAviso.CORONA -> R.drawable.ic_aviso_corona
    IconoAviso.FLOR -> R.drawable.ic_aviso_flor
}
