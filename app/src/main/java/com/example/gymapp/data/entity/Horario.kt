package com.example.gymapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "horarios")
data class Horario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val horaInicio: LocalTime,
    val horaFin: LocalTime
)