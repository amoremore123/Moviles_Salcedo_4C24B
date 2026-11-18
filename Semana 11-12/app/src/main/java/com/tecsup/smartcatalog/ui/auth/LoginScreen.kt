package com.tecsup.smartcatalog.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.ui.components.BotonPrincipal
import com.tecsup.smartcatalog.ui.components.FondoOndas
import com.tecsup.smartcatalog.ui.components.coloresCampo

@Composable
fun LoginScreen(
    estado: EstadoSesion,
    onIniciar: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onRegistro: (() -> Unit)? = null
) {
    var correo by remember { mutableStateOf("") }
    // No rememberSaveable: nunca guardar la contraseña en el Bundle de la actividad.
    var clave by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val cargando = estado == EstadoSesion.Iniciando
    val error = estado as? EstadoSesion.Error
    val focus = LocalFocusManager.current
    val enviar = { focus.clearFocus(); onIniciar(correo, clave) }
    PaginaAcceso(modifier) {
        MarcaSmartCatalog()
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Bienvenido", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Accede con la cuenta asignada para el laboratorio.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        TarjetaAcceso {
            OutlinedTextField(
                value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth().testTag("correo"), singleLine = true, enabled = !cargando,
                shape = CircleShape, colors = coloresCampo(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = error?.errorCorreo != null,
                supportingText = error?.errorCorreo?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = clave, onValueChange = { clave = it }, label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth().testTag("clave"), singleLine = true, enabled = !cargando,
                shape = CircleShape, colors = coloresCampo(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                isError = error?.errorClave != null,
                supportingText = error?.errorClave?.let { { Text(it) } },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { TextButton(onClick = { visible = !visible }, enabled = !cargando) { Text(if (visible) "Ocultar" else "Mostrar") } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (!cargando) enviar() })
            )
            error?.let {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                    Text(it.mensaje, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                }
            }
            BotonPrincipal(onClick = enviar, enabled = !cargando, modifier = Modifier.fillMaxWidth()) {
                if (cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    Spacer(Modifier.width(10.dp))
                    Text("Verificando tu acceso...")
                } else {
                    Text("Iniciar sesión")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
        onRegistro?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Eres nuevo?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = it, enabled = !cargando) { Text("Regístrate aquí") }
            }
        }
        Image(painterResource(R.drawable.gatito_saludo), contentDescription = null, modifier = Modifier.size(130.dp))
    }
}

@Composable
fun VerificandoSesion(modifier: Modifier = Modifier) {
    FondoOndas(modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painterResource(R.drawable.gatito_saludo), contentDescription = null, modifier = Modifier.size(150.dp))
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Verificando tu sesión...", style = MaterialTheme.typography.titleMedium)
        }
    }
}
