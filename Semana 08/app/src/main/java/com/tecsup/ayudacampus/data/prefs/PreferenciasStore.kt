package com.tecsup.ayudacampus.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "ajustes")

class PreferenciasStore(context: Context) {
    private val store = context.applicationContext.dataStore
    private val preferencias = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val perfilActivo: Flow<String> = preferencias.map { it[PERFIL_ACTIVO] ?: "" }
    val usuarioId: Flow<Int> = preferencias.map { it[USUARIO_ID] ?: 0 }
    val modoOscuro: Flow<Boolean> = preferencias.map { it[MODO_OSCURO] ?: false }
    val notificaciones: Flow<Boolean> = preferencias.map { it[NOTIFICACIONES] ?: true }
    val recibirTickets: Flow<Boolean> = preferencias.map { it[RECIBIR_TICKETS] ?: true }

    suspend fun guardarSesion(rol: String, usuarioId: Int) {
        store.edit {
            it[PERFIL_ACTIVO] = rol
            it[USUARIO_ID] = usuarioId
        }
    }

    suspend fun guardarModoOscuro(b: Boolean) {
        store.edit { it[MODO_OSCURO] = b }
    }

    suspend fun guardarNotificaciones(b: Boolean) {
        store.edit { it[NOTIFICACIONES] = b }
    }

    suspend fun guardarRecibirTickets(b: Boolean) {
        store.edit { it[RECIBIR_TICKETS] = b }
    }

    suspend fun cerrarSesion() {
        store.edit {
            it.remove(PERFIL_ACTIVO)
            it.remove(USUARIO_ID)
        }
    }

    private companion object {
        val PERFIL_ACTIVO = stringPreferencesKey("perfilActivo")
        val USUARIO_ID = intPreferencesKey("usuarioId")
        val MODO_OSCURO = booleanPreferencesKey("modoOscuro")
        val NOTIFICACIONES = booleanPreferencesKey("notificaciones")
        val RECIBIR_TICKETS = booleanPreferencesKey("recibirTickets")
    }
}
