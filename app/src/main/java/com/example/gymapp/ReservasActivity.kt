package com.example.gymapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.data.database.AppDatabase
import com.example.gymapp.data.repository.GymRepository
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class ReservasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: TimeSlotAdapter
    private lateinit var repository: GymRepository

    private val timeSlots = mutableListOf<TimeSlot>()
    private var selectedDia: String = "Lunes" // Día por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservas)

        // Inicializar repository
        val database = AppDatabase.getDatabase(this)
        repository = GymRepository(database)

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerView)
        confirmButton = findViewById(R.id.confirmButton)
        tabLayout = findViewById(R.id.tabLayout)

        // Configurar RecyclerView
        adapter = TimeSlotAdapter(timeSlots) { selectedTimeSlot ->
            timeSlots.forEach { it.isSelected = false }
            selectedTimeSlot.isSelected = true
            adapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Configurar tabs con los días de la semana actual
        setupTabsWithCurrentWeek()

        // Cargar horarios para el día seleccionado por defecto
        loadTimeSlotsForDay(selectedDia)

        // Configurar click del botón
        confirmButton.setOnClickListener {
            val selectedSlot = timeSlots.find { it.isSelected }
            if (selectedSlot != null) {
                lifecycleScope.launch {
                    try {
                        repository.reservarHorario(selectedDia, selectedSlot.startTime)
                        Toast.makeText(
                            this@ReservasActivity,
                            "Reserva confirmada para ${selectedSlot.startTime}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Recargar los horarios para mostrar los cupos actualizados
                        loadTimeSlotsForDay(selectedDia)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ReservasActivity,
                            "Error al realizar la reserva: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Por favor selecciona un horario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Referenciar el TextView
        val dateHeader: TextView = findViewById(R.id.dateHeader)

        // Obtener la fecha actual
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("es"))
        val formattedDate = today.format(formatter)

        // Asignar la fecha formateada al TextView
        dateHeader.text = ("Dia " + formattedDate)

        // Configurar tabs
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    selectedDia = getDayNameFromTab(it.position)
                    loadTimeSlotsForDay(selectedDia)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupTabsWithCurrentWeek() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM")

        // Agregar tabs para cada día de la semana (Lunes a Sábado)
        for (i in 0..5) {
            val date = today.plusDays(i.toLong())
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
            val formattedDate = date.format(formatter)
            tabLayout.addTab(tabLayout.newTab().setText("$dayName\n$formattedDate"))
        }
    }

    private fun getDayNameFromTab(position: Int): String {
        return when (position) {
            0 -> "Lunes"
            1 -> "Martes"
            2 -> "Miércoles"
            3 -> "Jueves"
            4 -> "Viernes"
            5 -> "Sábado"
            else -> "Lunes"
        }
    }

    private fun loadTimeSlotsForDay(dia: String) {
        lifecycleScope.launch {
            try {
                val diaConHorarios = repository.obtenerHorariosPorDia(dia)
                timeSlots.clear()

                diaConHorarios?.horarios?.forEach { horario ->
                    val disponibilidad = repository.obtenerDisponibilidad(horario.id)
                    timeSlots.add(
                        TimeSlot(
                            startTime = horario.hora,
                            endTime = "11:00", // Podrías calcular la hora de fin si lo necesitas
                            availableSpots = disponibilidad?.cuposDisponibles ?: horario.cuposTotales
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ReservasActivity,
                    "Error al cargar horarios: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}