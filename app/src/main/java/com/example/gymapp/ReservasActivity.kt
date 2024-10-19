package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgymusm.model.BloqueHorario
import com.example.gymapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ReservasActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reserva_horario) // Establece el layout para esta actividad
        FirebaseApp.initializeApp(this)
        initializeDatabase() // Inicializa la referencia a la base de datos de Firebase
        fetchMenuItems() // Obtiene los elementos del menú desde la base de datos
        botonMenu()
//        setupButtons() // Configura los listeners de clic para los botones
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

    // Obtienelos elementos del menú desde Firebase
    private fun fetchMenuItems() {
        database.child("bloqueHorarios").get().addOnSuccessListener { snapshot -> // Obtiene datos del nodo "menuItems"
            val bloques = mutableListOf<BloqueHorario>() // Crea una lista para almacenar los elementos del menú
            for (itemSnapshot in snapshot.children) { // Itera a través de los datos obtenidos
                val bloque = itemSnapshot.getValue(BloqueHorario::class.java) // Convierte los datos a un objeto MenuItem
                if (bloque != null) {
                    bloques.add(bloque) // Agrega el elemento del menú a la lista
                }
            }
//            updateUI(bloques) // Actualiza la interfaz de usuario con los elementos del menú obtenidos
        }.addOnFailureListener {
            Log.e("Firebase", "Error al obtener los datos", it) // Registra el error si falla la obtención de datos
            Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show() // Muestra un mensaje de error
        }
    }

//    // Actualiza la interfaz de usuario con la lista de elementos del menú
//    private fun updateUI(bloqueHorarios: List<BloqueHorario>) {
//        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTimeBlocks) // Obtiene el RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(this) // Establece el layout manager
//        val adapter = MyAdapter(menuItems) { menuItem -> // Crea un adaptador para el RecyclerView
//            addToCart(menuItem) // Agrega el elemento al carrito cuando se hace clic
//        }
//        recyclerView.adapter = adapter // Establece el adaptador para el RecyclerView
//    }
}