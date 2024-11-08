package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.snap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.BloqueHorario
import com.example.gymapp.model.Reserva
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HorarioActivity: AppCompatActivity(){

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaptadorReserva

    // Datos usuario Activo
    private var userEmail: String? = null
    private var userName: String? = null
    private var userIsAdmin: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tuhorario)
        FirebaseApp.initializeApp(this)

        // Obtener los extras del Intent
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")
        userIsAdmin = intent.getBooleanExtra("userIsAdmin", false)

        // Inicializar RecyclerView y Adapter con lista vacía
        recyclerView = findViewById(R.id.recyclerViewDays)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdaptadorReserva(emptyList()) { reserva ->
            cancelarReserva(reserva)
        }
        recyclerView.adapter = adapter

        initializeDatabase()

        // Verificar si tenemos userName antes de fetch
        if (userName.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se pudo obtener el nombre de usuario", Toast.LENGTH_LONG).show()
            Log.e("HorarioActivity", "userName is null or empty")
            return
        }

        fetchMenuItems()
        botonMenu()
    }

    // Inicializa la referencia a la base de datos de Firebase
    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    // Obtienelos elementos del menú desde Firebase
    private fun fetchMenuItems() {
        userName?.let {
            database.child("reservas")
                .child(it)
                .get().addOnSuccessListener { snapshot -> // Obtiene datos del nodo
                val reservasLista = mutableListOf<Reserva>() // Crea una lista para almacenar los elementos del menú
                for (itemSnapshot in snapshot.children) { // Itera a través de los datos obtenidos
                    val reserva = itemSnapshot.getValue(Reserva::class.java) // Convierte los datos a un objeto MenuItem
                    if (reserva != null && reserva.estado == "Activo") {
                        reserva.id = itemSnapshot.key ?: ""
                        reservasLista.add(reserva) // Agrega el elemento del menú a la lista
                    }
                }
                    // Ordenar la lista por fecha antes de actualizar la UI
                reservasLista.sortBy { it.fecha }
                updateUI(reservasLista) // Actualiza la interfaz de usuario con los elementos del menú obtenidos
            }.addOnFailureListener {
                Log.e("Firebase", "Error al obtener los datos", it) // Registra el error si falla la obtención de datos
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show() // Muestra un mensaje de error
            }
        }
    }

    // Actualiza la interfaz de usuario con la lista de elementos del menú
    private fun updateUI(reservasLista: List<Reserva>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDays) // Obtiene el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this) // Establece el layout manager
        val adapter = AdaptadorReserva(reservasLista) { reserva ->
            cancelarReserva(reserva)
        }
        recyclerView.adapter = adapter // Establece el adaptador para el RecyclerView
    }
    private fun cancelarReserva(reserva: Reserva) {
        // Validacion de Horas config con hora actual
        if (!puedesCancelarReserva(reserva)) {
            Toast.makeText(this, "No se puede cancelar con menos de 2 horas de anticipación", Toast.LENGTH_SHORT).show()
            return
        }
        // Usamos la estructura correcta del JSON para actualizar
        userName?.let {
            database.child("reservas")
                .child(it)
                .child(reserva.id) // Aquí deberías tener una forma de identificar la reserva específica
                .child("estado")
                .setValue("Cancelada")
                .addOnSuccessListener {
                    Toast.makeText(this, "Reserva cancelada exitosamente", Toast.LENGTH_SHORT).show()
                    fetchMenuItems() // Recargar la lista de reservas
                    restaurarCupo(reserva)
                    fetchMenuItems()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cancelar la reserva", Toast.LENGTH_SHORT).show()
                    Log.e("Firebase", "Error al cancelar la reserva", it)
                }
        }

    }

    private fun puedesCancelarReserva(reserva: Reserva): Boolean {
        try {
            val fechaHoraReserva = "${reserva.fecha} ${reserva.hora_inicio}"
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val fechaReserva = formatter.parse(fechaHoraReserva)
            val ahora = Calendar.getInstance().time

            // Calcular diferencia en horas
            val diferencia = fechaReserva.time - ahora.time
            val diferenciaHoras = diferencia / (1000 * 60 * 60.0)

            return diferenciaHoras > 2

        } catch (e: Exception) {
            Log.e("Firebase", "Error al validar tiempo de cancelación", e)
            return false
        }
    }

    private fun restaurarCupo(reserva: Reserva) {

        database.child("bloqueHorarios").get()
            .addOnSuccessListener { snapshot ->
                val bloques =
                    mutableListOf<BloqueHorario>()
                for (itemSnapshot in snapshot.children) { // Itera a través de los datos obtenidos
                    val bloque =
                        itemSnapshot.getValue(BloqueHorario::class.java)
                    if (bloque != null && bloque.dia == reserva.dia && bloque.hora_inicio == reserva.hora_inicio) {
                        // Loopea otra vez hasta encontrar el bloque correcto!!
                        database.child("bloqueHorarios")
                            .child(itemSnapshot.key ?: "") // Usamos la key del bloque
                            .child("cupos_disponibles")
                            .setValue(bloque.cupos_disponibles + 1)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Cupo restaurado exitosamente")
                            }
                            .addOnFailureListener { error ->
                                Log.e("Firebase", "Error al restaurar cupo", error)
                            }
                        break
                    }
                }
            }
    }


    private fun botonMenu() {
        val menuNavegacion = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        menuNavegacion.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intentHome = Intent(this, HomeActivity::class.java)
                    intentHome.putExtra("userEmail", userEmail)
                    intentHome.putExtra("userName", userName)
                    intentHome.putExtra("userIsAdmin", userIsAdmin)
                    startActivity(intentHome)
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
                    true
                }
                else -> false
            }
        }
    }
}