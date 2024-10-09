package com.example.gymapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymapp.data.entity.Dia

@Dao
interface DiaDao {
    @Query("SELECT * FROM dias")
    fun getAll(): List<Dia>

    @Insert
    fun insert(dia: Dia): Long

    @Update
    fun update(dia: Dia)

    @Delete
    fun delete(dia: Dia)
}