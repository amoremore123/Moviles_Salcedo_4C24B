package com.tecsup.smartcatalog.ui.lista

import com.tecsup.smartcatalog.domain.model.Item
import org.junit.Assert.*
import org.junit.Test

class CriteriosCatalogoTest {
    private val productos = listOf(
        producto(1, "Samsung Galaxy A", 350.5, true),
        producto(2, "Pixel", 399.0, true),
        producto(3, "SAMSUNG   Galaxy B", 150.0, false),
        producto(4, "Samsung Galaxy C", 350.5, true)
    )

    @Test fun buscaNombreExactoParcialYNormalizado() {
        assertEquals(listOf(2), ids(CriteriosCatalogo(busqueda = "Pixel")))
        assertEquals(listOf(1, 3, 4), ids(CriteriosCatalogo(busqueda = "  SAMSUNG   galaxy ")))
        assertTrue(ids(CriteriosCatalogo(busqueda = "otro")).isEmpty())
    }

    @Test fun combinaRangoInclusivoNombreFavoritosYOrden() {
        val criterios = CriteriosCatalogo("Galaxy", "350,50", "350.5", true, OrdenCatalogo.NOMBRE_DESC)
        assertEquals(listOf(4, 1), ids(criterios))
        assertEquals(listOf(1, 2, 4), ids(CriteriosCatalogo(soloFavoritos = true)))
    }

    @Test fun ordenaDeFormaEstableYLimpiarRecuperaElOrdenDeRoom() {
        assertEquals(listOf(3, 1, 4, 2), ids(CriteriosCatalogo(orden = OrdenCatalogo.PRECIO_ASC)))
        assertEquals(listOf(2, 1, 4, 3), ids(CriteriosCatalogo(orden = OrdenCatalogo.PRECIO_DESC)))
        assertEquals(listOf(2, 1, 3, 4), ids(CriteriosCatalogo(orden = OrdenCatalogo.NOMBRE_ASC)))
        assertEquals(listOf(4, 3, 1, 2), ids(CriteriosCatalogo(orden = OrdenCatalogo.NOMBRE_DESC)))
        assertEquals(listOf(1, 2, 3, 4), ids(CriteriosCatalogo()))
    }

    @Test fun diferenciaRangoInvalidoDeListaVacia() {
        assertNotNull(CriteriosCatalogo(precioMinimo = "-1").errorPrecio)
        assertNotNull(CriteriosCatalogo(precioMaximo = "∞").errorPrecio)
        val invertido = CriteriosCatalogo(precioMinimo = "400", precioMaximo = "300")
        assertNotNull(invertido.errorPrecio)
        assertTrue(invertido.aplicar(productos).isEmpty())
        assertTrue(CriteriosCatalogo().aplicar(emptyList()).isEmpty())
    }

    private fun ids(criterios: CriteriosCatalogo) = criterios.aplicar(productos).map(Item::id)

    private fun producto(id: Int, nombre: String, precio: Double, favorito: Boolean) =
        Item(id, nombre, "", precio, "", "smartphones", favorito)
}
