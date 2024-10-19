package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class HorarioActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tuhorario)
        FirebaseApp.initializeApp(this)

//        setupButtons()
        botonMenu()
    }

    private fun botonMenu() {
        val menuNavegacion = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        menuNavegacion.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intentHome = Intent(this, HomeActivity::class.java)
                    startActivity(intentHome)
                    true
                }
                R.id.nav_calendar -> {
                    val intentCalendario = Intent(this, ReservasActivity::class.java)
                    startActivity(intentCalendario)
                    true
                }
                R.id.nav_clock -> {
                    true
                }
                else -> false
            }
        }
    }
}