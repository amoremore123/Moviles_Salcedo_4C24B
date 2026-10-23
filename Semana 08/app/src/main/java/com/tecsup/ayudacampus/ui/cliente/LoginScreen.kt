package com.tecsup.ayudacampus.ui.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    rolSeleccionado: String,
    onRolSeleccionado: (String) -> Unit,
    cargando: Boolean,
    error: String?,
    onEditar: () -> Unit,
    onLogin: (String, String, String) -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val ingresar = {
        focus.clearFocus()
        onLogin(correo, clave, rolSeleccionado)
    }
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("A", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Column {
                    Text("AyudaCampus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("MESA DE AYUDA · TECSUP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tu campus,\nsiempre conectado.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Ingresa para conectar con el soporte de tu campus.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Bienvenido de nuevo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Elige cómo quieres ingresar", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PerfilOpcion("Estudiante", "CLIENTE", rolSeleccionado, !cargando, Modifier.weight(1f)) {
                            onEditar(); onRolSeleccionado(it)
                        }
                        PerfilOpcion("Soporte TI", "ADMIN", rolSeleccionado, !cargando, Modifier.weight(1f)) {
                            onEditar(); onRolSeleccionado(it)
                        }
                    }
                    OutlinedTextField(
                        value = correo, onValueChange = { correo = it; onEditar() },
                        modifier = Modifier.fillMaxWidth().testTag("correo"),
                        label = { Text("Correo") }, placeholder = { Text("tu.correo@campus.test") },
                        enabled = !cargando, singleLine = true, shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = clave, onValueChange = { clave = it; onEditar() },
                        modifier = Modifier.fillMaxWidth().testTag("clave"),
                        label = { Text("Contraseña") }, enabled = !cargando, singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { mostrarClave = !mostrarClave }, enabled = !cargando) {
                                Text(if (mostrarClave) "Ocultar" else "Mostrar", style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (!cargando) ingresar() })
                    )
                    if (error != null) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                            Text(error, Modifier.fillMaxWidth().padding(12.dp).testTag("errorLogin"), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Button(
                        onClick = ingresar, enabled = !cargando,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("ingresar"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(if (cargando) "Ingresando…" else "Ingresar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Text("Un solo campus. Dos formas de ayudar.", Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PerfilOpcion(
    texto: String, rol: String, seleccionado: String, habilitado: Boolean,
    modifier: Modifier, onSeleccionar: (String) -> Unit
) {
    FilterChip(
        selected = rol == seleccionado, onClick = { onSeleccionar(rol) }, enabled = habilitado,
        label = { Text(texto, fontWeight = FontWeight.Medium) },
        modifier = modifier.heightIn(min = 48.dp).testTag("perfil_$rol"),
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
