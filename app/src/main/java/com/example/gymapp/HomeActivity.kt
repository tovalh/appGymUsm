package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

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


    }
}