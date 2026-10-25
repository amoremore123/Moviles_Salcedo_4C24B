package com.tecsup.ayudacampus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.FallbackToDestructiveMigration
import com.tecsup.ayudacampus.data.local.dao.CategoriaDao
import com.tecsup.ayudacampus.data.local.dao.NotificacionDao
import com.tecsup.ayudacampus.data.local.dao.TicketDao
import com.tecsup.ayudacampus.data.local.dao.UsuarioDao
import com.tecsup.ayudacampus.data.local.entity.Categoria
import com.tecsup.ayudacampus.data.local.entity.Comentario
import com.tecsup.ayudacampus.data.local.entity.Notificacion
import com.tecsup.ayudacampus.data.local.entity.Ticket
import com.tecsup.ayudacampus.data.local.entity.Usuario

@Database(
    entities = [Categoria::class, Usuario::class, Ticket::class, Comentario::class, Notificacion::class],
    version = 2,
    exportSchema = false,
    fallbackToDestructiveMigration = [FallbackToDestructiveMigration::class]
)
abstract class AyudaDatabase : RoomDatabase() {
    abstract fun categoriaDao(): CategoriaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ticketDao(): TicketDao
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        @Volatile
        private var INSTANCE: AyudaDatabase? = null

        fun obtener(context: Context): AyudaDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AyudaDatabase::class.java,
                "ayudacampus.db"
            ).fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
