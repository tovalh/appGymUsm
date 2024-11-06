package com.example.gymapp.model

// Clase base para representar una reserva
open class UsuarioItem(
    open val userId: String = "",
    open val nombre: String = "",
    open val email: String = "",
    open val asistio: Boolean = false,
    open val hora_marcacion: String = ""
)

// Clase para representar una reserva
data class Usuario(
    override var userId: String = "",
    override val nombre: String = "",
    override val email: String = "",
    override val asistio: Boolean = false,
    override val hora_marcacion: String = ""
) : UsuarioItem(userId,nombre, email, asistio, hora_marcacion)