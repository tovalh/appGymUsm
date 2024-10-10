package com.example.gymapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gymapp.data.database.AppDatabase
import com.example.gymapp.data.entity.Dia
import com.example.gymapp.data.entity.Disponibilidad
import com.example.gymapp.data.relations.DiaConHorarios
import com.example.gymapp.data.entity.Horario
import java.time.LocalDate
import java.time.temporal.ChronoField

class GymRepository(private val database: AppDatabase) {

    suspend fun inicializarDatosBasicos() {
        // Insertar días si no existen
        val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

        dias.forEach { nombreDia ->
            val diaExistente = database.diaDao().getDiaConHorarios(nombreDia)
            if (diaExistente == null) {
                // Crear el día
                val dia = Dia(nombre = nombreDia)
                val diaId = database.diaDao().insertDia(dia).toInt()

                // Crear horarios para ese día
                val horarios = listOf("10:00", "11:00", "12:00", "13:00", "14:00", "15:00")
                horarios.forEach { hora ->
                    val horario = Horario(
                        hora = hora,
                        cuposTotales = 20,
                        diaId = diaId
                    )
                    database.horarioDao().insertHorario(horario)
                }
            }
        }
    }

    suspend fun obtenerHorariosPorDia(nombreDia: String): DiaConHorarios? {
        return database.diaDao().getDiaConHorarios(nombreDia)
    }

    suspend fun reservarHorario(dia: String, hora: String): Boolean {
        // Implementar la lógica de reserva
        return true
    }

    suspend fun obtenerDisponibilidad(horarioId: Int): Disponibilidad? {
        val semanaActual = LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        return database.disponibilidadDao().getDisponibilidadPorHorario(horarioId, semanaActual)
    }
}