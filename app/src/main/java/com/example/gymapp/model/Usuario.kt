package com.example.gymapp.model

// Clase base para representar un Usuario
open class UsuarioItem(
    open var username: String = "",
    open var asistio: Boolean = false,
    open var hora_marcacion: String = "",
    open var password: String = "",
    open var isAdmin: Boolean = false
)

// Clase para representar un Usuario
data class Usuario(
    override var username: String = "",
    override var asistio: Boolean = false,
    override var hora_marcacion: String = "" ,
    override var password: String = "",
    override var isAdmin: Boolean = false
) : UsuarioItem(username, asistio, hora_marcacion, password, isAdmin)