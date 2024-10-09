package com.example.gymapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import android.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.gymapp.data.database.AppDatabase
import com.example.gymapp.data.relations.DiaConHorarios
import kotlinx.coroutines.launch
// Asegúrate de importar tus clases de Room (Repository, Database, etc.)
import com.example.gymapp.data.repository.GymRepository

class HorarioActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var repository: GymRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horario)

        // Inicializar el repository
        val database = AppDatabase.getDatabase(this)
        repository = GymRepository(database)

        // Inicializar datos básicos
        lifecycleScope.launch {
            try {
                repository.inicializarDatosBasicos()
            } catch (e: Exception) {
                // Manejar cualquier error de inicialización
                e.printStackTrace()
            }
        }

        setupBottomNavigation()
        setupDayCards()
    }

    private fun setupBottomNavigation() {
        // Tu código existente de bottomNavigation
    }

    private fun setupDayCards() {
        val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val daysInEnglish = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val cardIds = listOf(
            R.id.mondayCard, R.id.tuesdayCard, R.id.wednesdayCard,
            R.id.thursdayCard, R.id.fridayCard, R.id.saturdayCard
        )

        for (i in days.indices) {
            val cardView = findViewById<MaterialCardView>(cardIds[i])
            val dayTextView = cardView.findViewById<TextView>(R.id.dayText)
            dayTextView.text = daysInEnglish[i]

            cardView.setOnClickListener {
                mostrarHorariosDelDia(days[i])
            }
        }
    }

    private fun mostrarHorariosDelDia(dia: String) {
        lifecycleScope.launch {
            try {
                val diaConHorarios = repository.obtenerHorariosPorDia(dia)
                diaConHorarios?.let {
                    mostrarDialogoHorarios(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Mostrar un mensaje de error al usuario
            }
        }
    }

    private fun mostrarDialogoHorarios(diaConHorarios: DiaConHorarios) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Horarios para ${diaConHorarios.dia.nombre}")

        // Crear la lista de horarios para mostrar
        val horariosString = diaConHorarios.horarios.joinToString("\n") { horario ->
            "${horario.hora} - Cupos disponibles: ${horario.cuposTotales}"
        }

        dialog.setMessage(horariosString)
        dialog.setPositiveButton("Cerrar") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        dialog.show()
    }
}