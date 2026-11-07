package com.tecsup.smartcatalog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun observarTodos(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE favorito = 1 ORDER BY id ASC")
    fun observarFavoritos(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): ItemEntity?

    /** Inserta nuevos IDs y actualiza sus datos sin sobrescribir favoritos de filas existentes. */
    @Transaction
    suspend fun insertarTodos(items: List<ItemEntity>) {
        val resultados = insertarNuevos(items)
        items.forEachIndexed { indice, item ->
            if (resultados[indice] == -1L) {
                actualizarDatos(item.id, item.nombre, item.descripcion, item.precio, item.imagen, item.categoria)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarNuevos(items: List<ItemEntity>): List<Long>

    // Deliberadamente no incluye favorito: ese estado se modifica solo con actualizarFavorito.
    @Query("UPDATE items SET nombre = :nombre, descripcion = :descripcion, precio = :precio, imagen = :imagen, categoria = :categoria WHERE id = :id")
    suspend fun actualizarDatos(id: Int, nombre: String, descripcion: String, precio: Double, imagen: String, categoria: String)

    @Query("UPDATE items SET favorito = :favorito WHERE id = :id")
    suspend fun actualizarFavorito(id: Int, favorito: Boolean)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun contarItems(): Int

    @Query("SELECT COUNT(*) FROM items WHERE favorito = 1")
    suspend fun contarFavoritos(): Int

    @Query("DELETE FROM items")
    suspend fun eliminarTodos()
}
