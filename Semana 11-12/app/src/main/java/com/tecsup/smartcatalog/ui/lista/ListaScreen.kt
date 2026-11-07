package com.tecsup.smartcatalog.ui.lista

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.components.Cargando
import com.tecsup.smartcatalog.ui.components.ErrorView
import com.tecsup.smartcatalog.ui.components.EstadoVacio
import com.tecsup.smartcatalog.ui.components.ItemCard

@Composable
fun ListaScreen(viewModel: ListaViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    ListaContenido(estado, onReintentar = viewModel::reintentar, modifier = modifier)
}

@Composable
private fun ListaContenido(
    estado: UiState<List<Item>>,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { EncabezadoCatalogo() }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (estado) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorView(mensaje = estado.mensaje, onReintentar = onReintentar)
                is UiState.Exito -> {
                    if (estado.datos.isEmpty()) {
                        EstadoVacio(
                            titulo = "Sin smartphones por ahora",
                            descripcion = "Todavía no hay productos en esta colección."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item(key = "encabezado") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Nuestra colección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            "${estado.datos.size} modelos",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            items(estado.datos, key = { it.id }) { item -> ItemCard(item) }
                            item(key = "nota") {
                                Text(
                                    "Imágenes y precios ilustrativos",
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoCatalogo() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp)) {
                    Image(painterResource(R.drawable.ic_smartcatalog), contentDescription = null, modifier = Modifier.padding(5.dp))
                }
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(20.dp))
            Text("Explora nuestros smartphones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Colección de muestra · Precios en soles", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
