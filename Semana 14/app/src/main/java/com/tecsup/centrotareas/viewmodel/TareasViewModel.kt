package com.tecsup.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.centrotareas.data.TareasRepository
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    // ----- Tarea con progreso (pasos 1 a 4)
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    // Paso 3: avance de 0f a 1f; la pantalla solo ve la versión de lectura
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    // Paso 4: Job de la tarea y si está corriendo
    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    // Paso 2: la espera corre en segundo plano; el hilo principal queda libre
    fun tareaLarga() {
        if (_corriendo.value) return // evita lanzar dos tareas a la vez
        _corriendo.value = true
        trabajo = viewModelScope.launch {
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                withContext(Dispatchers.Default) {
                    Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
                    // 100 vueltas x 40 ms = unos 4 segundos
                    for (i in 1..100) {
                        delay(40) // delay revisa la cancelación
                        _progreso.value = i / 100f
                    }
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                // Se ejecuta termine bien o cancelada
                _corriendo.value = false
            }
        }
    }

    // Paso 4: detiene la tarea; la barra se queda donde estaba
    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    // ----- Contar primos (paso 5)
    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()
    private var trabajoPrimos: Job? = null

    // Paso 5: el cálculo pesado lo hace el Repository; aquí solo se mide y se publica el estado
    fun contar(hasta: Int) {
        if (_primos.value is PrimosUi.Calculando) return
        _primos.value = PrimosUi.Calculando
        trabajoPrimos = viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                val cantidad = repo.contarPrimos(hasta)
                _primos.value = PrimosUi.Listo(cantidad, System.currentTimeMillis() - inicio)
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e // se relanza para no romper la cancelación
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    // ----- Dos cargas (pasos 6 y 7)
    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    // Paso 6: primero el perfil y después los pedidos (2 s + 3 s ≈ 5 s)
    fun cargarUnaPorUna() {
        if (_carga.value is CargaUi.Cargando) return
        _carga.value = CargaUi.Cargando
        viewModelScope.launch {
            try {
                val inicio = System.currentTimeMillis()
                val perfil = repo.cargarPerfil()
                val pedidos = repo.cargarPedidos(_simularError.value)
                _carga.value = CargaUi.Exito(
                    "Una por una", perfil, pedidos.size, System.currentTimeMillis() - inicio
                )
            } catch (e: CancellationException) {
                throw e // la cancelación no es un error: se deja pasar
            } catch (e: Exception) {
                _carga.value = CargaUi.Error("No se pudo cargar: ${e.message}")
            }
        }
    }

    // Paso 6: las dos cargas al mismo tiempo (máximo(2 s, 3 s) ≈ 3 s)
    fun cargarALaVez() {
        if (_carga.value is CargaUi.Cargando) return
        _carga.value = CargaUi.Cargando
        viewModelScope.launch {
            try {
                val inicio = System.currentTimeMillis()
                val (perfil, pedidos) = coroutineScope {
                    val a = async { repo.cargarPerfil() }
                    val b = async { repo.cargarPedidos(_simularError.value) }
                    Pair(a.await(), b.await())
                }
                _carga.value = CargaUi.Exito(
                    "A la vez", perfil, pedidos.size, System.currentTimeMillis() - inicio
                )
            } catch (e: CancellationException) {
                throw e // la cancelación no es un error: se deja pasar
            } catch (e: Exception) {
                // Si una carga falla, coroutineScope cancela la otra y lanza el error aquí
                _carga.value = CargaUi.Error("No se pudo cargar: ${e.message}")
            }
        }
    }

    // Paso 7: interruptor para que los pedidos fallen con IOException
    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }
}
