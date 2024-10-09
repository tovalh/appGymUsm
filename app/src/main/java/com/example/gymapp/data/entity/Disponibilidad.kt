package com.example.gymapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "disponibilidad",
    foreignKeys = [
        ForeignKey(
            entity = Dia::class,
            parentColumns = ["id"],
            childColumns = ["diaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Horario::class,
            parentColumns = ["id"],
            childColumns = ["horarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Disponibilidad(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val diaId: Long,
    val horarioId: Long,
    val cuposDisponibles: Int,
    val cuposTotales: Int
)