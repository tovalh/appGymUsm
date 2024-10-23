package com.example.gymapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// Clase base para representar un elemento común
open class Item(
    open val dia: String = "",
    open val hora_inicio: String = "",
    open val hora_final: String = "",
    open val cupos_disponibles: Int = 0
)

// Clase para representar un bloque horario
data class BloqueHorario(
    var id: String = "",
    override val dia: String = "",
    override val hora_inicio: String = "",
    override val hora_final: String = "",
    override val cupos_disponibles: Int = 0,
    val estadoReserva: String = ""
) : Item(dia, hora_inicio, hora_final, cupos_disponibles)

// Clase para representar un bloque horario en el carrito
@Parcelize
data class CartBloqueHorario(
    val dia: String = "",
    val hora_inicio: String = "",
    val hora_final: String = "",
    val cupos_disponibles: Int = 0
): Parcelable