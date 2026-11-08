package com.tecsup.smartcatalog.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.R

@Composable
fun LoginScreen(estado: EstadoSesion, onIniciar: (String, String) -> Unit, modifier: Modifier = Modifier) {
    var correo by remember { mutableStateOf("") }
    // No rememberSaveable: nunca guardar la contraseña en el Bundle de la actividad.
    var clave by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val cargando = estado == EstadoSesion.Iniciando
    val error = estado as? EstadoSesion.Error
    val focus = LocalFocusManager.current
    val enviar = { focus.clearFocus(); onIniciar(correo, clave) }
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(76.dp)) {
                    Image(painterResource(R.drawable.ic_smartcatalog), null, Modifier.padding(14.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SmartCatalog", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Qué bueno verte", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Inicia sesión y encuentra tu próximo smartphone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Bienvenido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth().testTag("correo"), singleLine = true, enabled = !cargando,
                            isError = error?.errorCorreo != null,
                            supportingText = error?.errorCorreo?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = clave, onValueChange = { clave = it }, label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth().testTag("clave"), singleLine = true, enabled = !cargando,
                            isError = error?.errorClave != null,
                            supportingText = error?.errorClave?.let { { Text(it) } },
                            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { TextButton(onClick = { visible = !visible }, enabled = !cargando) { Text(if (visible) "Ocultar" else "Mostrar") } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (!cargando) enviar() })
                        )
                        error?.let {
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                                Text(it.mensaje, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Button(onClick = enviar, enabled = !cargando, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            if (cargando) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Verificando tu acceso...")
                            } else Text("Iniciar sesión")
                        }
                    }
                }
                Text("Usa la cuenta asignada para el laboratorio. Si necesitas acceso, consulta al responsable.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun VerificandoSesion(modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator()
            Spacer(Modifier.height(20.dp))
            Text("Verificando tu sesión...", style = MaterialTheme.typography.titleMedium)
        }
    }
}
