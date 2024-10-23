package com.example.gymapp.model

// Clase base para representar una reserva
open class ReservaItem(
    open val bloqueId: String = "",
    open val dia: String = "",
    open val hora_inicio: String = "",
    open val hora_final: String = "",
    open val fecha: String = "",
    open val estado: String
)

// Clase para representar una reserva
data class Reserva(
    override val bloqueId: String = "",
    override val dia: String = "",
    override val hora_inicio: String = "",
    override val hora_final: String = "",
    override val fecha: String = "",
    override val estado: String = ""
) : ReservaItem(bloqueId, dia, hora_inicio, hora_final, fecha,estado)