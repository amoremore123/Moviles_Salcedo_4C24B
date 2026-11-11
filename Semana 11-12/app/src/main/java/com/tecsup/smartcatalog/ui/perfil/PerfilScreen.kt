package com.tecsup.smartcatalog.ui.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
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
            Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver desde el perfil")
                    }
                    Text("Mi perfil", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 560.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedCard(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DatoPerfil("Nombre", sesion.nombre ?: "Sin nombre registrado")
                        DatoPerfil("Correo electrónico", sesion.correo)
                        DatoPerfil("Rol", if (sesion.rol == Rol.ADMIN) "Administrador" else "Usuario")
                        OutlinedButton(onClick = {}, enabled = false) { Text("Editar nombre") }
                        Text(
                            "La edición requiere permisos de escritura en Firestore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedCard(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.fillMaxWidth().padding(18.dp).selectableGroup()) {
                        Text("Tema de la aplicación", style = MaterialTheme.typography.titleLarge)
                        TemaSmartCatalog.entries.forEach { opcion ->
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                    .selectable(tema == opcion, onClick = { preferencias.elegir(opcion) }, role = Role.RadioButton),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = tema == opcion, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(opcion.etiqueta, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun DatoPerfil(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
