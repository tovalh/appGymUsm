package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymapp.data.entity.Horario

@Dao
interface HorarioDao {
    @Query("SELECT * FROM horarios")
    fun getAll(): List<Horario>

    @Insert
    fun insert(horario: Horario): Long

    @Update
    fun update(horario: Horario)

    @Delete
    fun delete(horario: Horario)
}