package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymapp.data.entity.Horario

@Dao
interface HorarioDao {
    @Query("SELECT * FROM horario WHERE dia_id = :diaId")
    suspend fun getHorariosPorDia(diaId: Int): List<Horario>

    @Insert
    suspend fun insertHorario(horario: Horario)

    @Query("UPDATE horario SET cupos_totales = :cupos WHERE id = :horarioId")
    suspend fun actualizarCupos(horarioId: Int, cupos: Int)
}