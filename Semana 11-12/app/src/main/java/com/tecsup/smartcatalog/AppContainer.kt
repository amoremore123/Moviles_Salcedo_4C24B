package com.tecsup.smartcatalog

import android.content.Context
import coil.ImageLoader
import com.tecsup.smartcatalog.data.local.AppDatabase
import com.tecsup.smartcatalog.data.local.ItemDao
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.RetrofitClient
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.data.repository.ItemRepositoryImpl
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import com.tecsup.smartcatalog.ui.detalle.DetalleViewModel
import com.tecsup.smartcatalog.ui.favoritos.FavoritosViewModel

/** Inyección manual; construir el contenedor no ejecuta una consulta de productos. */
class AppContainer(
    context: Context,
    val apiService: ApiService = RetrofitClient.apiService,
    private val crearDatabase: (Context) -> AppDatabase = { AppDatabase.crear(it) }
) {
    private val applicationContext = context.applicationContext
    // La base se abre cuando el catálogo necesita el repositorio; siempre usa el contexto de aplicación.
    val database: AppDatabase by lazy { crearDatabase(applicationContext) }
    val itemDao: ItemDao by lazy { database.itemDao() }
    val itemRepository: ItemRepository by lazy { ItemRepositoryImpl(apiService, itemDao) }
    val listaViewModelFactory: ListaViewModel.Factory by lazy { ListaViewModel.Factory(itemRepository) }
    val favoritosViewModelFactory: FavoritosViewModel.Factory by lazy { FavoritosViewModel.Factory(itemRepository) }
    fun detalleViewModelFactory(id: Int): DetalleViewModel.Factory = DetalleViewModel.Factory(itemRepository, id)
    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(applicationContext).diskCache(null).build()
    }
}
