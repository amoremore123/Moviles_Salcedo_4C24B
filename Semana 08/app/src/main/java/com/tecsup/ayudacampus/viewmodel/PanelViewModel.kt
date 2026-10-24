package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.model.DatoGrafico
import com.tecsup.ayudacampus.model.EstadoTicket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PanelViewModel(
    repository: AyudaRepository,
    sesion: StateFlow<Usuario?>,
    private val ahora: () -> Long = System::currentTimeMillis,
    private val intervaloActualizacionMs: Long = 60_000L
) : BandejaViewModel(repository, sesion) {
    data class Indicadores(
        val abiertos: Int = 0, val enProceso: Int = 0, val resueltos: Int = 0, val cerrados: Int = 0,
        val porEstado: List<DatoGrafico> = emptyList(), val porCategoria: List<DatoGrafico> = emptyList(),
        val altasSinAtender: Int = 0
    )
    private val _indicadores = MutableStateFlow(Indicadores())
    val indicadores: StateFlow<Indicadores> = _indicadores.asStateFlow()
    private val _cargandoIndicadores = MutableStateFlow(true)
    val cargandoIndicadores: StateFlow<Boolean> = _cargandoIndicadores.asStateFlow()
    private val _errorIndicadores = MutableStateFlow<String?>(null)
    val errorIndicadores: StateFlow<String?> = _errorIndicadores.asStateFlow()
    val abiertos = indicadores.map { it.abiertos }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val enProceso = indicadores.map { it.enProceso }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val resueltos = indicadores.map { it.resueltos }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val cerrados = indicadores.map { it.cerrados }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val porEstado = indicadores.map { it.porEstado }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val porCategoria = indicadores.map { it.porCategoria }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val altasSinAtender = indicadores.map { it.altasSinAtender }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        require(intervaloActualizacionMs > 0)
        viewModelScope.launch {
            administrador.collectLatest { usuario ->
                _indicadores.value = Indicadores()
                _errorIndicadores.value = null
                _cargandoIndicadores.value = usuario != null
                if (usuario == null) return@collectLatest
                try {
                    val conteos = combine(
                        repository.observarCantidadPorEstado(EstadoTicket.ABIERTO),
                        repository.observarCantidadPorEstado(EstadoTicket.EN_PROCESO),
                        repository.observarCantidadPorEstado(EstadoTicket.RESUELTO),
                        repository.observarCantidadPorEstado(EstadoTicket.CERRADO)
                    ) { a, e, r, c -> listOf(a, e, r, c) }
                    // Renueva el limite aunque Room no cambie: los tickets envejecen con el reloj.
                    val alertas = flow {
                        while (true) {
                            emit(ahora() - 3_600_000L)
                            delay(intervaloActualizacionMs)
                        }
                    }.flatMapLatest { limite -> repository.observarAltasSinAtender(limite) }
                    combine(conteos, repository.observarCantidadPorCategoria(), alertas) { numeros, categorias, altas ->
                        Indicadores(numeros[0], numeros[1], numeros[2], numeros[3],
                            listOf("Abiertos", "En proceso", "Resueltos", "Cerrados").zip(numeros) { etiqueta, cantidad -> DatoGrafico(etiqueta, cantidad) },
                            categorias.map { DatoGrafico(it.categoria, it.cantidad) }, altas)
                    }.collect { datos ->
                        _indicadores.value = if (mismaSesion(usuario)) datos else Indicadores()
                        _cargandoIndicadores.value = false
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _indicadores.value = Indicadores()
                    _errorIndicadores.value = "No se pudieron cargar los indicadores"
                    _cargandoIndicadores.value = false
                }
            }
        }
    }
    class Factory(private val repository: AyudaRepository, private val sesion: StateFlow<Usuario?>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PanelViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return PanelViewModel(repository, sesion) as T
        }
    }
}
