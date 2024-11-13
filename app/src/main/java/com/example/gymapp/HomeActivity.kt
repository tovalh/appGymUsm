package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gymapp.model.Reserva
import com.example.gymapp.model.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
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
        // Obtener los extras del Intent
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")
        userIsAdmin = intent.getBooleanExtra("userIsAdmin", false)

        // Agregar logs de debug
        Log.d("HomeActivity", "userEmail: $userEmail")
        Log.d("HomeActivity", "userName: $userName")
        Log.d("HomeActivity", "userIsAdmin: $userIsAdmin")

        //Botones Admin - Usuario
        setupButtons()
        botonMenu()

        // Actualizar información del usuario
        actualizarProximaReserva()
        calcularPorcentajeAsistencia()
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

        val botonAdmi = findViewById<Button>(R.id.botonAdmi)
        botonAdmi.visibility = if (userIsAdmin) android.view.View.VISIBLE else android.view.View.GONE

        if (userIsAdmin) {
            botonAdmi.setOnClickListener {
                irPaginaAdmi()
            }
        }
    }

    private fun irPaginaAdmi() {
        val intent = Intent(this, AdminActivity::class.java)
        intent.putExtra("userEmail", userEmail)
        intent.putExtra("userName", userName)
        intent.putExtra("userIsAdmin", userIsAdmin)
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
                    intentCalendario.putExtra("userIsAdmin", userIsAdmin)
                    startActivity(intentCalendario)
                    true
                }
                R.id.nav_clock -> {
                    val intentReloj = Intent(this, HorarioActivity::class.java)
                    intentReloj.putExtra("userEmail", userEmail)
                    intentReloj.putExtra("userName", userName)
                    intentReloj.putExtra("userIsAdmin", userIsAdmin)
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
        intent.putExtra("userIsAdmin", userIsAdmin)
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
        val fechaHoy = LocalDate.now()

        // Usar el nombre del usuario en lugar del email
        val userNombre = userName ?: return

        database.child("reservas").child(userNombre)
            .get()
            .addOnSuccessListener { snapshot ->
                var proximaReserva: Reserva? = null
                var fechaMasCercana: LocalDate? = null

                snapshot.children.forEach { reservaSnapshot ->
                    val reserva = reservaSnapshot.getValue(Reserva::class.java)
                    if (reserva != null && reserva.estado == "Activo") {
                        val fechaReserva = LocalDate.parse(
                            reserva.fecha,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        )

                        // Solo considerar fechas futuras o de hoy
                        if (!fechaReserva.isBefore(fechaHoy)) {
                            // Si no tenemos una fecha más cercana o esta fecha es más cercana que la actual
                            if (fechaMasCercana == null || fechaReserva.isBefore(fechaMasCercana)) {
                                fechaMasCercana = fechaReserva
                                proximaReserva = reserva
                            }
                        }
                    }
                }

                if (proximaReserva != null) {
                    tvBloque.text = "Bloque ${proximaReserva!!.hora_inicio} - ${proximaReserva!!.hora_final}"
                    val fechaFormateada = fechaMasCercana?.format(formatoFecha) ?: ""
                    tvDiaLunes.text = "${proximaReserva!!.dia} $fechaFormateada"
                } else {
                    mostrarMensajeSinReserva()
                }
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Error al obtener reserva próxima", error)
                mostrarMensajeSinReserva()
            }
    }

    // También deberíamos actualizar calcularPorcentajeAsistencia() para usar el nombre
    private fun calcularPorcentajeAsistencia() {
        val tvPorcentajeAsistencia = findViewById<TextView>(R.id.tvPorcentajeAsistencia)

        val userNombre = userName ?: return

        database.child("reservas").child(userNombre).get().addOnSuccessListener { snapshot ->
            var totalReservas = 0
            var totalAsistencias = 0

            snapshot.children.forEach { reservaSnapshot ->
                val reserva = reservaSnapshot.getValue(Reserva::class.java)
                if (reserva != null) {
                    totalReservas++
                    if (reserva.estado == "Asistido") {
                        totalAsistencias++
                    }
                }
            }

            if (totalReservas > 0) {
                val porcentajeAsistencia = (totalAsistencias * 100) / totalReservas
                tvPorcentajeAsistencia.text = "$porcentajeAsistencia%"
            } else {
                tvPorcentajeAsistencia.text = "N/A"
            }
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Error al obtener porcentaje de asistencia", error)
            tvPorcentajeAsistencia.text = "Error"
        }
    }



    private fun mostrarMensajeSinReserva() {
        val tvBloque = findViewById<TextView>(R.id.tvBloque)
        val tvDiaLunes = findViewById<TextView>(R.id.tvDiaLunes)

        tvBloque.text = "Sin reservas próximas"
        tvDiaLunes.text = "Reserva tu próxima clase"
    }
}