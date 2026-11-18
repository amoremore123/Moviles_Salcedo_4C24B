package com.tecsup.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.campusgps.data.LugaresRepository
import com.tecsup.campusgps.data.UbicacionNoDisponibleException
import com.tecsup.campusgps.data.UbicacionRepository
import com.tecsup.campusgps.model.Lugar
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.model.Posicion
import com.tecsup.campusgps.model.TipoError
import com.tecsup.campusgps.model.UbicacionUi
import com.tecsup.campusgps.util.distanciaMetros
import com.tecsup.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    // ---- Mejora 1: filtrar por categoría ----

    // Categorías disponibles, en el orden en que aparecen en el Repository
    val categorias: List<String> = lugaresRepo.obtenerLugares().map { it.categoria }.distinct()

    // null = sin filtro (se muestran todas)
    private val _categoria = MutableStateFlow<String?>(null)
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    // Lo que dibujan el mapa y la lista: los lugares ordenados, filtrados por categoría
    val lugaresVisibles: StateFlow<List<LugarConDistancia>> =
        combine(_lugares, _categoria) { lista, cat ->
            if (cat == null) lista else lista.filter { it.lugar.categoria == cat }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _lugares.value)

    // Tocar el chip activo lo desactiva (vuelve a "Todas")
    fun filtrarCategoria(cat: String?) {
        _categoria.value = if (cat == _categoria.value) null else cat
    }

    // ---- Mejora 2: radio de cercanía ----

    val radios: List<Int> = listOf(100, 200, 500)

    private val _radio = MutableStateFlow(200)
    val radio: StateFlow<Int> = _radio.asStateFlow()

    // Ids de los lugares cuya distancia ya calculada cae dentro del radio.
    // La comparación se hace aquí, no en el Composable.
    val idsDentroDelRadio: StateFlow<Set<Int>> =
        combine(_lugares, _radio) { lista, r ->
            lista.filter { it.metros != null && it.metros <= r }.map { it.lugar.id }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun cambiarRadio(metros: Int) {
        if (metros in radios) _radio.value = metros
    }

    fun buscar() {
        // Sin Play Services no hay FusedLocationProviderClient: no tiene sentido reintentar
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }

        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
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

    // Traduce la excepción del Repository a un error con mensaje y tipo para la pantalla
    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error(
            "Falta el permiso de ubicación",
            TipoError.SIN_PERMISO
        )
        is UbicacionNoDisponibleException -> UbicacionUi.Error(
            "Activa el GPS e intenta de nuevo",
            TipoError.GPS_APAGADO
        )
        else -> UbicacionUi.Error(
            e.message ?: "No se pudo obtener la ubicación",
            TipoError.OTRO
        )
    }

    private companion object {
        const val TAG = "CampusGPS"
    }
}
