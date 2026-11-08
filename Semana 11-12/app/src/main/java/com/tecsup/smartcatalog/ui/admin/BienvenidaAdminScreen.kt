package com.tecsup.smartcatalog.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BienvenidaAdminScreen(onCatalogo: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.safeDrawingPadding().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
            Text("SmartCatalog", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Bienvenido, administrador", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Tu acceso de administrador está verificado. Puedes explorar el catálogo, sus detalles y los favoritos de este dispositivo.")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCatalogo) { Text("Explorar catálogo") }
            Spacer(Modifier.height(24.dp))
            Text("Espacio de administración", style = MaterialTheme.typography.titleMedium)
            Text("Las herramientas de administración estarán disponibles próximamente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
