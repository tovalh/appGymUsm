package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymapp.data.entity.Reserva

@Dao
interface ReservaDao {
    @Query("SELECT * FROM reservas")
    fun getAll(): List<Reserva>

    @Insert
    fun insert(reserva: Reserva): Long

    @Update
    fun update(reserva: Reserva)

    @Delete
    fun delete(reserva: Reserva)
}