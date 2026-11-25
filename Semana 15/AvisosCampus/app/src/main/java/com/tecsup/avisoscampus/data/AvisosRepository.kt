package com.tecsup.avisoscampus.data

import com.tecsup.avisoscampus.model.Aviso
import com.tecsup.avisoscampus.model.Origen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface AvisosRepository {
    val avisos: StateFlow<List<Aviso>>
    fun registrar(titulo: String, texto: String, origen: Origen): Aviso
}

// Guarda los avisos solo en memoria (se pierden al cerrar la app)
class AvisosRepositoryMemoria : AvisosRepository {

    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    override val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    private val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var siguienteId = 1

    override fun registrar(titulo: String, texto: String, origen: Origen): Aviso {
        val aviso = Aviso(
            id = siguienteId++,
            titulo = titulo,
            texto = texto,
            origen = origen,
            hora = formatoHora.format(Date())
        )
        // Lo más reciente arriba
        _avisos.update { listOf(aviso) + it }
        return aviso
    }
}
