package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.BloqueHorario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder


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

    // Esta función recibe un número que representa el día de la semana (1=Lunes, 2=Martes, etc.)
    private fun calcularFechaSeleccionada(diaSeleccionado: Int): LocalDateTime {
        // Obtiene la fecha y hora actual del sistema
        val fechaActual = LocalDateTime.now()

        // Obtiene el número del día de la semana actual (1-7)
        val diaActual = fechaActual.dayOfWeek.value

        // Calcula cuántos días hay de diferencia entre el día seleccionado y el día actual
        var diferencia = diaSeleccionado - diaActual

        // Si la diferencia es negativa (seleccionamos un día anterior al actual)
        if (diferencia < 0) {
            diferencia += 6 // Suma 6 días para ir a la próxima semana
        }

        // Añade los días de diferencia a la fecha actual y retorna la nueva fecha
        return fechaActual.plusDays(diferencia.toLong())
    }

    // Función que se ejecuta cuando se confirma una reserva
    private fun confirmarReserva() {
        // Convierte el día seleccionado de texto a número
        val diaSeleccionado = when(selectedBloque?.dia) {
            "Lunes" -> 1
            "Martes" -> 2
            "Miercoles" -> 3
            "Jueves" -> 4
            "Viernes" -> 5
            "Sabado" -> 6
            else -> 1 // Si hay algún error, usa Lunes por defecto
        }

        // Calcula la fecha del día seleccionado
        val fechaSeleccionada = calcularFechaSeleccionada(diaSeleccionado)

        // Define el formato de fecha que queremos (ejemplo: "21 de Octubre")
        val formatoFecha = DateTimeFormatter.ofPattern("dd 'de' MMMM")
        // Aplica el formato a la fecha seleccionada
        val fechaFormateada = fechaSeleccionada.format(formatoFecha)

        // Verifica si se ha seleccionado un bloque horario
        if (selectedBloque != null) {
            // Crea un mapa con todos los datos de la reserva
            val reservaMap = hashMapOf(
                "fecha_reserva" to fechaFormateada,
                "fecha_hora_creacion" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                "hora_inicio" to selectedBloque?.hora_inicio,
                "hora_fin" to selectedBloque?.hora_final,
                "dia" to selectedBloque?.dia
            )

            // Guarda los datos en Firebase
            database.child("reservas")
                .push()  // Crea una nueva entrada
                .setValue(reservaMap)  // Establece los valores
                .addOnSuccessListener {  // Si se guarda correctamente
                    Toast.makeText(this, "Reserva confirmada para: $fechaFormateada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->  // Si hay un error al guardar
                    Toast.makeText(this, "Error al confirmar la reserva: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Si no se seleccionó ningún horario, muestra un mensaje de error
            Toast.makeText(this, "Por favor, selecciona un horario primero", Toast.LENGTH_SHORT).show()
        }
    }
}
