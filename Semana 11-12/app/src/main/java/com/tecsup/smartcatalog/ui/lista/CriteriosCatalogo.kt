package com.tecsup.smartcatalog.ui.lista

import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import java.text.Collator
import java.util.Locale

internal enum class OrdenCatalogo(val etiqueta: String) {
    ORIGINAL("Orden original"),
    PRECIO_ASC("Precio: menor a mayor"),
    PRECIO_DESC("Precio: mayor a menor"),
    NOMBRE_ASC("Nombre: A–Z"),
    NOMBRE_DESC("Nombre: Z–A")
}

internal data class CriteriosCatalogo(
    val busqueda: String = "",
    val precioMinimo: String = "",
    val precioMaximo: String = "",
    val soloFavoritos: Boolean = false,
    val orden: OrdenCatalogo = OrdenCatalogo.ORIGINAL
) {
    private val minimo = convertirPrecio(precioMinimo)
    private val maximo = convertirPrecio(precioMaximo)
    val errorPrecio = when {
        (precioMinimo.isNotBlank() && minimo == null) || (precioMaximo.isNotBlank() && maximo == null) ->
            "Ingresa precios válidos, mayores o iguales a cero."
        minimo != null && maximo != null && minimo > maximo ->
            "El precio mínimo debe ser menor o igual al máximo."
        else -> null
    }

    fun aplicar(productos: List<Item>): List<Item> {
        if (errorPrecio != null) return emptyList()
        val consulta = normalizar(busqueda)
        val coincidencias = productos.filter { item ->
            (consulta.isEmpty() || normalizar(item.nombre).contains(consulta)) &&
                (minimo == null || item.precio >= minimo) &&
                (maximo == null || item.precio <= maximo) &&
                (!soloFavoritos || item.favorito)
        }
        return when (orden) {
            OrdenCatalogo.ORIGINAL -> coincidencias
            OrdenCatalogo.PRECIO_ASC -> coincidencias.sortedBy { it.precio }
            OrdenCatalogo.PRECIO_DESC -> coincidencias.sortedByDescending { it.precio }
            OrdenCatalogo.NOMBRE_ASC -> coincidencias.sortedWith(compararNombres())
            OrdenCatalogo.NOMBRE_DESC -> coincidencias.sortedWith(compararNombres().reversed())
        }
    }
}

internal data class VistaCatalogo(
    val estado: UiState<List<Item>>,
    val criterios: CriteriosCatalogo,
    val coincidencias: List<Item>
)

private val espacios = Regex("\\s+")
private val precioDecimal = Regex("[0-9]+([.,][0-9]+)?")

private fun normalizar(texto: String): String = texto.trim().replace(espacios, " ").lowercase(Locale.ROOT)

private fun convertirPrecio(texto: String): Double? {
    val limpio = texto.trim()
    return limpio.takeIf(precioDecimal::matches)?.replace(',', '.')?.toDoubleOrNull()?.takeIf(Double::isFinite)
}

private fun compararNombres(): Comparator<Item> {
    val comparador = Collator.getInstance(Locale.forLanguageTag("es-PE")).apply { strength = Collator.PRIMARY }
    return Comparator { primero, segundo -> comparador.compare(normalizar(primero.nombre), normalizar(segundo.nombre)) }
}
