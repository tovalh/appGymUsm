package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymapp.data.entity.Reserva

@Dao
interface ReservaDao {
    @Query("SELECT * FROM reserva")
    suspend fun getAllReservas(): List<Reserva>

    @Insert
    suspend fun insertReserva(reserva: Reserva)

    @Delete
    suspend fun deleteReserva(reserva: Reserva)

    @Query("SELECT * FROM reserva WHERE fecha = :fecha")
    suspend fun getReservasByFecha(fecha: String): List<Reserva>
}