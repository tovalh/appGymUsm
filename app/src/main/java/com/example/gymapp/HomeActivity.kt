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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    // Datos usuario Activo
    private var userEmail: String? = null
    private var userName: String? = null
    private var userIsAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        FirebaseApp.initializeApp(this)
        initializeDatabase()
        setupButtons()
        botonMenu()
        actualizarProximaReserva()

        // Obtener los extras del Intent
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")
        userIsAdmin = intent.getBooleanExtra("userIsAdmin", false)
    }

    override fun onResume() {
        super.onResume()
        mostrarFraseAleatoria()
    }

    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    private fun setupButtons() {
        val reservaHorarioBoton = findViewById<Button>(R.id.btnReserva)
        reservaHorarioBoton.setOnClickListener {
            irReservas()
        }

        // Agregamos el botón de administrador
        val botonAdmi = findViewById<Button>(R.id.botonAdmi)
        botonAdmi.setOnClickListener {
            irPaginaAdmi()
        }
    }

    // Nueva función para navegar a la página de administrador
    private fun irPaginaAdmi() {
        val intent = Intent(this, AdminActivity::class.java)
        intent.putExtra("userEmail", userEmail)
        intent.putExtra("userName", userName)
        startActivity(intent)
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
                    intentCalendario.putExtra("userEmail", userEmail)
                    intentCalendario.putExtra("userName", userName)
                    startActivity(intentCalendario)
                    true
                }

                R.id.nav_clock -> {
                    val intentReloj = Intent(this, HorarioActivity::class.java)
                    intentReloj.putExtra("userEmail", userEmail)
                    intentReloj.putExtra("userName", userName)
                    startActivity(intentReloj)
                    true
                }

                else -> false
            }
        }
    }

    private fun irReservas() {
        val intent = Intent(this, ReservasActivity::class.java)
        intent.putExtra("userEmail", userEmail)
        intent.putExtra("userName", userName)
        startActivity(intent)
    }

    private fun mostrarFraseAleatoria() {
        val tvFrase = findViewById<TextView>(R.id.tvQuote)

        database.child("frases_motivacionales").get().addOnSuccessListener { snapshot ->
            val listaFrases = mutableListOf<String>()

            snapshot.children.forEach { fraseSnapshot ->
                fraseSnapshot.getValue(String::class.java)?.let { frase ->
                    listaFrases.add(frase)
                }
            }

            if (listaFrases.isNotEmpty()) {
                val fraseAleatoria = listaFrases.random()
                tvFrase.text = "\"$fraseAleatoria\""
            } else {
                tvFrase.text = "\"No hay frases disponibles.\""
            }
        }.addOnFailureListener {
            tvFrase.text = "\"El esfuerzo de hoy es el éxito de mañana.\""
        }
    }

    private fun actualizarProximaReserva() {
        val tvBloque = findViewById<TextView>(R.id.tvBloque)
        val tvDiaLunes = findViewById<TextView>(R.id.tvDiaLunes)

        val formatoFecha = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES"))
        val formatoFechaHoy = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fechaHoy = LocalDateTime.now().format(formatoFechaHoy)
        val userId = "usuario1"

        database.child("reservas").child(userId)
            .orderByChild("fecha")
            .startAt(fechaHoy)
            .get()
            .addOnSuccessListener { snapshot ->
                var proximaReservaEncontrada = false
                for (reservaSnapshot in snapshot.children) {
                    val reserva = reservaSnapshot.getValue(Reserva::class.java)
                    if (reserva?.estado == "Activo") {
                        proximaReservaEncontrada = true

                        tvBloque.text = "Bloque ${reserva.hora_inicio} - ${reserva.hora_final}"

                        val fechaReserva = LocalDate.parse(
                            reserva.fecha,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        )
                        val fechaFormateada = fechaReserva.format(formatoFecha)

                        tvDiaLunes.text = "${reserva.dia} $fechaFormateada"
                        break
                    }
                }

                if (!proximaReservaEncontrada) {
                    mostrarMensajeSinReserva()
                }
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Error al obtener reserva próxima", error)
            }
    }

    private fun mostrarMensajeSinReserva() {
        val tvBloque = findViewById<TextView>(R.id.tvBloque)
        val tvDiaLunes = findViewById<TextView>(R.id.tvDiaLunes)

        tvBloque.text = "Reservar bloque"
        tvDiaLunes.text = "Reservar Dia"
    }
}