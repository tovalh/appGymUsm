package com.example.gymapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.BloqueHorario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReservasActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private var selectedBloque: BloqueHorario? = null
    private var currentDaySelected: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reserva_horario)
        FirebaseApp.initializeApp(this)
        initializeDatabase()
        initializeRecyclerView()

        // Inicializar con el día actual
        val fechaActual = LocalDateTime.now()
        val diaActual = fechaActual.dayOfWeek.value

        // Si es domingo (día 7), establecer el día actual como Lunes
        currentDaySelected = if (diaActual == 7) {
            "Lunes"
        } else {
            val diasDeLaSemana = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")
            diasDeLaSemana[diaActual - 1]
        }

        fetchMenuItems()
        botonMenu()
        setupButtons()
        initializeTextView()
        bloquearBotones()
    }

    private fun initializeTextView() {
        val fechaActual = LocalDateTime.now()
        val diaActual = fechaActual.dayOfWeek.value

        // Si es domingo, mostrar información del lunes
        val diasDeLaSemana = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo")
        val diaSeleccionado = if (diaActual == 7) {
            "Lunes"
        } else {
            diasDeLaSemana[diaActual - 1]
        }

        val formatoFecha = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES"))
        val fechaSeleccionada = calcularFechaSeleccionada(if (diaActual == 7) 1 else diaActual)
        val fechaFormateada = fechaSeleccionada.format(formatoFecha)

        val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
        txtFechaSeleccionada.text = "Día $diaSeleccionado $fechaFormateada"
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

    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewTimeBlocks)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = MyAdapter(emptyList()) { bloqueSeleccionado ->
            handleBloqueSelection(bloqueSeleccionado)
        }
        recyclerView.adapter = adapter
    }

    private fun handleBloqueSelection(bloqueSeleccionado: BloqueHorario) {
        selectedBloque = bloqueSeleccionado
        Toast.makeText(
            this,
            "Bloque seleccionado: ${bloqueSeleccionado.hora_inicio}",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun actualizarTextViewFecha(dia: String) {
        val diaNumerico = when (dia) {
            "Lunes" -> 1
            "Martes" -> 2
            "Miercoles" -> 3
            "Jueves" -> 4
            "Viernes" -> 5
            "Sabado" -> 6
            else -> 1
        }

        val fechaSeleccionada = calcularFechaSeleccionada(diaNumerico)
        val formatoFecha = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES"))
        val fechaFormateada = fechaSeleccionada.format(formatoFecha)

        val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
        txtFechaSeleccionada.text = "Día $dia $fechaFormateada"
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnLunes).setOnClickListener {
            filterbloqueHorarios("Lunes")
        }

        findViewById<Button>(R.id.btnMartes).setOnClickListener {
            filterbloqueHorarios("Martes")
        }

        findViewById<Button>(R.id.btnMiercoles).setOnClickListener {
            filterbloqueHorarios("Miercoles")
        }

        findViewById<Button>(R.id.btnJueves).setOnClickListener {
            filterbloqueHorarios("Jueves")
        }

        findViewById<Button>(R.id.btnViernes).setOnClickListener {
            filterbloqueHorarios("Viernes")
        }

        findViewById<Button>(R.id.btnSabado).setOnClickListener {
            filterbloqueHorarios("Sabado")
        }

        findViewById<Button>(R.id.ConfirmabtnReserva).setOnClickListener {
            confirmarReserva()
        }
    }

    private fun fetchMenuItems() {
        database.child("bloqueHorarios").get()
            .addOnSuccessListener { snapshot ->
                val bloques = mutableListOf<BloqueHorario>()
                for (itemSnapshot in snapshot.children) {
                    val bloque = itemSnapshot.getValue(BloqueHorario::class.java)
                    if (bloque != null && bloque.dia == currentDaySelected) {
                        bloque.id = itemSnapshot.key ?: ""
                        if (bloque.cupos_disponibles > 0) {
                            bloques.add(bloque)
                        }
                    }
                }
                updateUI(bloques)
                actualizarTextViewFecha(currentDaySelected)
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener los datos", exception)
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(bloqueHorarios: List<BloqueHorario>) {
        adapter.updateBloques(bloqueHorarios)
    }

    private fun filterbloqueHorarios(dia: String) {
        currentDaySelected = dia

        database.child("bloqueHorarios").get()
            .addOnSuccessListener { snapshot ->
                val bloques = mutableListOf<BloqueHorario>()
                for (itemSnapshot in snapshot.children) {
                    val bloque = itemSnapshot.getValue(BloqueHorario::class.java)
                    if (bloque != null && bloque.dia == dia) {
                        bloque.id = itemSnapshot.key ?: ""
                        if (bloque.cupos_disponibles > 0) {
                            bloques.add(bloque)
                        }
                    }
                }
                updateUI(bloques)
                actualizarTextViewFecha(dia)
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener los datos", exception)
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calcularFechaSeleccionada(diaSeleccionado: Int): LocalDateTime {
        val fechaActual = LocalDateTime.now()
        val diaActual = fechaActual.dayOfWeek.value

        // Si es domingo, calculamos para la próxima semana
        var diferencia = if (diaActual == 7) {
            diaSeleccionado + (7 - diaActual)
        } else {
            diaSeleccionado - diaActual
        }

        // Si la diferencia es negativa (seleccionamos un día anterior al actual)
        if (diferencia < 0) {
            diferencia += 7 // Suma 7 días para ir a la próxima semana
        }

        return fechaActual.plusDays(diferencia.toLong())
    }

    private fun bloquearBotones() {
        val fechaActual = LocalDateTime.now()
        val diaActual = fechaActual.dayOfWeek.value

        val btnLunes = findViewById<Button>(R.id.btnLunes)
        val btnMartes = findViewById<Button>(R.id.btnMartes)
        val btnMiercoles = findViewById<Button>(R.id.btnMiercoles)
        val btnJueves = findViewById<Button>(R.id.btnJueves)
        val btnViernes = findViewById<Button>(R.id.btnViernes)

        // Si es domingo, no deshabilitar ningún botón ya que mostraremos la próxima semana
        if (diaActual != 7) {
            when (diaActual) {
                2 -> btnLunes.isEnabled = false
                3 -> {
                    btnLunes.isEnabled = false
                    btnMartes.isEnabled = false
                }
                4 -> {
                    btnLunes.isEnabled = false
                    btnMartes.isEnabled = false
                    btnMiercoles.isEnabled = false
                }
                5 -> {
                    btnLunes.isEnabled = false
                    btnMartes.isEnabled = false
                    btnMiercoles.isEnabled = false
                    btnJueves.isEnabled = false
                }
                6 -> {
                    btnLunes.isEnabled = false
                    btnMartes.isEnabled = false
                    btnMiercoles.isEnabled = false
                    btnJueves.isEnabled = false
                    btnViernes.isEnabled = false
                }
            }
        }
    }

    private fun penalizacionActiva(): Boolean {
        var tienePenalizacion = false

        database.child("penalizaciones_activas")
            .child("usuario1")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val fechaActual = LocalDate.now().toString()
                    val fechaFin = snapshot.child("fecha_fin").getValue(String::class.java)
                    if (fechaFin != null) {
                        Log.d("fechaFin",fechaFin)
                    }
                    Log.d("fechaActual",fechaActual)
                    if (fechaFin != null && fechaActual <= fechaFin) {
                        tienePenalizacion = true
                    }
                }
            }

        return tienePenalizacion
    }

    private fun confirmarReserva() {
        if (selectedBloque == null) {
            Toast.makeText(this, "Por favor, selecciona un horario primero", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("penalizaciones_activas")
            .child("usuario1")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val fechaActual = LocalDate.now().toString()
                    val fechaFin = snapshot.child("fecha_fin").getValue(String::class.java)

                    if (fechaFin != null && fechaActual <= fechaFin) {
                        Toast.makeText(this, "No puedes realizar reservas mientras tengas una penalización activa", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                }

                // Si no hay penalización, continúa con la reserva
                val bloqueRef = database.child("bloqueHorarios").child(selectedBloque!!.id)

                bloqueRef.get().addOnSuccessListener { bloqueSnapshot ->
                    val cuposActuales = bloqueSnapshot.child("cupos_disponibles").getValue(Int::class.java) ?: 0

                    if (cuposActuales > 0) {
                        bloqueRef.child("cupos_disponibles").setValue(cuposActuales - 1)
                            .addOnSuccessListener {
                                crearReserva()
                                filterbloqueHorarios(currentDaySelected)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al actualizar cupos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "No hay cupos disponibles en este horario", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error al verificar cupos disponibles: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar penalizaciones: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearReserva() {
        val fechaSeleccionada = calcularFechaSeleccionada(
            when (selectedBloque?.dia) {
                "Lunes" -> 1
                "Martes" -> 2
                "Miercoles" -> 3
                "Jueves" -> 4
                "Viernes" -> 5
                "Sabado" -> 6
                else -> 1
            }
        )

        val formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatoFechaMensaje = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES"))

        val fechaFormateada = fechaSeleccionada.format(formatoFecha)
        val fechaFormateadaMensaje = fechaSeleccionada.format(formatoFechaMensaje)

        database.child("reservas")
            .child("usuario1")
            .get()
            .addOnSuccessListener { snapshot ->
                val sumaReserva = snapshot.childrenCount + 1
                val estadoReservadefault = "Activo"

                val reservaMap = hashMapOf(
                    "dia" to selectedBloque?.dia,
                    "fecha" to fechaFormateada,
                    "hora_final" to selectedBloque?.hora_final,
                    "hora_inicio" to selectedBloque?.hora_inicio,
                    "estado" to estadoReservadefault
                )

                database.child("reservas")
                    .child("usuario1")
                    .child("reserva$sumaReserva")
                    .setValue(reservaMap)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Reserva confirmada para: $fechaFormateadaMensaje",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchMenuItems()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al confirmar la reserva: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }
}