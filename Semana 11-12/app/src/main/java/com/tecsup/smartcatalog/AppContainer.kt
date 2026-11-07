package com.tecsup.smartcatalog

import android.content.Context
import coil.ImageLoader
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.RetrofitClient
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.data.repository.ItemRepositoryRemoto
import com.tecsup.smartcatalog.ui.lista.ListaViewModel

/** Inyección manual; construir el contenedor no ejecuta una consulta de productos. */
class AppContainer(context: Context, val apiService: ApiService = RetrofitClient.apiService) {
    private val applicationContext = context.applicationContext
    val itemRepository: ItemRepository by lazy { ItemRepositoryRemoto(apiService) }
    val listaViewModelFactory: ListaViewModel.Factory by lazy { ListaViewModel.Factory(itemRepository) }
    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(applicationContext).diskCache(null).build()
    }
}
