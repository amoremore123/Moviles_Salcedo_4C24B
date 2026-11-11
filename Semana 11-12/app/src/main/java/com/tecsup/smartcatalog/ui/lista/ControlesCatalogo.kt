package com.tecsup.smartcatalog.ui.lista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ControlesCatalogo(criterios: CriteriosCatalogo, cambiar: ((CriteriosCatalogo) -> CriteriosCatalogo) -> Unit) {
    var verFiltros by rememberSaveable { mutableStateOf(false) }
    var verOrden by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = criterios.busqueda,
            onValueChange = { valor -> cambiar { it.copy(busqueda = valor) } },
            label = { Text("Buscar por nombre") },
            modifier = Modifier.fillMaxWidth().testTag("buscar-catalogo"),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            trailingIcon = if (criterios.busqueda.isNotEmpty()) {
                { TextButton(onClick = { cambiar { it.copy(busqueda = "") } }) { Text("Borrar") } }
            } else null
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = verFiltros, onClick = { verFiltros = !verFiltros }, label = { Text("Filtros") })
            Box {
                OutlinedButton(onClick = { verOrden = true }) { Text(criterios.orden.etiqueta) }
                DropdownMenu(expanded = verOrden, onDismissRequest = { verOrden = false }) {
                    OrdenCatalogo.entries.forEach { orden ->
                        DropdownMenuItem(
                            text = { Text(orden.etiqueta) },
                            onClick = {
                                cambiar { it.copy(orden = orden) }
                                verOrden = false
                            }
                        )
                    }
                }
            }
        }
        if (verFiltros) {
            OutlinedCard {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoPrecio(
                            criterios.precioMinimo, "Precio mínimo", criterios.errorPrecio != null,
                            Modifier.weight(1f).testTag("precio-minimo")
                        ) { valor -> cambiar { it.copy(precioMinimo = valor) } }
                        CampoPrecio(
                            criterios.precioMaximo, "Precio máximo", criterios.errorPrecio != null,
                            Modifier.weight(1f).testTag("precio-maximo")
                        ) { valor -> cambiar { it.copy(precioMaximo = valor) } }
                    }
                    FilterChip(
                        selected = criterios.soloFavoritos,
                        onClick = { cambiar { it.copy(soloFavoritos = !it.soloFavoritos) } },
                        label = { Text("Solo favoritos") }
                    )
                }
            }
        }
        criterios.errorPrecio?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        if (criterios != CriteriosCatalogo()) {
            TextButton(onClick = { cambiar { CriteriosCatalogo() } }) { Text("Limpiar filtros") }
        }
    }
}

@Composable
private fun CampoPrecio(valor: String, etiqueta: String, error: Boolean, modifier: Modifier, cambiar: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = cambiar,
        label = { Text(etiqueta) },
        modifier = modifier,
        singleLine = true,
        isError = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}
