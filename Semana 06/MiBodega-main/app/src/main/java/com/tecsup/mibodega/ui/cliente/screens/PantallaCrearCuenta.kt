package com.tecsup.mibodega.ui.cliente.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.mibodega.ui.cliente.modelo.SesionManager
import com.tecsup.mibodega.ui.cliente.modelo.Usuario
import com.tecsup.mibodega.ui.componentes.BotonPrimario
import com.tecsup.mibodega.ui.componentes.CampoTexto
import com.tecsup.mibodega.ui.theme.BodegaTheme
import com.tecsup.mibodega.ui.theme.GrisClaro
import com.tecsup.mibodega.ui.theme.RojoPrecio
import com.tecsup.mibodega.ui.theme.VerdeBodega

@Composable
fun PantallaCrearCuenta(
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.size(48.dp))
        }
        Text(
            text = "Completa tus datos para continuar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Foto de perfil",
                tint = VerdeBodega,
                modifier = Modifier
                    .size(84.dp)
                    .background(GrisClaro, CircleShape)
                    .padding(4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        CampoTexto(
            etiqueta = "Nombre completo",
            valor = nombre,
            onValorCambia = { nombre = it; mensajeError = null },
            placeholder = "Juan Pérez"
        )
        Spacer(Modifier.height(14.dp))

        CampoTexto(
            etiqueta = "Correo electrónico",
            valor = correo,
            onValorCambia = { correo = it; mensajeError = null },
            placeholder = "ejemplo@tecsup.edu.pe",
            teclado = KeyboardType.Email
        )
        Spacer(Modifier.height(14.dp))

        CampoTexto(
            etiqueta = "Teléfono",
            valor = telefono,
            onValorCambia = { telefono = it; mensajeError = null },
            placeholder = "987 654 321",
            teclado = KeyboardType.Phone
        )
        Spacer(Modifier.height(14.dp))

        CampoTexto(
            etiqueta = "Contraseña",
            valor = password,
            onValorCambia = { password = it; mensajeError = null },
            placeholder = "Mínimo 6 caracteres",
            teclado = KeyboardType.Password
        )
        Spacer(Modifier.height(14.dp))

        CampoTexto(
            etiqueta = "Confirmar contraseña",
            valor = confirmarPassword,
            onValorCambia = { confirmarPassword = it; mensajeError = null },
            placeholder = "Repite tu contraseña",
            teclado = KeyboardType.Password
        )
        Spacer(Modifier.height(14.dp))

        CampoTexto(
            etiqueta = "Dirección de entrega",
            valor = direccion,
            onValorCambia = { direccion = it; mensajeError = null },
            placeholder = "Av. Los Olivos 123"
        )
        Spacer(Modifier.height(14.dp))

        CampoTexto(
            etiqueta = "Referencia",
            valor = referencia,
            onValorCambia = { referencia = it; mensajeError = null },
            placeholder = "Frente al parque"
        )

        if (mensajeError != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = mensajeError!!,
                color = RojoPrecio,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        BotonPrimario(
            texto = "Crear cuenta",
            onClick = {
                when {
                    nombre.isBlank() || correo.isBlank() || password.isBlank() || direccion.isBlank() -> {
                        mensajeError = "Por favor completa todos los campos obligatorios."
                    }
                    !correo.contains("@") -> {
                        mensajeError = "Ingresa un correo electrónico válido."
                    }
                    password.length < 6 -> {
                        mensajeError = "La contraseña debe tener al menos 6 caracteres."
                    }
                    password != confirmarPassword -> {
                        mensajeError = "Las contraseñas no coinciden."
                    }
                    else -> {
                        val nuevoUsuario = Usuario(
                            nombre = nombre,
                            correo = correo,
                            password = password,
                            telefono = telefono.ifBlank { "987654321" },
                            direccion = direccion,
                            referencia = referencia
                        )
                        val resultado = SesionManager.registrarUsuario(nuevoUsuario)
                        if (resultado.isSuccess) {
                            onRegistroExitoso()
                        } else {
                            mensajeError = resultado.exceptionOrNull()?.message ?: "Error al registrar cuenta"
                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaCrearCuentaPreview() {
    BodegaTheme {
        PantallaCrearCuenta(onVolver = {}, onRegistroExitoso = {})
    }
}
