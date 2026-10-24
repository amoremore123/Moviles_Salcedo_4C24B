package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

class CategoriasViewModel(
    private val repository: AyudaRepository,
    private val sesion: StateFlow<Usuario?>,
    private val categoriaId: Int? = null
) : ViewModel() {
    data class Pantalla(
        val categorias: List<Categoria> = emptyList(), val cargando: Boolean = true,
        val formulario: Categoria = Categoria(nombre = "", icono = "tag"),
        val operando: Boolean = false, val errorCarga: String? = null,
        val error: String? = null, val confirmacion: String? = null, val guardado: Boolean = false
    )
    private val _pantalla = MutableStateFlow(Pantalla())
    val pantalla = _pantalla.asStateFlow()
    val categorias = pantalla.map { it.categorias }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val recarga = MutableStateFlow(0)
    private var ocupado = false
    private var formularioCargado = false
    private var ultimoUsuario: Int? = null

    init {
        viewModelScope.launch {
            combine(sesion, recarga) { usuario, _ -> usuario }.collectLatest { usuario ->
                val mismoUsuario = ultimoUsuario == usuario?.id && usuario?.rol == "ADMIN"
                val borrador = if (mismoUsuario) _pantalla.value.formulario else Categoria(nombre = "", icono = "tag")
                ultimoUsuario = usuario?.id
                if (!mismoUsuario) formularioCargado = false
                _pantalla.value = Pantalla(formulario = borrador, operando = ocupado)
                try {
                    if (categoriaId != null && categoriaId <= 0) throw ReglaSoporte("Categoría no disponible")
                    val autor = repository.validarSoporte(sesion)
                    repository.observarCategorias().collect { lista ->
                        exigirMismaSesion(sesion, autor)
                        val categoria = categoriaId?.let { id -> lista.firstOrNull { it.id == id } ?: throw ReglaSoporte("Categoría no disponible") }
                        _pantalla.update { it.copy(categorias = lista, cargando = false, errorCarga = null,
                            formulario = if (!formularioCargado && categoria != null) categoria else it.formulario) }
                        formularioCargado = true
                    }
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) {
                    _pantalla.update { it.copy(categorias = emptyList(), cargando = false,
                        errorCarga = if (e is ReglaSoporte) e.message else "No se pudieron cargar las categorías") }
                }
            }
        }
    }
    fun reintentar() { recarga.update { it + 1 } }
    private fun editar(cambio: (Categoria) -> Categoria) {
        if (!ocupado && !_pantalla.value.guardado) _pantalla.update { it.copy(formulario = cambio(it.formulario), error = null, confirmacion = null) }
    }
    fun actualizarNombre(nombre: String) = editar { it.copy(nombre = nombre) }
    fun seleccionarIcono(icono: String) = editar { it.copy(icono = icono) }
    fun establecerActiva(activa: Boolean) = editar { it.copy(activa = activa) }
    private fun ejecutar(accion: suspend (Usuario) -> String) {
        if (ocupado) return
        ocupado = true
        _pantalla.update { it.copy(operando = true, error = null, confirmacion = null) }
        viewModelScope.launch {
            try {
                val mensaje = cambios.withLock {
                    val autor = repository.validarSoporte(sesion)
                    accion(autor)
                }
                _pantalla.update { it.copy(confirmacion = mensaje) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _pantalla.update { it.copy(error = if (e is ReglaSoporte) e.message else "No se pudo completar la operación. Los datos se conservan; vuelve a intentarlo.") }
            } finally {
                ocupado = false
                _pantalla.update { it.copy(operando = false) }
            }
        }
    }
    fun guardar(categoria: Categoria = _pantalla.value.formulario) = ejecutar { autor ->
        val nombre = limpiarNombre(categoria.nombre)
        if (nombre.isEmpty()) throw ReglaSoporte("Ingresa el nombre de la categoría")
        if (categoria.icono !in iconos) throw ReglaSoporte("Selecciona un icono válido")
        if (categoria.id < 0) throw ReglaSoporte("Categoría no disponible")
        val actuales = repository.observarCategorias().first()
        if (categoria.id != 0 && actuales.none { it.id == categoria.id }) throw ReglaSoporte("Categoría no disponible")
        if (actuales.any { it.id != categoria.id && limpiarNombre(it.nombre).lowercase(Locale.ROOT) == nombre.lowercase(Locale.ROOT) })
            throw ReglaSoporte("Ya existe una categoría con ese nombre")
        exigirMismaSesion(sesion, autor)
        repository.guardarCategoria(categoria.copy(nombre = nombre))
        _pantalla.update { it.copy(guardado = true) }
        "Categoría guardada"
    }
    fun cambiarActiva(categoria: Categoria, activa: Boolean) = ejecutar { autor ->
        if (repository.observarCategorias().first().none { it.id == categoria.id }) throw ReglaSoporte("Categoría no disponible")
        exigirMismaSesion(sesion, autor)
        repository.cambiarActiva(categoria.id, activa)
        "Categoría ${if (activa) "activada" else "desactivada"}"
    }
    fun eliminar(categoria: Categoria) = ejecutar { autor ->
        val actual = repository.observarCategorias().first().firstOrNull { it.id == categoria.id } ?: throw ReglaSoporte("Categoría no disponible")
        if (repository.observarTodosLosTickets().first().any { it.ticket.categoriaId == actual.id })
            throw ReglaSoporte("Esta categoría tiene tickets. Desactívala en lugar de eliminarla.")
        exigirMismaSesion(sesion, autor)
        repository.eliminarCategoria(actual)
        "Categoría eliminada"
    }
    companion object {
        val iconos = listOf("pc", "code", "wifi", "key", "printer", "tag")
        private val cambios = Mutex()
        private fun limpiarNombre(nombre: String) = nombre.trim().replace(Regex("\\s+"), " ")
    }
    class Factory(private val repository: AyudaRepository, private val sesion: StateFlow<Usuario?>, private val id: Int? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CategoriasViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return CategoriasViewModel(repository, sesion, id) as T
        }
    }
}
