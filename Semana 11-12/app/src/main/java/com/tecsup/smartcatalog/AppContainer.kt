package com.tecsup.smartcatalog

import android.content.Context
import coil.ImageLoader
import com.tecsup.smartcatalog.data.local.AppDatabase
import com.tecsup.smartcatalog.data.local.ItemDao
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.RetrofitClient
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.data.repository.ItemRepositoryRemoto
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import com.tecsup.smartcatalog.ui.detalle.DetalleViewModel

/** Inyección manual; construir el contenedor no ejecuta una consulta de productos. */
class AppContainer(context: Context, val apiService: ApiService = RetrofitClient.apiService) {
    private val applicationContext = context.applicationContext
    // H8 solo prepara Room: el catálogo sigue utilizando exclusivamente ItemRepositoryRemoto.
    val database: AppDatabase by lazy { AppDatabase.crear(applicationContext) }
    val itemDao: ItemDao by lazy { database.itemDao() }
    val itemRepository: ItemRepository by lazy { ItemRepositoryRemoto(apiService) }
    val listaViewModelFactory: ListaViewModel.Factory by lazy { ListaViewModel.Factory(itemRepository) }
    fun detalleViewModelFactory(id: Int): DetalleViewModel.Factory = DetalleViewModel.Factory(itemRepository, id)
    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(applicationContext).diskCache(null).build()
    }
}
