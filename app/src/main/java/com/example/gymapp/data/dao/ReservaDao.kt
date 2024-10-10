package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.gymapp.data.entity.Reserva
import com.example.gymapp.data.relations.ReservaConDetalles

@Dao
interface ReservaDao {
    @Transaction
    @Query("SELECT * FROM reserva WHERE usuario_id = :usuarioId")
    suspend fun getReservasByUsuario(usuarioId: Int): List<ReservaConDetalles>

    @Transaction
    @Query("""
        SELECT * FROM reserva 
        WHERE usuario_id = :usuarioId 
        AND fecha = :fecha 
        LIMIT 1
    """)
    suspend fun getReservaByUsuarioAndFecha(usuarioId: Int, fecha: String): ReservaConDetalles?

    @Insert
    suspend fun insert(reserva: Reserva): Long

    @Delete
    suspend fun delete(reserva: Reserva)
}