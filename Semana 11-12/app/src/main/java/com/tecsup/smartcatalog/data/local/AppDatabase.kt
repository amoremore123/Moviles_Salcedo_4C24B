package com.tecsup.smartcatalog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ItemEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        const val NOMBRE = "smartcatalog.db"

        /** Se invoca de forma perezosa; sin borrado inicial ni migración destructiva automática. */
        fun crear(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            NOMBRE
        ).build()
    }
}
