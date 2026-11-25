package com.tecsup.avisoscampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.avisoscampus.data.AvisosRepository
import com.tecsup.avisoscampus.model.Aviso
import com.tecsup.avisoscampus.model.FiltroHistorial
import com.tecsup.avisoscampus.model.IconoAviso
import com.tecsup.avisoscampus.model.Origen
import com.tecsup.avisoscampus.model.PermisoUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// El ViewModel no conoce Context, NotificationManager ni BroadcastReceiver:
// solo decide qué aviso emitir; la UI se encarga de mostrarlo.
class AvisosViewModel(
    private val repo: AvisosRepository
) : ViewModel() {

    // Historial de avisos (viene del repositorio)
    val avisos: StateFlow<List<Aviso>> = repo.avisos

    // Filtro elegido en el historial
    private val _filtro = MutableStateFlow(FiltroHistorial.TODOS)
    val filtro: StateFlow<FiltroHistorial> = _filtro.asStateFlow()

    // Historial ya filtrado: se recalcula si cambia la lista o el filtro
    val avisosFiltrados: StateFlow<List<Aviso>> =
        combine(repo.avisos, _filtro) { lista, filtro ->
            when (filtro) {
                FiltroHistorial.TODOS -> lista
                FiltroHistorial.AVISOS -> lista.filter {
                    it.origen == Origen.BOTON || it.origen == Origen.MODO_AVION
                }
                FiltroHistorial.RECORDATORIOS -> lista.filter {
                    it.origen == Origen.RECORDATORIO
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cambiarFiltro(filtro: FiltroHistorial) {
        _filtro.value = filtro
    }

    fun borrarHistorial() {
        repo.borrarTodos()
    }

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
        // Con el permiso ya concedido, el aviso de error deja de tener sentido
        if (concedido) _mensaje.value = null
    }

    // Mensaje que escribe el usuario para su aviso (máximo 100 caracteres)
    private val _mensajeAviso = MutableStateFlow("¡Hola!")
    val mensajeAviso: StateFlow<String> = _mensajeAviso.asStateFlow()

    fun cambiarMensajeAviso(texto: String) {
        _mensajeAviso.value = texto.take(100)
    }

    // Ícono elegido para el aviso
    private val _iconoAviso = MutableStateFlow(IconoAviso.ESTRELLA)
    val iconoAviso: StateFlow<IconoAviso> = _iconoAviso.asStateFlow()

    fun cambiarIcono(icono: IconoAviso) {
        _iconoAviso.value = icono
    }

    // Botón "Mostrar aviso": guarda el aviso y le pide a la UI que lo notifique
    fun avisarAhora() {
        viewModelScope.launch {
            val texto = _mensajeAviso.value.ifBlank { "Tu entrega fue registrada" }
            val aviso = repo.registrar(
                titulo = "Tarea lista",
                texto = texto,
                origen = Origen.BOTON,
                icono = _iconoAviso.value
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
                    texto = "Revisa tu laboratorio pendiente: faltan los pasos de permiso, " +
                        "recordatorio y receptor del modo avión, y recuerda subir las " +
                        "capturas al README antes de la fecha límite.",
                    origen = Origen.RECORDATORIO,
                    urgente = true // va por el canal "Urgentes"
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

    // Si la UI debe tener registrado el receptor del modo avión
    private val _escuchandoAvion = MutableStateFlow(false)
    val escuchandoAvion: StateFlow<Boolean> = _escuchandoAvion.asStateFlow()

    fun cambiarEscucha(valor: Boolean) {
        _escuchandoAvion.value = valor
    }

    // El receptor solo nos pasa el estado; aquí se decide qué aviso generar
    fun alCambiarModoAvion(activo: Boolean) {
        viewModelScope.launch {
            val aviso = repo.registrar(
                titulo = "Modo avión",
                texto = if (activo) "Activado" else "Desactivado",
                origen = Origen.MODO_AVION
            )
            _eventos.emit(aviso)
        }
    }

    // La UI avisa que no pudo notificar (falta el permiso); el aviso ya está en el historial
    fun noSePudoMostrar() {
        _mensaje.value = "Sin permiso: el aviso se guardó en el historial, pero no se mostró"
    }
}
