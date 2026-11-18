package com.tecsup.campusgps.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.campusgps.data.LugaresRepositoryLocal
import com.tecsup.campusgps.data.PreferenciasRepositoryDataStore
import com.tecsup.campusgps.data.UbicacionRepositoryGoogle

/**
 * Crea el MapaViewModel con sus repositorios.
 * El Context llega aquí y se entrega solo al Repository: el ViewModel nunca lo recibe.
 */
class CampusViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapaViewModel::class.java)) {
            return MapaViewModel(
                ubicacionRepo = UbicacionRepositoryGoogle(appContext),
                lugaresRepo = LugaresRepositoryLocal(),
                preferenciasRepo = PreferenciasRepositoryDataStore(appContext)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
