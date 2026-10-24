package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.prefs.PreferenciasStore
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repository: AyudaRepository,
    private val preferencias: PreferenciasStore,
    private val sesion: StateFlow<Usuario?>
) : ViewModel() {
    data class Perfil(val usuario: Usuario? = null, val cargando: Boolean = true,
        val abiertos: Int = 0, val resueltos: Int = 0, val total: Int = 0, val error: String? = null)
    data class Ajustes(val modoOscuro: Boolean, val notificaciones: Boolean, val recibirTickets: Boolean)
    private val _perfil = MutableStateFlow(Perfil())
    val perfil = _perfil.asStateFlow()
    private val _ajustes = MutableStateFlow<Ajustes?>(null)
    val ajustes = _ajustes.asStateFlow()
    private val _guardando = MutableStateFlow(false)
    val guardando = _guardando.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _confirmacion = MutableStateFlow<String?>(null)
    val confirmacion = _confirmacion.asStateFlow()
    private val recarga = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            combine(preferencias.modoOscuro, preferencias.notificaciones, preferencias.recibirTickets, ::Ajustes)
                .collect { _ajustes.value = it }
        }
        viewModelScope.launch {
            combine(sesion, recarga) { usuario, _ -> usuario }.collectLatest { usuario ->
                _perfil.value = Perfil()
                try {
                    val real = validar(usuario)
                    if (real.rol == "CLIENTE") repository.observarTicketsCliente(real.id).collect { tickets ->
                        comprobarSesion(real)
                        _perfil.value = Perfil(real, false,
                            tickets.count { it.ticket.estado == "ABIERTO" },
                            tickets.count { it.ticket.estado == "RESUELTO" }, tickets.size)
                    } else _perfil.value = Perfil(real, false)
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { _perfil.value = Perfil(cargando = false, error = "No se pudo cargar el perfil. Necesitas una sesión válida.") }
            }
        }
    }
    private fun comprobarSesion(usuario: Usuario) {
        check(sesion.value?.id == usuario.id && sesion.value?.rol == usuario.rol)
    }
    private suspend fun validar(usuario: Usuario?): Usuario {
        check(usuario != null && usuario.id > 0 && usuario.rol in setOf("CLIENTE", "ADMIN"))
        val real = repository.obtenerUsuario(usuario.id)
        check(real != null && real.rol == usuario.rol)
        comprobarSesion(real)
        return real
    }
    fun reintentar() { recarga.update { it + 1 } }
    private fun guardar(soloSoporte: Boolean = false, accion: suspend () -> Unit) {
        if (_guardando.value) return
        _guardando.value = true
        _error.value = null
        _confirmacion.value = null
        viewModelScope.launch {
            try {
                val usuario = validar(sesion.value)
                check(!soloSoporte || usuario.rol == "ADMIN")
                comprobarSesion(usuario)
                accion()
                _confirmacion.value = "Preferencia guardada"
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo guardar la preferencia. Revisa tu sesión y vuelve a intentarlo." }
            finally { _guardando.value = false }
        }
    }
    fun guardarModoOscuro(b: Boolean) = guardar { preferencias.guardarModoOscuro(b) }
    fun guardarNotificaciones(b: Boolean) = guardar { preferencias.guardarNotificaciones(b) }
    fun guardarRecibirTickets(b: Boolean) = guardar(soloSoporte = true) { preferencias.guardarRecibirTickets(b) }

    class Factory(private val repository: AyudaRepository, private val preferencias: PreferenciasStore,
                  private val sesion: StateFlow<Usuario?>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PerfilViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return PerfilViewModel(repository, preferencias, sesion) as T
        }
    }
}
