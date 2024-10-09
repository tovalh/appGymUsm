package com.example.gymapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "dia")
data class Dia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String, // "Lunes", "Martes", etc.

    @ColumnInfo(name = "activo")
    val activo: Boolean = true  // Por si necesitas desactivar algún día
)