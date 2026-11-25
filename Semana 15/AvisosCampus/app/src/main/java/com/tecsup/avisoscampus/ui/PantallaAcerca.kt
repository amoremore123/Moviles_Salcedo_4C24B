package com.tecsup.avisoscampus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.avisoscampus.R

@Composable
fun PantallaAcerca() {
    PantallaBase(titulo = "Acerca de") {
        // Presentación de la app con la mascota
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(132.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Image(
                        painterResource(R.drawable.ilus_mascota),
                        contentDescription = "Mascota de AvisosCampus",
                        modifier = Modifier.size(104.dp)
                    )
                }
                Text(
                    "AvisosCampus",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Versión 1.0",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                Text(
                    "App de laboratorio que muestra notificaciones locales y escucha el modo avión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Datos del proyecto
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 4.dp)) {
                Dato(Icons.Filled.Person, "Autor", "Amore Salcedo Macedo")
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Dato(Icons.Filled.School, "Curso", "Programación en Móviles (4to ciclo)")
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Dato(Icons.Filled.SupervisorAccount, "Docente", "Juan León S.")
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Dato(
                    Icons.Filled.Code,
                    "Tecnologías",
                    "Kotlin, Jetpack Compose, Material 3, MVVM + Repository"
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Dato(Icons.Filled.Brush, "Ilustraciones", "ver CREDITOS_IMAGENES.md")
            }
        }
    }
}

@Composable
private fun Dato(icono: ImageVector, etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Column(Modifier.padding(start = 14.dp)) {
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(valor, fontWeight = FontWeight.SemiBold)
        }
    }
}
