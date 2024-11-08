package com.example.gymapp.model

// Clase base para representar un Usuario
open class UsuarioItem(
    open val username: String = "",
    open val asistio: Boolean = false,
    open val hora_marcacion: String = "",
    open val password: String = "",
    open val isAdmin: Boolean = false
)

// Clase para representar un Usuario
data class Usuario(
    override val username: String = "",
    override val asistio: Boolean = false,
    override val hora_marcacion: String = "" ,
    override val password: String = "",
    override val isAdmin: Boolean = false
) : UsuarioItem(username, asistio, hora_marcacion, password, isAdmin)