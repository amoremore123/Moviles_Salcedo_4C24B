package com.tecsup.mibodega.ui.cliente.modelo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Usuario(
    val nombre: String,
    val correo: String,
    val password: String,
    val telefono: String = "987654321",
    val direccion: String = "Av. Principal 123",
    val referencia: String = "Cerca al parque"
)

data class Pedido(
    val id: String,
    val items: List<ItemCarrito>,
    val total: Double,
    val fecha: String,
    val estado: String,
    val direccion: String
)

object SesionManager {
    // Lista de usuarios registrados en memoria (con un usuario por defecto para pruebas)
    private val usuariosRegistrados = mutableListOf(
        Usuario(
            nombre = "Juan Pérez",
            correo = "juan@tecsup.edu.pe",
            password = "123456",
            telefono = "987654321",
            direccion = "Av. Los Olivos 123",
            referencia = "Frente al parque"
        )
    )

    // Usuario actualmente autenticado (null si no ha iniciado sesión)
    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set

    // Historial de pedidos por correo de usuario
    private val pedidosPorUsuario = mutableMapOf<String, MutableList<Pedido>>(
        "juan@tecsup.edu.pe" to mutableListOf(
            Pedido(
                id = "#1024",
                items = listOf(
                    ItemCarrito(producto = listaProductosFake[4], cantidad = 2), // 2 Coca-Cola
                    ItemCarrito(producto = listaProductosFake[3], cantidad = 1)  // 1 Galleta Oreo
                ),
                total = 16.50,
                fecha = "02 Oct 2026",
                estado = "Entregado",
                direccion = "Av. Los Olivos 123"
            )
        )
    )

    fun registrarUsuario(usuario: Usuario): Result<Unit> {
        if (usuariosRegistrados.any { it.correo.equals(usuario.correo, ignoreCase = true) }) {
            return Result.failure(Exception("El correo ya está registrado en el sistema."))
        }
        usuariosRegistrados.add(usuario)
        usuarioActual = usuario
        if (!pedidosPorUsuario.containsKey(usuario.correo)) {
            pedidosPorUsuario[usuario.correo] = mutableListOf()
        }
        return Result.success(Unit)
    }

    fun iniciarSesion(correo: String, pass: String): Result<Usuario> {
        val usuario = usuariosRegistrados.find { it.correo.equals(correo, ignoreCase = true) }
        return when {
            usuario == null -> Result.failure(Exception("El correo no está registrado."))
            usuario.password != pass -> Result.failure(Exception("Contraseña incorrecta."))
            else -> {
                usuarioActual = usuario
                if (!pedidosPorUsuario.containsKey(usuario.correo)) {
                    pedidosPorUsuario[usuario.correo] = mutableListOf()
                }
                Result.success(usuario)
            }
        }
    }

    fun cerrarSesion() {
        usuarioActual = null
    }

    fun obtenerPedidosActuales(): List<Pedido> {
        val correo = usuarioActual?.correo ?: return emptyList()
        return pedidosPorUsuario[correo] ?: emptyList()
    }

    fun agregarPedido(pedido: Pedido) {
        val correo = usuarioActual?.correo ?: return
        val lista = pedidosPorUsuario.getOrPut(correo) { mutableListOf() }
        lista.add(0, pedido)
    }
}
