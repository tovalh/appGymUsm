package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.gymapp.data.entity.Dia
import com.example.gymapp.data.relations.DiaConHorarios

@Dao
interface DiaDao {
    @Query("SELECT * FROM dia")
    suspend fun getAllDias(): List<Dia>

    @Insert
    suspend fun insertDia(dia: Dia): Long

    @Transaction
    @Query("SELECT * FROM dia WHERE nombre = :nombreDia")
    suspend fun getDiaConHorarios(nombreDia: String): DiaConHorarios?
}