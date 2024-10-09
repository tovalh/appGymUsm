package com.example.gymapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "reserva",
//    foreignKeys = [
//        ForeignKey(
//            entity = Disponibilidad::class,
//            parentColumns = ["id"],
//            childColumns = ["disponibilidadId"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ]
)
data class Reserva(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "fecha")
    val fecha: String,

    @ColumnInfo(name = "hora")
    val hora: String,

    @ColumnInfo(name = "usuario_id")
    val usuarioId: Int
)