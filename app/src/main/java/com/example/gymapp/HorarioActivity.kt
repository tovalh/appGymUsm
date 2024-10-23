package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.Reserva
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HorarioActivity: AppCompatActivity(){

    private lateinit var database: DatabaseReference

    // Esto es necesario??
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaptadorReserva

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tuhorario)
        FirebaseApp.initializeApp(this)
        initializeDatabase() // Inicializa la referencia a la base de datos de Firebase
        fetchMenuItems()
        botonMenu()
    }

    // Inicializa la referencia a la base de datos de Firebase
    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    // Obtienelos elementos del menú desde Firebase
    private fun fetchMenuItems() {
        database.child("reservas")
            .child("usuario1")
            .get().addOnSuccessListener { snapshot -> // Obtiene datos del nodo "menuItems"
            val reservasLista = mutableListOf<Reserva>() // Crea una lista para almacenar los elementos del menú
            for (itemSnapshot in snapshot.children) { // Itera a través de los datos obtenidos
                val reserva = itemSnapshot.getValue(Reserva::class.java) // Convierte los datos a un objeto MenuItem
                if (reserva != null) {
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
        // Usamos la estructura correcta del JSON para actualizar
        database.child("reservas")
            .child("usuario1")
            .child("reserva2") // Aquí deberías tener una forma de identificar la reserva específica
            .child("estado")
            .setValue("cancelado")
            .addOnSuccessListener {
                Toast.makeText(this, "Reserva cancelada exitosamente", Toast.LENGTH_SHORT).show()
                fetchMenuItems() // Recargar la lista de reservas
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cancelar la reserva", Toast.LENGTH_SHORT).show()
                Log.e("Firebase", "Error al cancelar la reserva", it)
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