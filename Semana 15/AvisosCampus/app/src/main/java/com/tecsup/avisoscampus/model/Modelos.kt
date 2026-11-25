package com.tecsup.avisoscampus.model

// De dónde viene cada aviso (se muestra en el historial)
enum class Origen(val texto: String) {
    BOTON("Botón"),
    RECORDATORIO("Recordatorio"),
    MODO_AVION("Modo avión")
}

// Filtros del historial
enum class FiltroHistorial(val etiqueta: String) {
    TODOS("Todos"),
    AVISOS("Avisos"),
    RECORDATORIOS("Recordatorios")
}

// Íconos que el usuario puede elegir para su aviso
enum class IconoAviso(val etiqueta: String) {
    ESTRELLA("Estrella"),
    CORAZON("Corazón"),
    REGALO("Regalo"),
    CORONA("Corona"),
    FLOR("Flor")
}

// Un aviso que la app ya mostró como notificación
data class Aviso(
    val id: Int,
    val titulo: String,
    val texto: String,
    val origen: Origen,
    val hora: String,
    val urgente: Boolean = false, // true = va por el canal "Urgentes"
    val icono: IconoAviso = IconoAviso.ESTRELLA
)

// Estado del permiso POST_NOTIFICATIONS visto desde la UI
sealed interface PermisoUi {
    data object SinPedir : PermisoUi
    data object Concedido : PermisoUi
    data object Denegado : PermisoUi
}
