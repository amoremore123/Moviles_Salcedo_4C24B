package com.tecsup.smartcatalog.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.notificaciones.NotificadorCatalogo
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.ui.navigation.AccesoRutas
import com.tecsup.smartcatalog.ui.navigation.DestinoProtegido
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlmacenamientoCatalogo {
    data object Cargando : AlmacenamientoCatalogo
    data object Error : AlmacenamientoCatalogo
    data class Disponible(val productos: Int, val favoritos: Int) : AlmacenamientoCatalogo
}

sealed interface SincronizacionAdmin {
    data object Inicial : SincronizacionAdmin
    data object EnCurso : SincronizacionAdmin
    data class Correcta(val recibidos: Int) : SincronizacionAdmin
    data class Fallida(val sinConexion: Boolean) : SincronizacionAdmin
}

enum class ResultadoCache { INICIAL, LIMPIADA, ERROR }

class PanelAdminViewModel(
    private val repositorio: ItemRepository,
    val ultimaSincronizacion: StateFlow<Long?>,
    private val notificador: NotificadorCatalogo = NotificadorCatalogo {},
    private val limpiarImagenes: () -> Unit
) : ViewModel() {
    private val almacenamientoMutable = MutableStateFlow<AlmacenamientoCatalogo>(AlmacenamientoCatalogo.Cargando)
    val almacenamiento = almacenamientoMutable.asStateFlow()
    private val sincronizacionMutable = MutableStateFlow<SincronizacionAdmin>(SincronizacionAdmin.Inicial)
    val sincronizacion = sincronizacionMutable.asStateFlow()
    private val cacheMutable = MutableStateFlow(ResultadoCache.INICIAL)
    val cache = cacheMutable.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repositorio.items.collect { items ->
                    almacenamientoMutable.value = AlmacenamientoCatalogo.Disponible(items.size, items.count { it.favorito })
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                almacenamientoMutable.value = AlmacenamientoCatalogo.Error
            }
        }
    }

    fun sincronizar(sesion: Sesion?) {
        if (!AccesoRutas.permite(sesion, DestinoProtegido.ADMIN) ||
            sincronizacionMutable.value == SincronizacionAdmin.EnCurso
        ) return
        sincronizacionMutable.value = SincronizacionAdmin.EnCurso
        viewModelScope.launch {
            val cantidad = try {
                repositorio.refrescar().getOrThrow()
            } catch (cancelacion: CancellationException) {
                sincronizacionMutable.value = SincronizacionAdmin.Inicial
                throw cancelacion
            } catch (error: Exception) {
                sincronizacionMutable.value = SincronizacionAdmin.Fallida(error is IOException || error.cause is IOException)
                return@launch
            }
            currentCoroutineContext().ensureActive()
            sincronizacionMutable.value = SincronizacionAdmin.Correcta(cantidad)
            if (cantidad > 0) notificador.publicar(cantidad)
        }
    }

    fun limpiarCache(sesion: Sesion?) {
        if (!AccesoRutas.permite(sesion, DestinoProtegido.ADMIN)) return
        cacheMutable.value = try {
            limpiarImagenes()
            ResultadoCache.LIMPIADA
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            ResultadoCache.ERROR
        }
    }

    class Factory(
        private val repositorio: ItemRepository,
        private val ultimaSincronizacion: StateFlow<Long?>,
        private val notificador: NotificadorCatalogo,
        private val limpiarImagenes: () -> Unit
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PanelAdminViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return PanelAdminViewModel(repositorio, ultimaSincronizacion, notificador, limpiarImagenes) as T
        }
    }
}
