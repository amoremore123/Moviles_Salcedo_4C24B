package com.tecsup.smartcatalog.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.R

@Composable
internal fun BienvenidaScreen(onLogin: () -> Unit, onRegistro: () -> Unit) {
    PaginaAcceso {
        MarcaSmartCatalog()
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    painterResource(R.drawable.smartphone_aurora),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(190.dp)
                )
                Text("Encuentra tu próximo smartphone", style = MaterialTheme.typography.displaySmall)
                Text("Explora modelos y guarda tus favoritos para consultarlos después.")
            }
        }
        Button(onLogin, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("Iniciar sesión") }
        OutlinedButton(onRegistro, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("Crear cuenta") }
    }
}

@Composable
internal fun PaginaAcceso(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = contenido
            )
        }
    }
}

@Composable
internal fun MarcaSmartCatalog() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Image(painterResource(R.drawable.ic_smartcatalog), null, Modifier.padding(6.dp))
        }
        Text("SmartCatalog", style = MaterialTheme.typography.titleLarge)
    }
}
