package com.example.gymapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reserva",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Horario::class,
            parentColumns = ["id"],
            childColumns = ["horario_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuario_id"]),
        Index(value = ["horario_id"])
    ]
)
data class Reserva(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "fecha")
    val fecha: String,

    @ColumnInfo(name = "usuario_id")
    val usuarioId: Int,

    @ColumnInfo(name = "horario_id")
    val horarioId: Int
)