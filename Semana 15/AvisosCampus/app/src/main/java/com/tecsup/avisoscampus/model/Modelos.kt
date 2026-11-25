package com.tecsup.avisoscampus.model

// De dónde viene cada aviso (se muestra en el historial)
enum class Origen(val texto: String) {
    BOTON("Botón"),
    RECORDATORIO("Recordatorio"),
    MODO_AVION("Modo avión")
}

// Un aviso que la app ya mostró como notificación
data class Aviso(
    val id: Int,
    val titulo: String,
    val texto: String,
    val origen: Origen,
    val hora: String,
    val urgente: Boolean = false // true = va por el canal "Urgentes"
)

// Estado del permiso POST_NOTIFICATIONS visto desde la UI
sealed interface PermisoUi {
    data object SinPedir : PermisoUi
    data object Concedido : PermisoUi
    data object Denegado : PermisoUi
}
