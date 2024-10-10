package com.example.gymapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymapp.data.entity.Reserva
import com.example.gymapp.data.entity.Usuario

data class UsuarioConReservas(
    @Embedded val usuario: Usuario,
    @Relation(
        parentColumn = "id",
        entityColumn = "usuario_id"
    )
    val reservas: List<Reserva>
)