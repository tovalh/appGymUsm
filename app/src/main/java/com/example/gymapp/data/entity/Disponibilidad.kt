package com.example.gymapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "disponibilidad",
    foreignKeys = [
        ForeignKey(
            entity = Horario::class,
            parentColumns = ["id"],
            childColumns = ["horario_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Disponibilidad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "horario_id")
    val horarioId: Int,

    @ColumnInfo(name = "cupos_disponibles")
    val cuposDisponibles: Int,

    @ColumnInfo(name = "semana")
    val semana: Int  // Para trackear la semana del año
)