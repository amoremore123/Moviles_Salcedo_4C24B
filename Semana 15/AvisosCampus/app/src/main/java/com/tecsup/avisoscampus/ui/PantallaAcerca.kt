package com.tecsup.avisoscampus.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// Marcador simple: el diseño final llega en un commit posterior
@Composable
fun PantallaAcerca() {
    PantallaBase(titulo = "Acerca de") {
        Tarjeta("AvisosCampus") {
            Text("App de laboratorio que muestra notificaciones locales y escucha el modo avión.")
            Text("Kotlin · Jetpack Compose · Material 3 · MVVM + Repository")
        }
        Tarjeta("Autor") {
            Text("Amore Salcedo Macedo")
            Text("Programación en Móviles — 4to ciclo")
            Text("Docente: Juan León S.")
        }
    }
}
