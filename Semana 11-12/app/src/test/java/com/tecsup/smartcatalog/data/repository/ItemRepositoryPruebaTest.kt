package com.tecsup.smartcatalog.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemRepositoryPruebaTest {
    @Test
    fun coleccionInicialTieneCuatroIdentificadoresUnicos() = runTest {
        val repository = ItemRepositoryPrueba()
        val items = repository.items.first()
        assertEquals(4, items.size)
        assertEquals(4, items.map { it.id }.distinct().size)
        assertEquals(listOf("Aurora One", "Andino Pro", "Costa Lite", "Sierra Max"), items.map { it.nombre })
        assertEquals(listOf(499.90, 749.90, 299.90, 599.90), items.map { it.precio })
    }

    @Test
    fun detalleInexistenteDevuelveFalloSinLanzarExcepcion() = runTest {
        assertTrue(ItemRepositoryPrueba().obtenerDetalle(-1).isFailure)
    }

    @Test
    fun vaciarYRecargarOperaSoloSobreLaColeccionEnMemoria() = runTest {
        val repository = ItemRepositoryPrueba()
        repository.limpiarCache()
        assertTrue(repository.items.first().isEmpty())
        assertEquals(0, repository.cantidadGuardada())
        assertEquals(4, repository.refrescar().getOrThrow())
        assertEquals(4, repository.items.first().size)
    }
}
