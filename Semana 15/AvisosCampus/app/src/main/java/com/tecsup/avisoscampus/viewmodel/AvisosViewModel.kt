package com.tecsup.avisoscampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.avisoscampus.data.AvisosRepository
import com.tecsup.avisoscampus.model.Aviso
import com.tecsup.avisoscampus.model.Origen
import com.tecsup.avisoscampus.model.PermisoUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// El ViewModel no conoce Context, NotificationManager ni BroadcastReceiver:
// solo decide qué aviso emitir; la UI se encarga de mostrarlo.
class AvisosViewModel(
    private val repo: AvisosRepository
) : ViewModel() {

    // Historial de avisos (viene del repositorio)
    val avisos: StateFlow<List<Aviso>> = repo.avisos

    // Avisos que la UI debe convertir en notificación (eventos de una sola vez)
    private val _eventos = MutableSharedFlow<Aviso>()
    val eventos: SharedFlow<Aviso> = _eventos.asSharedFlow()

    // Estado del permiso de notificaciones
    private val _permiso = MutableStateFlow<PermisoUi>(PermisoUi.SinPedir)
    val permiso: StateFlow<PermisoUi> = _permiso.asStateFlow()

    // Mensaje corto para la UI (por ejemplo, avisar que falta el permiso)
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // La UI pide el permiso y solo nos avisa el resultado (true = concedido)
    fun permisoRespondido(concedido: Boolean) {
        _permiso.value = if (concedido) PermisoUi.Concedido else PermisoUi.Denegado
    }

    // Botón "Mostrar aviso": guarda el aviso y le pide a la UI que lo notifique
    fun avisarAhora() {
        viewModelScope.launch {
            val aviso = repo.registrar(
                titulo = "Tarea lista",
                texto = "Tu entrega fue registrada",
                origen = Origen.BOTON
            )
            _eventos.emit(aviso)
        }
    }

    // Corrutina del recordatorio activo (null si nunca se programó)
    private var trabajo: Job? = null

    // Segundos que faltan para el recordatorio; null = no hay recordatorio activo
    private val _faltan = MutableStateFlow<Int?>(null)
    val faltan: StateFlow<Int?> = _faltan.asStateFlow()

    // Vive en viewModelScope: sigue contando al rotar o minimizar la app
    fun recordarEn(segundos: Int = 10) {
        if (trabajo?.isActive == true) return // ya hay uno en curso

        trabajo = viewModelScope.launch {
            try {
                for (s in segundos downTo 1) {
                    _faltan.value = s
                    delay(1000)
                }
                val aviso = repo.registrar(
                    titulo = "Recordatorio",
                    texto = "Revisa tu laboratorio pendiente",
                    origen = Origen.RECORDATORIO
                )
                _eventos.emit(aviso)
            } finally {
                // Se ejecuta al terminar y también al cancelar
                _faltan.value = null
            }
        }
    }

    // Cancelar interrumpe el delay: no se registra ni se emite ningún aviso
    fun cancelarRecordatorio() {
        trabajo?.cancel()
    }

    // TODO Paso 6: registrar el aviso que llega desde el receptor de modo avión

    // TODO Paso 8: limpiar el mensaje cuando la UI ya lo mostró
}
