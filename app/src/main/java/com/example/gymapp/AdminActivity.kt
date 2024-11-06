package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.BloqueHorario
import com.example.gymapp.model.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdminActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdministradorAdapter
    private lateinit var spinnerBloques: Spinner
    private lateinit var bloquesAdapter: ArrayAdapter<String>
    private var currentDaySelected: String = ""
    private var currentDate: String = ""
    private var bloquesList = mutableListOf<BloqueHorario>()
    private var bloquesMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admi)

        currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        initializeFirebase()
        initializeViews()
        setupInitialDay()
        setupNavigationAndUI()
    }

    private fun initializeFirebase() {
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().reference
    }

    private fun initializeViews() {
        initializeRecyclerView()
        initializeSpinner()
    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerAdmi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdministradorAdapter(emptyList()) { usuario ->
            mostrarDetallesUsuario(usuario)
        }
        recyclerView.adapter = adapter
    }

    private fun mostrarDetallesUsuario(usuario: Usuario) {
        // Aquí puedes mostrar un diálogo o navegar a una nueva actividad con los detalles del usuario
        Toast.makeText(this, "Usuario seleccionado: ${usuario.nombre}", Toast.LENGTH_SHORT).show()
    }

    private fun initializeSpinner() {
        spinnerBloques = findViewById(R.id.spinnerBloques)
        bloquesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        bloquesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBloques.adapter = bloquesAdapter
        setupSpinnerListener()
    }

    private fun setupSpinnerListener() {
        spinnerBloques.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val bloqueNombre = parent.getItemAtPosition(position).toString()
                val bloqueId = bloquesMap[bloqueNombre]
                bloqueId?.let { fetchUsuarios(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupInitialDay() {
        val fechaActual = LocalDateTime.now()
        val diaActual = fechaActual.dayOfWeek.value
        currentDaySelected = if (diaActual == 7) {
            "Lunes"
        } else {
            val diasDeLaSemana = listOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")
            diasDeLaSemana[diaActual - 1]
        }
    }

    private fun setupNavigationAndUI() {
        setupButtons()
        botonMenu()
        initializeTextView()
        bloquearBotones()
        fetchBloquesHorarios()
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
                    val intentCalendar = Intent(this, ReservasActivity::class.java)
                    startActivity(intentCalendar)
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

    private fun fetchBloquesHorarios() {
        val fechaSeleccionada = calcularFechaSeleccionada(when(currentDaySelected) {
            "Lunes" -> 1
            "Martes" -> 2
            "Miercoles" -> 3
            "Jueves" -> 4
            "Viernes" -> 5
            "Sabado" -> 6
            else -> 1
        })
        currentDate = fechaSeleccionada.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // Obtenemos todos los bloques y filtramos en el cliente
        database.child("bloqueHorarios").get()
            .addOnSuccessListener { bloquesSnapshot ->
                bloquesList.clear()
                bloquesMap.clear()
                val bloquesNombres = mutableListOf<String>()

                for (bloqueSnapshot in bloquesSnapshot.children) {
                    val bloque = bloqueSnapshot.getValue(BloqueHorario::class.java)
                    // Filtramos por el día actual
                    if (bloque != null && bloque.dia == currentDaySelected) {
                        bloque.id = bloqueSnapshot.key ?: ""
                        bloquesList.add(bloque)

                        val bloqueNombre = "${bloque.hora_inicio} - ${bloque.hora_final}"
                        bloquesNombres.add(bloqueNombre)
                        bloquesMap[bloqueNombre] = bloque.id
                    }
                }

                // Verificamos las asistencias para estos bloques
                verificarAsistencias(bloquesNombres)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener los bloques horarios", exception)
                Toast.makeText(this, "Error al obtener los bloques horarios", Toast.LENGTH_SHORT).show()
            }

    }

    private fun verificarAsistencias(bloquesNombres: List<String>) {
        database.child("asistencias").child(currentDate).get()
            .addOnSuccessListener { asistenciasSnapshot ->
                updateUIWithBloques(bloquesNombres)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al verificar asistencias", exception)
                Toast.makeText(this, "Error al verificar asistencias", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUIWithBloques(bloquesNombres: List<String>) {
        bloquesAdapter.clear()
        bloquesAdapter.addAll(bloquesNombres)
        bloquesAdapter.notifyDataSetChanged()

        if (bloquesNombres.isNotEmpty()) {
            spinnerBloques.setSelection(0)
            val primerBloqueId = bloquesMap[bloquesNombres[0]]
            primerBloqueId?.let { fetchUsuarios(it) }
        } else {
            adapter.updateUsuarios(emptyList())
        }

        actualizarTextViewFecha(currentDaySelected)
    }

    private fun fetchUsuarios(bloqueId: String) {
        // Intentamos obtener los usuarios de las asistencias
        database.child("asistencias").child(currentDate).child(bloqueId).child("usuarios").get()
            .addOnSuccessListener { asistenciasSnapshot ->
                val usuariosIds = asistenciasSnapshot.children.mapNotNull { it.key }
                Log.d("fetchUsuarios", "Usuarios encontrados: $usuariosIds")

                if (usuariosIds.isNotEmpty()) {
                    obtenerDetallesUsuarios(usuariosIds)
                } else {
                    adapter.updateUsuarios(emptyList())
                    Toast.makeText(this, "No se encontraron usuarios para el bloque seleccionado.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener las asistencias", exception)
                Toast.makeText(this, "Error al obtener las asistencias", Toast.LENGTH_SHORT).show()
            }
    }


    private fun obtenerDetallesUsuarios(usuariosIds: List<String>) {
        val usuariosList = mutableListOf<Usuario>()
        var usuariosCompletados = 0

        for (usuarioId in usuariosIds) {
            database.child("usuarios").child(usuarioId).get()
                .addOnSuccessListener { usuarioSnapshot ->
                    val usuario = usuarioSnapshot.getValue(Usuario::class.java)
                    if (usuario != null) {
                        usuario.userId = usuarioId
                        usuariosList.add(usuario)
                    }

                    usuariosCompletados++
                    if (usuariosCompletados == usuariosIds.size) {
                        adapter.updateUsuarios(usuariosList)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Error al obtener detalles del usuario", exception)
                    usuariosCompletados++
                    if (usuariosCompletados == usuariosIds.size) {
                        adapter.updateUsuarios(usuariosList)
                    }
                }
        }
    }

    private fun filterbloqueHorarios(dia: String) {
        currentDaySelected = dia
        fetchBloquesHorarios()
    }

    private fun calcularFechaSeleccionada(diaSeleccionado: Int): LocalDateTime {
        val fechaActual = LocalDateTime.now()
        val diaActual = fechaActual.dayOfWeek.value

        var diferencia = if (diaActual == 7) {
            diaSeleccionado + (7 - diaActual)
        } else {
            diaSeleccionado - diaActual
        }

        if (diferencia < 0) {
            diferencia += 7
        }

        return fechaActual.plusDays(diferencia.toLong())
    }

    private fun initializeTextView() {
        val fechaActual = LocalDateTime.now()
        val formatoFecha = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES"))
        val fechaSeleccionada = calcularFechaSeleccionada(if (fechaActual.dayOfWeek.value == 7) 1 else fechaActual.dayOfWeek.value)

        findViewById<TextView>(R.id.txtDiaSemana).text =
            "Día $currentDaySelected ${fechaSeleccionada.format(formatoFecha)}"
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

        findViewById<TextView>(R.id.txtDiaSemana).text =
            "Día $dia ${fechaSeleccionada.format(formatoFecha)}"
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
}