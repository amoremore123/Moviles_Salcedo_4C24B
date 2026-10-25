package com.tecsup.ayudacampus.ui.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.ayudacampus.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    vm: RegistroViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    val datos by vm.formulario.collectAsStateWithLifecycle()
    val errores by vm.errores.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val registrando by vm.registrando.collectAsStateWithLifecycle()
    val exitoso by vm.registroExitoso.collectAsStateWithLifecycle()
    var desplegadoRol by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    LaunchedEffect(exitoso) {
        if (exitoso) {
            onRegistroExitoso()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver, enabled = !registrando) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver al login")
                }
                Text("Crear cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Column {
                    Text("Bienvenido a AyudaCampus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Crea tu cuenta para enviar tickets de soporte", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Informacion de cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = datos.nombre,
                        onValueChange = vm::actualizarNombre,
                        modifier = Modifier.fillMaxWidth().testTag("registro_nombre"),
                        label = { Text("Nombre completo") },
                        placeholder = { Text("Juan Perez") },
                        enabled = !registrando,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = errores.nombre != null,
                        supportingText = errores.nombre?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = datos.correo,
                        onValueChange = vm::actualizarCorreo,
                        modifier = Modifier.fillMaxWidth().testTag("registro_correo"),
                        label = { Text("Correo electronico") },
                        placeholder = { Text("tu.correo@campus.test") },
                        enabled = !registrando,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                        isError = errores.correo != null,
                        supportingText = errores.correo?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = datos.clave,
                        onValueChange = vm::actualizarClave,
                        modifier = Modifier.fillMaxWidth().testTag("registro_clave"),
                        label = { Text("Contraseña") },
                        placeholder = { Text("Minimo 4 caracteres") },
                        enabled = !registrando,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (registrando) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = errores.clave != null,
                        supportingText = errores.clave?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = datos.confirmarClave,
                        onValueChange = vm::actualizarConfirmarClave,
                        modifier = Modifier.fillMaxWidth().testTag("registro_confirmar"),
                        label = { Text("Confirmar contraseña") },
                        placeholder = { Text("Repite tu contraseña") },
                        enabled = !registrando,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errores.confirmarClave != null,
                        supportingText = errores.confirmarClave?.let { { Text(it) } },
                        keyboardActions = KeyboardActions(onDone = { if (!registrando) vm.registrar() })
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tipo de cuenta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RolOption(
                                texto = "Estudiante",
                                rol = "CLIENTE",
                                seleccionado = datos.rol,
                                habilitado = !registrando,
                                modifier = Modifier.weight(1f),
                                onSeleccionar = vm::seleccionarRol
                            )
                            Text("o", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Soporte TI",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (error != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                error!!,
                                Modifier.fillMaxWidth().padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = { focus.clearFocus(); vm.registrar() },
                        enabled = !registrando,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("btn_registrar"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (registrando) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            if (registrando) "Creando cuenta..." else "Crear cuenta",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ya tienes una cuenta? ", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onVolver) {
                    Text("Inicia sesion aqui", style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline))
                }
            }
        }
    }
}

@Composable
private fun RolOption(
    texto: String,
    rol: String,
    seleccionado: String,
    habilitado: Boolean,
    modifier: Modifier,
    onSeleccionar: (String) -> Unit
) {
    FilterChip(
        selected = rol == seleccionado,
        onClick = { onSeleccionar(rol) },
        enabled = habilitado,
        label = { Text(texto, fontWeight = FontWeight.Medium) },
        modifier = modifier.heightIn(min = 48.dp).testTag("rol_$rol"),
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
