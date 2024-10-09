package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gymapp.data.database.AppDatabase
import com.example.gymapp.data.entity.Reserva
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
//BBDD
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Inicializar la base de datos
        db = AppDatabase.getDatabase(this)

        // Probar la base de datos
        lifecycleScope.launch {
            try {
                // Insertar una reserva de prueba
                val reservaPrueba = Reserva(
                    fecha = "2024-03-20",
                    hora = "10:00",
                    usuarioId = 1
                )
                db.reservaDao().insertReserva(reservaPrueba)

                // Obtener todas las reservas
                val reservas = db.reservaDao().getAllReservas()
                Log.d("DatabaseTest", "Reservas en la base de datos: ${reservas.size}")

                Toast.makeText(
                    this@HomeActivity,
                    "Base de datos funcionando correctamente",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e("DatabaseTest", "Error al probar la base de datos", e)
                Toast.makeText(
                    this@HomeActivity,
                    "Error al probar la base de datos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        setupBottomNavigation()
        setupReserveButton()
    }

    private fun setupReserveButton() {
        val btnReserve = findViewById<Button>(R.id.btnReserve)
        btnReserve.setOnClickListener {
            val intent = Intent(this, ReservasActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    // Ya estamos en Home
                    true
                }
                R.id.nav_calendar -> {
                    // Navegar a ReservasActivity
                    startActivity(Intent(this, ReservasActivity::class.java))
                    true
                }
                R.id.nav_clock -> {
                    // También podría navegar a ReservasActivity o a otra pantalla de horarios
                    startActivity(Intent(this, HorarioActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Seleccionar el item Home por defecto
        bottomNavigation.selectedItemId = R.id.nav_home
    }
}