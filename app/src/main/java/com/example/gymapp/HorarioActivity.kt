package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.View
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class HorarioActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horario)

        setupBottomNavigation()
        setupDayCards()
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    // Navegar a ReservasActivity
                    startActivity(Intent(this, ReservasActivity::class.java))
                    true
                }
                R.id.nav_clock -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDayCards() {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val cardIds = listOf(R.id.mondayCard, R.id.tuesdayCard, R.id.wednesdayCard,
            R.id.thursdayCard, R.id.fridayCard, R.id.saturdayCard)

        for (i in days.indices) {
            val cardView = findViewById<MaterialCardView>(cardIds[i])
            val dayTextView = cardView.findViewById<TextView>(R.id.dayText)
            dayTextView.text = days[i]

            // Configuración adicional del card...

            cardView.setOnClickListener {
                // Manejar el clic en la tarjeta
            }
        }
    }

    // You might have additional methods here, such as:
    // private fun getClassTimeForDay(day: String): String {
    //     // Logic to return class times for a given day
    // }

    // Additional methods for handling navigation, data loading, etc.
}