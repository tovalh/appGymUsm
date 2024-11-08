package com.example.gymapp.model

// Clase base para representar una reserva
open class UsuarioItem(
    open val nombre: String = "",
    open val asistio: Boolean = false,
    open val hora_marcacion: String = ""
)

// Clase para representar una reserva
data class Usuario(
    override var nombre: String = "",
    override val asistio: Boolean = false,
    override val hora_marcacion: String = ""
) : UsuarioItem(nombre, asistio, hora_marcacion)