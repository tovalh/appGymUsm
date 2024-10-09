package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymapp.data.entity.Disponibilidad


@Dao
interface DisponibilidadDao {
    @Query("SELECT * FROM disponibilidad")
    fun getAll(): List<Disponibilidad>

    @Insert
    fun insert(disponibilidad: Disponibilidad): Long

    @Update
    fun update(disponibilidad: Disponibilidad)

    @Delete
    fun delete(disponibilidad: Disponibilidad)
}