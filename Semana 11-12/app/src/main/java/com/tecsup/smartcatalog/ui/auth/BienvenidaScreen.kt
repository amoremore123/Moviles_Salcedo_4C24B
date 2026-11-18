package com.tecsup.smartcatalog.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.ui.components.BotonPrincipal
import com.tecsup.smartcatalog.ui.components.FondoOndas

@Composable
internal fun BienvenidaScreen(onLogin: () -> Unit, onRegistro: () -> Unit) {
    PaginaAcceso {
        MarcaSmartCatalog()
        Image(painterResource(R.drawable.gatito_saludo), contentDescription = null, modifier = Modifier.size(180.dp))
        TarjetaAcceso {
            Text(
                "Encuentra tu próximo smartphone",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Explora modelos y guarda tus favoritos para consultarlos después.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        BotonPrincipal(onLogin, Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        BotonSecundario("Crear cuenta", onRegistro)
    }
}

@Composable
internal fun PaginaAcceso(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    FondoOndas(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = contenido
            )
        }
    }
}

@Composable
internal fun MarcaSmartCatalog() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "SmartCatalog",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Tu catálogo en una app ♡",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Tarjeta blanca redondeada con sombra suave para agrupar contenido de acceso. */
@Composable
internal fun TarjetaAcceso(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = contenido)
    }
}

@Composable
internal fun BotonSecundario(texto: String, onClick: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) { Text(texto) }
}
