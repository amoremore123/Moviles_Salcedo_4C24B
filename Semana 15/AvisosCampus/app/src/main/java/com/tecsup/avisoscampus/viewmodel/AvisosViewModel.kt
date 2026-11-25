package com.tecsup.avisoscampus.viewmodel

import androidx.lifecycle.ViewModel
import com.tecsup.avisoscampus.data.AvisosRepository
import com.tecsup.avisoscampus.model.Aviso
import com.tecsup.avisoscampus.model.PermisoUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

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

    // TODO Paso 2: actualizar el permiso según la respuesta del usuario

    // TODO Paso 3: enviar un aviso al pulsar el botón (registrar + emitir evento)

    // TODO Paso 5: programar el recordatorio con una espera en viewModelScope

    // TODO Paso 6: registrar el aviso que llega desde el receptor de modo avión

    // TODO Paso 8: limpiar el mensaje cuando la UI ya lo mostró
}
