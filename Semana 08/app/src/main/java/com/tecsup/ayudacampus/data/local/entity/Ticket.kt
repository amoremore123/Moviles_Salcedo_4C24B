package com.tecsup.ayudacampus.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
@Entity(tableName = "tickets", foreignKeys = [ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["clienteId"]), ForeignKey(entity = Categoria::class, parentColumns = ["id"], childColumns = ["categoriaId"])], indices = [Index("clienteId"), Index("categoriaId")])
data class Ticket(@PrimaryKey(autoGenerate = true) val id: Int = 0, val clienteId: Int, val categoriaId: Int, val titulo: String, val descripcion: String, val ubicacion: String, val prioridad: String = Prioridad.MEDIA.name, val estado: String = EstadoTicket.ABIERTO.name, val fecha: Long = System.currentTimeMillis(), val fechaCierre: Long? = null, val calificacion: Int? = null)
