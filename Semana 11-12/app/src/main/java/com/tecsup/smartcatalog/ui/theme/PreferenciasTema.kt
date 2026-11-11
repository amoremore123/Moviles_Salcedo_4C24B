package com.tecsup.smartcatalog.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TemaSmartCatalog(val valor: String, val etiqueta: String) {
    SISTEMA("sistema", "Seguir sistema"),
    CLARO("claro", "Claro"),
    OSCURO("oscuro", "Oscuro");

    fun esOscuro(sistemaOscuro: Boolean): Boolean = when (this) {
        SISTEMA -> sistemaOscuro
        CLARO -> false
        OSCURO -> true
    }

    companion object {
        internal fun desdeValor(valor: String?): TemaSmartCatalog =
            entries.firstOrNull { it.valor == valor } ?: SISTEMA
    }
}

class PreferenciasTema(context: Context) {
    private val preferencias = context.getSharedPreferences("preferencias_interfaz", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(TemaSmartCatalog.desdeValor(preferencias.getString("tema", null)))
    val tema = mutable.asStateFlow()

    fun elegir(tema: TemaSmartCatalog) {
        preferencias.edit().putString("tema", tema.valor).apply()
        mutable.value = tema
    }
}
