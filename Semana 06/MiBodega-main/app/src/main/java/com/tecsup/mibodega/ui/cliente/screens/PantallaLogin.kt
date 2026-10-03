package com.tecsup.mibodega.ui.cliente.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.mibodega.R
import com.tecsup.mibodega.ui.cliente.modelo.SesionManager
import com.tecsup.mibodega.ui.componentes.BotonPrimario
import com.tecsup.mibodega.ui.componentes.BotonSecundario
import com.tecsup.mibodega.ui.componentes.CampoTexto
import com.tecsup.mibodega.ui.theme.AzulEnlace
import com.tecsup.mibodega.ui.theme.BodegaTheme
import com.tecsup.mibodega.ui.theme.FondoClaro
import com.tecsup.mibodega.ui.theme.RojoPrecio
import com.tecsup.mibodega.ui.theme.VerdeBodega

@Composable
fun PantallaLogin(
    onRegistrarse: () -> Unit,
    onLoginExitoso: () -> Unit,
    onTerminos: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FondoClaro, MaterialTheme.colorScheme.background),
                    endY = 900f
                )
            )
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ilustracion_bodega),
                contentDescription = "Ilustración de la bodega",
                modifier = Modifier.size(140.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                append("Mi ")
                withStyle(SpanStyle(color = VerdeBodega)) { append("Bodega") }
            },
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Inicia sesión con tu cuenta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        CampoTexto(
            etiqueta = "Correo electrónico",
            valor = correo,
            onValorCambia = { correo = it; mensajeError = null },
            placeholder = "ejemplo@tecsup.edu.pe",
            teclado = KeyboardType.Email
        )

        Spacer(Modifier.height(16.dp))

        CampoTexto(
            etiqueta = "Contraseña",
            valor = password,
            onValorCambia = { password = it; mensajeError = null },
            placeholder = "********",
            teclado = KeyboardType.Password
        )

        if (mensajeError != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = mensajeError!!,
                color = RojoPrecio,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        BotonPrimario(
            texto = "Iniciar sesión",
            onClick = {
                if (correo.isBlank() || password.isBlank()) {
                    mensajeError = "Por favor ingresa correo y contraseña"
                    return@BotonPrimario
                }
                val resultado = SesionManager.iniciarSesion(correo, password)
                if (resultado.isSuccess) {
                    onLoginExitoso()
                } else {
                    mensajeError = resultado.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        BotonSecundario(
            texto = "Registrarme",
            onClick = onRegistrarse
        )

        Spacer(Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Al continuar aceptas nuestros",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Términos y Condiciones",
                style = MaterialTheme.typography.bodySmall,
                color = AzulEnlace,
                modifier = Modifier.clickable(onClick = onTerminos)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaLoginPreview() {
    BodegaTheme {
        PantallaLogin({}, {}, {})
    }
}
