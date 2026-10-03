package com.tecsup.mibodega.ui.cliente.screens.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBasket
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.mibodega.ui.cliente.modelo.Producto
import com.tecsup.mibodega.ui.cliente.modelo.listaProductosFake
import com.tecsup.mibodega.ui.componentes.BotonPrimario
import com.tecsup.mibodega.ui.componentes.SelectorCantidad
import com.tecsup.mibodega.ui.theme.BodegaTheme
import com.tecsup.mibodega.ui.theme.GrisClaro
import com.tecsup.mibodega.ui.theme.RojoPrecio
import com.tecsup.mibodega.ui.theme.VerdeBodega

/**
 * Pantalla 4: Detalle del producto (mockup "Cliente").
 * Guarda su propia cantidad seleccionada (remember) mientras el usuario
 * decide cuánto quiere; solo al tocar "Agregar al carrito" le avisa
 * a ClienteApp cuánto agregar.
 */
@Composable
fun DetalleProductoScreen(
    producto: Producto,
    onVolver: () -> Unit,
    onAgregarAlCarrito: (Producto, Int) -> Unit
) {
    var cantidad by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        EncabezadoDetalle(onVolver = onVolver)

        ImagenProducto()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "S/ %.2f".format(producto.precio),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 26.sp),
                color = RojoPrecio
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            SelectorCantidad(
                cantidad = cantidad,
                onIncrementar = { cantidad++ },
                onDecrementar = { if (cantidad > 1) cantidad-- }
            )

            Spacer(Modifier.weight(1f))

            BotonPrimario(
                texto = "Agregar al carrito",
                onClick = { onAgregarAlCarrito(producto, cantidad) }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EncabezadoDetalle(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
        }
        IconButton(onClick = { /* TODO: guardar como favorito */ }) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorito")
        }
    }
}

@Composable
private fun ImagenProducto() {
    // Placeholder de imagen: reemplázalo por Image(painterResource(...))
    // cuando tengan la foto real de cada producto.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.4f)
            .background(GrisClaro),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBasket,
            contentDescription = null,
            tint = VerdeBodega,
            modifier = Modifier.size(80.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleProductoPreview() {
    BodegaTheme {
        DetalleProductoScreen(
            producto = listaProductosFake.first { it.nombre == "Coca-Cola Original" },
            onVolver = {},
            onAgregarAlCarrito = { _, _ -> }
        )
    }
}

