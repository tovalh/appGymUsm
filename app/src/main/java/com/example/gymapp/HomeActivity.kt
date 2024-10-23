package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gymapp.model.Reserva
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    val fechaActual = LocalDateTime.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        FirebaseApp.initializeApp(this)
        initializeDatabase()
        setupButtons()
        mostrarFraseAleatoria()
        botonMenu()
        actualizarProximaReserva()
    }
    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    private fun setupButtons(){
        val reservaHorarioBoton = findViewById<Button>(R.id.btnReserva)
        reservaHorarioBoton.setOnClickListener {
            irReservas()
        }
    }

    private fun botonMenu() {
        val menuNavegacion = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        menuNavegacion.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_calendar -> {
                    val intentCalendario = Intent(this, ReservasActivity::class.java)
                    startActivity(intentCalendario)
                    true
                }
                R.id.nav_clock -> {
                    val intentReloj = Intent(this, HorarioActivity::class.java)
                    startActivity(intentReloj)
                    true
                }
                else -> false
            }
        }
    }

    private fun irReservas() {
        val intent = Intent(this, ReservasActivity::class.java)
        startActivity(intent)
    }

    private fun mostrarFraseAleatoria() {
        val tvFrase = findViewById<TextView>(R.id.tvQuote)

        database.child("frasesMotivacionales").get().addOnSuccessListener { snapshot ->
            // Convertir las frases a una lista
            val listaFrases = mutableListOf<String>()
            snapshot.children.forEach { fraseSnapshot ->
                fraseSnapshot.getValue(String::class.java)?.let { frase ->
                    listaFrases.add(frase)
                }
            }

            // Seleccionar una frase aleatoria
            if (listaFrases.isNotEmpty()) {
                val fraseAleatoria = listaFrases.random()
                tvFrase.text = "\"$fraseAleatoria\""
            }
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Error al obtener frases", error)
            tvFrase.text = "\"El esfuerzo de hoy es el éxito de mañana.\""  // Frase por defecto
        }
    }

    private fun actualizarProximaReserva() {
        // Referencia a los TextViews

        val tvBloque = findViewById<TextView>(R.id.tvBloque)
        val tvDiaLunes = findViewById<TextView>(R.id.tvDiaLunes)

        val formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fechaHoy = fechaActual.format(formatoFecha)
        val userId = "usuario1"

        database.child("reservas").child(userId)
            .orderByChild("fecha")
            .startAt(fechaHoy)
            .limitToFirst(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Obtener la primera reserva
                    val reservaSnapshot = snapshot.children.first()
                    val reserva = reservaSnapshot.getValue(Reserva::class.java)

                    reserva?.let {
                        tvBloque.text = "Bloque: ${it.hora_inicio} - ${it.hora_final}"
                        tvDiaLunes.text = "Día: ${it.dia} ${it.fecha}"
                    }
                } else {
                    tvBloque.text = "No hay reservas próximas"
                    tvDiaLunes.text = ""
                }
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Error al obtener reserva próxima", error)
                tvBloque.text = "Error al cargar reserva"
                tvDiaLunes.text = ""
            }
    }
}