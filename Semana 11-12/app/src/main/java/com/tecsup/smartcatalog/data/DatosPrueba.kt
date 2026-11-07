package com.tecsup.smartcatalog.data

import com.tecsup.smartcatalog.domain.model.Item

/** Datos ficticios para demostraciones de H4. No son resultados de DummyJSON. */
object DatosPrueba {
    // Ilustraciones locales; no requiere acceso a Internet.
    private const val RECURSOS = "android.resource://com.tecsup.smartcatalog/drawable/"

    val smartphones: List<Item> = listOf(
        Item(
            id = 1,
            nombre = "Aurora One",
            descripcion = "Smartphone ficticio con pantalla de 6,4 pulgadas y almacenamiento de 128 GB.",
            precio = 499.90,
            imagen = RECURSOS + "smartphone_aurora",
            categoria = "smartphones"
        ),
        Item(
            id = 2,
            nombre = "Andino Pro",
            descripcion = "Modelo ilustrativo con cámara triple y almacenamiento de 256 GB.",
            precio = 749.90,
            imagen = RECURSOS + "smartphone_andino",
            categoria = "smartphones"
        ),
        Item(
            id = 3,
            nombre = "Costa Lite",
            descripcion = "Smartphone ficticio compacto para tareas cotidianas, con 64 GB de almacenamiento.",
            precio = 299.90,
            imagen = RECURSOS + "smartphone_costa",
            categoria = "smartphones"
        ),
        Item(
            id = 4,
            nombre = "Sierra Max",
            descripcion = "Modelo ilustrativo de pantalla amplia, batería de larga duración y 128 GB.",
            precio = 599.90,
            imagen = RECURSOS + "smartphone_sierra",
            categoria = "smartphones"
        )
    )
}
