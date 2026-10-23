package com.tecsup.ayudacampus.data.local

import androidx.room.withTransaction
import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.data.local.entity.Comentario
import com.tecsup.ayudacampus.data.local.entity.Ticket
import com.tecsup.ayudacampus.data.local.entity.Usuario
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object DatosIniciales {
    private val mutex = Mutex()

    suspend fun sembrar(db: AyudaDatabase) {
        mutex.withLock {
            // La comprobación y todas las inserciones forman una sola operación atómica.
            db.withTransaction {
                if (db.usuarioDao().contar() != 0) return@withTransaction

                val categorias = listOf(
                    Categoria(nombre = "Hardware", icono = "pc"),
                    Categoria(nombre = "Software", icono = "code"),
                    Categoria(nombre = "Red / Wi-Fi", icono = "wifi"),
                    Categoria(nombre = "Cuenta", icono = "key"),
                    Categoria(nombre = "Impresión", icono = "printer")
                )
                val categoriaIds = categorias.map { db.categoriaDao().insertar(it).toInt() }
                val estudiante = Usuario(
                    nombre = "Juan", correo = "juan@campus.test", clave = "1234", rol = "CLIENTE"
                )
                val soporte = Usuario(
                    nombre = "Soporte TI", correo = "soporte@campus.test", clave = "admin", rol = "ADMIN"
                )
                val usuarioIds = db.usuarioDao().insertarTodos(listOf(estudiante, soporte))
                val estudianteId = usuarioIds[0].toInt()
                val soporteId = usuarioIds[1].toInt()
                val ahora = System.currentTimeMillis()
                val hora = 3_600_000L
                val tickets = listOf(
                    Ticket(
                        clienteId = estudianteId, categoriaId = categoriaIds[2],
                        titulo = "Wi-Fi sin conexión", descripcion = "No puedo conectarme a la red del campus.",
                        ubicacion = "Biblioteca", prioridad = "ALTA", estado = "ABIERTO",
                        fecha = ahora - 3 * hora
                    ),
                    Ticket(
                        clienteId = estudianteId, categoriaId = categoriaIds[0],
                        titulo = "Equipo no enciende", descripcion = "La computadora no responde al botón de encendido.",
                        ubicacion = "Laboratorio 2", prioridad = "ALTA", estado = "EN_PROCESO",
                        fecha = ahora - 5 * hora
                    ),
                    Ticket(
                        clienteId = estudianteId, categoriaId = categoriaIds[1],
                        titulo = "Error al abrir el editor", descripcion = "El editor muestra un error al iniciar el programa.",
                        ubicacion = "Laboratorio 1", prioridad = "MEDIA", estado = "RESUELTO",
                        fecha = ahora - 8 * hora
                    ),
                    Ticket(
                        clienteId = estudianteId, categoriaId = categoriaIds[3],
                        titulo = "Cuenta de campus bloqueada", descripcion = "Mi cuenta quedó bloqueada después de varios intentos.",
                        ubicacion = "Aula 204", prioridad = "MEDIA", estado = "CERRADO",
                        fecha = ahora - 24 * hora, fechaCierre = ahora - 12 * hora, calificacion = 5
                    ),
                    Ticket(
                        clienteId = estudianteId, categoriaId = categoriaIds[4],
                        titulo = "Impresora sin papel", descripcion = "La impresora indica que la bandeja está vacía.",
                        ubicacion = "Biblioteca", prioridad = "BAJA", estado = "ABIERTO",
                        fecha = ahora - hora / 2
                    ),
                    Ticket(
                        clienteId = estudianteId, categoriaId = categoriaIds[1],
                        titulo = "Actualizar programa de diseño", descripcion = "El programa instalado requiere una actualización.",
                        ubicacion = "Laboratorio 3", prioridad = "BAJA", estado = "EN_PROCESO",
                        fecha = ahora - 2 * hora
                    )
                )
                val ticketIds = tickets.map { db.ticketDao().insertarTicket(it).toInt() }
                val comentarios = listOf(
                    Comentario(
                        ticketId = ticketIds[0], autorId = estudianteId,
                        autorNombre = estudiante.nombre, autorRol = estudiante.rol,
                        texto = "La conexión falla también en mi teléfono.", fecha = ahora - 2 * hora
                    ),
                    Comentario(
                        ticketId = ticketIds[1], autorId = soporteId,
                        autorNombre = soporte.nombre, autorRol = soporte.rol,
                        texto = "Estamos revisando la fuente de alimentación.", fecha = ahora - 4 * hora
                    ),
                    Comentario(
                        ticketId = ticketIds[2], autorId = soporteId,
                        autorNombre = soporte.nombre, autorRol = soporte.rol,
                        texto = "Reinstalamos el editor y ya inicia correctamente.", fecha = ahora - 6 * hora
                    ),
                    Comentario(
                        ticketId = ticketIds[3], autorId = estudianteId,
                        autorNombre = estudiante.nombre, autorRol = estudiante.rol,
                        texto = "Ya puedo ingresar a mi cuenta. Gracias.", fecha = ahora - 12 * hora
                    )
                )
                comentarios.forEach { db.ticketDao().insertarComentario(it) }
            }
        }
    }
}
