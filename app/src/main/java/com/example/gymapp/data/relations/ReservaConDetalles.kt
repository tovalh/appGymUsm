package com.example.gymapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymapp.data.entity.Horario
import com.example.gymapp.data.entity.Reserva
import com.example.gymapp.data.entity.Usuario

data class ReservaConDetalles(
    @Embedded val reserva: Reserva,
    @Relation(
        parentColumn = "usuario_id",
        entityColumn = "id"
    )
    val usuario: Usuario,
    @Relation(
        parentColumn = "horario_id",
        entityColumn = "id"
    )
    val horario: Horario
)