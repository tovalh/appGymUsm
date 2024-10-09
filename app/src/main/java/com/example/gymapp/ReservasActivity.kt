package com.example.gymapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class ReservasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: TimeSlotAdapter

    private val timeSlots = mutableListOf<TimeSlot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservas)

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerView)
        confirmButton = findViewById(R.id.confirmButton)
        tabLayout = findViewById(R.id.tabLayout)

        // Configurar datos de ejemplo
        setupTimeSlots()

        // Configurar RecyclerView
        adapter = TimeSlotAdapter(timeSlots) { selectedTimeSlot ->
            // Deseleccionar todos los demás
            timeSlots.forEach { it.isSelected = false }
            // Seleccionar el actual
            selectedTimeSlot.isSelected = true
            // Actualizar la vista
            adapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Configurar click del botón
        confirmButton.setOnClickListener {
            val selectedSlot = timeSlots.find { it.isSelected }
            if (selectedSlot != null) {
                Toast.makeText(
                    this,
                    "Reserva confirmada para ${selectedSlot.startTime}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Por favor selecciona un horario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Configurar tabs
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Toast.makeText(
                    this@ReservasActivity,
                    "Seleccionado: ${tab?.text}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupTimeSlots() {
        val times = listOf(
            TimeSlot("10:45 am", "11:15 am"),
            TimeSlot("11:30 am", "12:00 pm"),
            TimeSlot("12:15 pm", "12:45 pm"),
            TimeSlot("1:00 pm", "1:30 pm"),
            TimeSlot("1:45 pm", "2:15 pm"),
            TimeSlot("2:30 pm", "3:00 pm")
        )
        timeSlots.addAll(times)
    }
}