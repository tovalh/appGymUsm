package com.example.gymapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gymapp.data.dao.DiaDao
import com.example.gymapp.data.dao.DisponibilidadDao
import com.example.gymapp.data.dao.HorarioDao
import com.example.gymapp.data.dao.ReservaDao
import com.example.gymapp.data.dao.UsuarioDao
import com.example.gymapp.data.entity.Dia
import com.example.gymapp.data.entity.Disponibilidad
import com.example.gymapp.data.entity.Horario
import com.example.gymapp.data.entity.Reserva
import com.example.gymapp.data.entity.Usuario

@Database(
    entities = [
        Dia::class,
        Horario::class,
        Disponibilidad::class,
        Reserva::class,
        Usuario::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaDao(): DiaDao
    abstract fun horarioDao(): HorarioDao
    abstract fun disponibilidadDao(): DisponibilidadDao
    abstract fun reservaDao(): ReservaDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}