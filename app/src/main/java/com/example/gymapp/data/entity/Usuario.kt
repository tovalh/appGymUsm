package com.example.gymapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password")
    val password: String
)