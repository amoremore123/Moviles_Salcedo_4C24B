package com.tecsup.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.campusgps.data.LugaresRepository
import com.tecsup.campusgps.data.UbicacionRepository
import com.tecsup.campusgps.model.Lugar
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.model.Posicion
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.util.distanciaMetros
import com.tecsup.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    fun buscar() {
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = UbicacionUi.Error(mensajeDe(it)) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error("Sin permiso de ubicación no puedo mostrar dónde estás")
    }

    // Calcula la distancia a cada lugar y publica la lista ordenada (más cercano primero)
    private fun actualizarDistancias(pos: Posicion) {
        val ordenados = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(pos, it)) }
            .sortedBy { it.metros }
        _lugares.value = ordenados

        ordenados.forEach {
            Log.d(TAG, "${it.lugar.nombre}: ${formatearDistancia(it.metros ?: 0f)}")
        }
    }

    // La pantalla avisa qué lugar tocó el usuario; null = ninguno
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }
    // Se deriva de lugares: el primero con distancia calculada (la lista ya está ordenada).
    // Es null mientras todavía no hay ubicación.
    val masCercano: StateFlow<Lugar?> = lugares
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun mensajeDe(e: Throwable): String = when (e) {
        is SecurityException -> "Falta el permiso de ubicación"
        else -> e.message ?: "No se pudo obtener la ubicación"
    }

    private companion object {
        const val TAG = "CampusGPS"
    }
}
