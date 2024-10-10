package com.example.gymapp.data.dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.gymapp.data.entity.Usuario
import com.example.gymapp.data.relations.UsuarioConReservas

interface UsuarioDao {
    @Transaction
    @Query("SELECT * FROM usuarios WHERE id = :userId")
    suspend fun getUsuarioConReservas(userId: Int): UsuarioConReservas?

    @Insert
    suspend fun registrarUsuario(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getUsuarioById(id: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): Usuario?
}
