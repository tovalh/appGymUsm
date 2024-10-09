package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymapp.data.entity.Disponibilidad

@Dao
interface DisponibilidadDao {
    @Query("SELECT * FROM disponibilidad WHERE horario_id = :horarioId AND semana = :semana")
    suspend fun getDisponibilidadPorHorario(horarioId: Int, semana: Int): Disponibilidad?

    @Insert
    suspend fun insertDisponibilidad(disponibilidad: Disponibilidad)

    @Update
    suspend fun updateDisponibilidad(disponibilidad: Disponibilidad)

    @Query("UPDATE disponibilidad SET cupos_disponibles = :cuposDisponibles WHERE id = :id")
    suspend fun actualizarCupos(id: Int, cuposDisponibles: Int)
}