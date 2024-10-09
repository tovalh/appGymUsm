package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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