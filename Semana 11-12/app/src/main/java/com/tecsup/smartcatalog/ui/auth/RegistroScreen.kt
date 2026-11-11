package com.tecsup.smartcatalog.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RegistroScreen(modelo: RegistroViewModel, onVolver: () -> Unit) {
    val estado by modelo.estado.collectAsStateWithLifecycle()
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    BackHandler { if (!estado.cargando && !estado.pendiente) onVolver() }
    LaunchedEffect(estado.pendiente, estado.completo) {
        if (estado.pendiente || estado.completo) {
            clave = ""
            confirmacion = ""
        }
    }

    PaginaAcceso {
        MarcaSmartCatalog()
        Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge)
        if (!modelo.disponible) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "Registro aún no disponible. Puedes revisar el formulario mientras se habilita el servidor.",
                    Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (!estado.pendiente && !estado.completo) {
            OutlinedTextField(nombre, { nombre = it }, Modifier.fillMaxWidth(), enabled = !estado.cargando, shape = MaterialTheme.shapes.medium,
                label = { Text("Nombre completo") }, singleLine = true)
            OutlinedTextField(correo, { correo = it }, Modifier.fillMaxWidth(), enabled = !estado.cargando, shape = MaterialTheme.shapes.medium,
                label = { Text("Correo electrónico") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            val transformacion = if (visible) VisualTransformation.None else PasswordVisualTransformation()
            OutlinedTextField(clave, { clave = it }, Modifier.fillMaxWidth(), enabled = !estado.cargando, shape = MaterialTheme.shapes.medium,
                label = { Text("Contraseña") }, singleLine = true, visualTransformation = transformacion,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            OutlinedTextField(confirmacion, { confirmacion = it }, Modifier.fillMaxWidth(), enabled = !estado.cargando, shape = MaterialTheme.shapes.medium,
                label = { Text("Confirmar contraseña") }, singleLine = true, visualTransformation = transformacion,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            TextButton(onClick = { visible = !visible }, enabled = !estado.cargando) {
                Text(if (visible) "Ocultar contraseñas" else "Mostrar contraseñas")
            }
            Button(
                onClick = { modelo.iniciar(nombre, correo, clave, confirmacion) },
                enabled = !estado.cargando && nombre.isNotBlank() && correo.isNotBlank() &&
                    clave.isNotBlank() && confirmacion.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
            ) { Text(if (modelo.disponible) "Crear cuenta" else "Validar formulario") }
        }
        if (estado.pendiente) {
            Button(modelo::comprobarCorreo, enabled = !estado.cargando) { Text("Ya verifiqué mi correo") }
            TextButton(modelo::reenviar, enabled = !estado.cargando) { Text("Reenviar enlace") }
        }
        if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        estado.mensaje?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        if (!estado.pendiente) {
            TextButton(onClick = onVolver, enabled = !estado.cargando) { Text("Volver al inicio de sesión") }
        }
    }
}
