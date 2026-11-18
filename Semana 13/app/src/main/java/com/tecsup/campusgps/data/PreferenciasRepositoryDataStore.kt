package com.tecsup.campusgps.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Una sola instancia de DataStore por archivo (regla de DataStore)
private val Context.preferenciasCampus: DataStore<Preferences> by preferencesDataStore("preferencias_campus")

/**
 * Implementación con DataStore Preferences.
 * Igual que con la ubicación, el Context se queda en la capa de datos.
 */
class PreferenciasRepositoryDataStore(context: Context) : PreferenciasRepository {

    private val dataStore = context.applicationContext.preferenciasCampus

    override val ultimoLugar: Flow<Int?> = dataStore.data.map { it[CLAVE_ULTIMO_LUGAR] }

    override suspend fun guardarUltimoLugar(id: Int) {
        dataStore.edit { it[CLAVE_ULTIMO_LUGAR] = id }
    }

    private companion object {
        val CLAVE_ULTIMO_LUGAR = intPreferencesKey("ultimo_lugar_id")
    }
}
