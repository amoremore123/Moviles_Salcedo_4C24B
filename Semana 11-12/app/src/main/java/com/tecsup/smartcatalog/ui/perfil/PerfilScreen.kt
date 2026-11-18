package com.tecsup.smartcatalog.ui.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.ui.components.CabeceraMarca
import com.tecsup.smartcatalog.ui.theme.ColoresMarca
import com.tecsup.smartcatalog.ui.theme.PreferenciasTema
import com.tecsup.smartcatalog.ui.theme.TemaSmartCatalog

@Composable
internal fun PerfilScreen(
    sesion: Sesion,
    preferencias: PreferenciasTema,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val tema by preferencias.tema.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CabeceraMarca("Mi perfil", navegacion = {
                IconButton(onClick = onVolver) {
                    Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver desde el perfil")
                }
            })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 560.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaBlanca {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            Modifier.size(84.dp).clip(CircleShape).background(ColoresMarca.degradado),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(sesion.nombre ?: "Sin nombre registrado", style = MaterialTheme.typography.titleLarge)
                        Text(sesion.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val admin = sesion.rol == Rol.ADMIN
                        Surface(
                            shape = CircleShape,
                            color = if (admin) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                if (admin) "Administrador" else "Usuario",
                                Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (admin) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = {}, enabled = false, shape = CircleShape) { Text("Editar nombre") }
                        Text(
                            "La edición requiere permisos de escritura en Firestore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TarjetaBlanca {
                    Column(Modifier.fillMaxWidth().padding(18.dp).selectableGroup(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tema de la aplicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TemaSmartCatalog.entries.forEach { opcion ->
                            val elegido = tema == opcion
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(CircleShape)
                                    .background(if (elegido) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .selectable(elegido, onClick = { preferencias.elegir(opcion) }, role = Role.RadioButton)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = elegido, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(opcion.etiqueta, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun TarjetaBlanca(contenido: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        content = contenido
    )
}
