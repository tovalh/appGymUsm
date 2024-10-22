package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgymusm.model.BloqueHorario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservasActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private var selectedBloque: BloqueHorario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reserva_horario) // Establece el layout para esta actividad
        FirebaseApp.initializeApp(this)
        initializeDatabase() // Inicializa la referencia a la base de datos de Firebase
        initializeRecyclerView() // inicializar RecyclerView
        fetchMenuItems() // Obtiene los elementos del menú desde la base de datos
        botonMenu()         // Navegacion barra menu abajo
        setupButtons() // Configura los listeners de clic para los botones
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



    // Inicializa la referencia a la base de datos de Firebase
    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    // Recycler View vacio
    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewTimeBlocks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(emptyList()) { bloqueSeleccionado ->
            handleBloqueSelection(bloqueSeleccionado)
        }
        recyclerView.adapter = adapter
    }

    //Logica para bloque seleccionado
    private fun handleBloqueSelection(bloqueSeleccionado: BloqueHorario) {
        selectedBloque = bloqueSeleccionado
        Toast.makeText(
            this,
            "Bloque seleccionado: ${bloqueSeleccionado.hora_inicio}",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Configura los listeners de clic para los botones de billetera, carrito y filtros
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
        // Agregar el nuevo botón de confirmar reserva
        findViewById<Button>(R.id.ConfirmabtnReserva).setOnClickListener {
            Log.d("ReservaDebug", "Botón confirmar presionado")
            confirmarReserva()
        }

    }

    // Obtienelos elementos del menú desde Firebase
    private fun fetchMenuItems() {
        database.child("bloqueHorarios").get().addOnSuccessListener { snapshot -> // Obtiene datos del nodo "menuItems"
            val bloques = mutableListOf<BloqueHorario>() // Crea una lista para almacenar los elementos del menú
            for (itemSnapshot in snapshot.children) { // Itera a través de los datos obtenidos
                val bloque = itemSnapshot.getValue(BloqueHorario::class.java) // Convierte los datos a un objeto MenuItem
                if (bloque != null && bloque.dia == "Lunes") {
                    bloques.add(bloque) // Agrega el elemento del menú a la lista
                }
            }
            updateUI(bloques) // Actualiza la interfaz de usuario con los elementos del menú obtenidos
        }.addOnFailureListener {
            Log.e("Firebase", "Error al obtener los datos", it) // Registra el error si falla la obtención de datos
            Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show() // Muestra un mensaje de error
        }
    }

    // Actualiza la interfaz de usuario con la lista de elementos del Bloque
    private fun updateUI(bloqueHorarios: List<BloqueHorario>) {
        adapter.updateBloques(bloqueHorarios)
    }
    // Filtra los elementos del menú según la categoría seleccionada
    private fun filterbloqueHorarios(dia: String) {
        database.child("bloqueHorarios").get().addOnSuccessListener { snapshot -> // Obtiene datos del nodo "menuItems"
            val bloques = mutableListOf<BloqueHorario>() // Crea una lista para almacenar los elementos del menú
            for (itemSnapshot in snapshot.children) { // Itera a través de los datos obtenidos
                val bloque = itemSnapshot.getValue(BloqueHorario::class.java) // Convierte los datos a un objeto MenuItem
                if (bloque != null && bloque.dia == dia) {
                    bloques.add(bloque)
                }

            }
            updateUI(bloques)
        }.addOnFailureListener {
            Log.e("Firebase", "Error al obtener los datos", it)
            Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarReserva() {
        // Verificar si hay un bloque seleccionado
        if (selectedBloque == null) {
            Toast.makeText(this, "Por favor selecciona un horario", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el ID del usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Debes iniciar sesión para reservar", Toast.LENGTH_SHORT).show()
            return
        }

        // Calcular la fecha para la reserva
        val calendar = Calendar.getInstance()
        val diaActual = calendar.get(Calendar.DAY_OF_WEEK)

        // Mapear días de la semana a valores numéricos (Domingo = 1, Lunes = 2, ..., Sábado = 7)
        val mapaDias = mapOf(
            "Lunes" to 1,
            "Martes" to 2,
            "Miercoles" to 3,
            "Jueves" to 4,
            "Viernes" to 5,
            "Sabado" to 6
        )

        val diaSeleccionadoNum = mapaDias[selectedBloque!!.dia] ?: return
        var diferencia = diaSeleccionadoNum - diaActual

        // Si la diferencia es negativa, añadir 7 días para ir a la próxima semana
        if (diferencia < 0) {
            diferencia += 7
        }

        // Establecer la fecha de reserva
        calendar.add(Calendar.DAY_OF_YEAR, diferencia)
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = formatoFecha.format(calendar.time)

        // Referencia a la base de datos de reservas
        val reservasRef = database.child("reservas").child(userId)

        // Crear objeto de reserva
        val reserva = hashMapOf(
            "dia" to selectedBloque!!.dia,
            "fecha" to fechaFormateada,
            "hora_inicio" to selectedBloque!!.hora_inicio,
            "hora_fin" to selectedBloque!!.hora_final,
            "timestamp" to ServerValue.TIMESTAMP
        )

        // Verificar si ya existe una reserva para ese día
        reservasRef
            .orderByChild("fecha")
            .equalTo(fechaFormateada)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(
                        this,
                        "Ya tienes una reserva para el día $fechaFormateada",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Guardar la reserva en Firebase
                    reservasRef.push().setValue(reserva)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Reserva confirmada para el $fechaFormateada",
                                Toast.LENGTH_SHORT
                            ).show()
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
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al verificar reservas existentes: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    }
