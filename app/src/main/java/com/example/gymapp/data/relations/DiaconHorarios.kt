package com.example.gymapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymapp.data.entity.Dia
import com.example.gymapp.data.entity.Horario

data class DiaConHorarios(
    @Embedded val dia: Dia,
    @Relation(
    parentColumn = "id",
    entityColumn = "dia_id"
)
val horarios: List<Horario>
)