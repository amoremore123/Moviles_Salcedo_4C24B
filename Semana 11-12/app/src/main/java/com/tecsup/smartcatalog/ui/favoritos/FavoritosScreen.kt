package com.tecsup.smartcatalog.ui.favoritos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.tecsup.smartcatalog.R
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.components.CabeceraMarca
import com.tecsup.smartcatalog.ui.components.Cargando
import com.tecsup.smartcatalog.ui.components.ErrorView
import com.tecsup.smartcatalog.ui.components.EstadoVacio
import com.tecsup.smartcatalog.ui.components.ItemCard

@Composable
fun FavoritosScreen(viewModel: FavoritosViewModel, imageLoader: ImageLoader, onVolver: () -> Unit, onItemClick: (Int) -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val desplazamiento = rememberLazyListState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CabeceraMarca("Mis favoritos", navegacion = {
                IconButton(onClick = onVolver) {
                    Icon(painterResource(R.drawable.ic_volver), "Volver al catálogo")
                }
            })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.ic_favorito), contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp)
                    )
                    Column(Modifier.padding(start = 10.dp)) {
                        Text("Guardados en este dispositivo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Compartidos entre cuentas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            mensaje?.let { texto ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp)
                ) {
                    Text(texto, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                when (val actual = estado) {
                    UiState.Cargando -> Cargando(mensaje = "Cargando tus favoritos...")
                    is UiState.Error -> ErrorView(actual.mensaje, onReintentar = viewModel::reintentar)
                    is UiState.Exito -> if (actual.datos.isEmpty()) {
                        EstadoVacio("Todavía no tienes favoritos", "Toca el corazón de un smartphone para guardarlo aquí.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                            state = desplazamiento,
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(actual.datos, key = { it.id }) { item ->
                                ItemCard(item, imageLoader, onClick = { onItemClick(item.id) }, onAlternarFavorito = { viewModel.quitarFavorito(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
