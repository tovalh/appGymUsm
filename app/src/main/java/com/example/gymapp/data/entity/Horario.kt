package com.example.gymapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "horario")
data class Horario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "hora")
    val hora: String,  // "10:00", "11:00", etc.

    @ColumnInfo(name = "cupos_totales")
    val cuposTotales: Int = 20,  // Número máximo de personas por horario

    @ColumnInfo(name = "dia_id")
    val diaId: Int  // Referencia al día
)