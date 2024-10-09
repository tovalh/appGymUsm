package com.example.gymapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "reservas",
    foreignKeys = [
        ForeignKey(
            entity = Disponibilidad::class,
            parentColumns = ["id"],
            childColumns = ["disponibilidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Reserva(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val disponibilidadId: Long,
    val usuarioId: Long,
    val fechaReserva: LocalDateTime
)