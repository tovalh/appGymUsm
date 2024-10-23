package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
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
        initializeTextView()// Inicializa el textview en dia lunes
    }
    private fun initializeTextView(){

        val formatoFecha = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES"))
        val fechaSeleccionada = calcularFechaSeleccionada(1) // 1 = Lunes
        val fechaFormateada = fechaSeleccionada.format(formatoFecha)

        // Actualiza el TextView con la fecha formateada
        val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
        txtFechaSeleccionada.text = "Lunes, $fechaFormateada"
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

        // Configurar el formato para que los meses aparezcan en español
        val formatoFecha = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES"))

        findViewById<Button>(R.id.btnLunes).setOnClickListener {
            filterbloqueHorarios("Lunes")

            val fechaSeleccionada = calcularFechaSeleccionada(1) // 1 = Lunes
            val fechaFormateada = fechaSeleccionada.format(formatoFecha)

            // Actualiza el TextView con la fecha formateada
            val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
            txtFechaSeleccionada.text = "Lunes, $fechaFormateada"
        }

        findViewById<Button>(R.id.btnMartes).setOnClickListener {
            filterbloqueHorarios("Martes")

            val fechaSeleccionada = calcularFechaSeleccionada(2) // 2 = Martes
            val fechaFormateada = fechaSeleccionada.format(formatoFecha)

            // Actualiza el TextView con la fecha formateada
            val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
            txtFechaSeleccionada.text = "Martes, $fechaFormateada"
        }

        findViewById<Button>(R.id.btnMiercoles).setOnClickListener {
            filterbloqueHorarios("Miercoles")

            val fechaSeleccionada = calcularFechaSeleccionada(3) // 3 = Miércoles
            val fechaFormateada = fechaSeleccionada.format(formatoFecha)

            // Actualiza el TextView con la fecha formateada
            val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
            txtFechaSeleccionada.text = "Miércoles, $fechaFormateada"
        }

        findViewById<Button>(R.id.btnJueves).setOnClickListener {
            filterbloqueHorarios("Jueves")

            val fechaSeleccionada = calcularFechaSeleccionada(4) // 4 = Jueves
            val fechaFormateada = fechaSeleccionada.format(formatoFecha)

            // Actualiza el TextView con la fecha formateada
            val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
            txtFechaSeleccionada.text = "Jueves, $fechaFormateada"
        }

        findViewById<Button>(R.id.btnViernes).setOnClickListener {
            filterbloqueHorarios("Viernes")

            val fechaSeleccionada = calcularFechaSeleccionada(5) // 5 = Viernes
            val fechaFormateada = fechaSeleccionada.format(formatoFecha)

            // Actualiza el TextView con la fecha formateada
            val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
            txtFechaSeleccionada.text = "Viernes, $fechaFormateada"
        }

        findViewById<Button>(R.id.btnSabado).setOnClickListener {
            filterbloqueHorarios("Sabado")

            val fechaSeleccionada = calcularFechaSeleccionada(6) // 6 = Sábado
            val fechaFormateada = fechaSeleccionada.format(formatoFecha)

            // Actualiza el TextView con la fecha formateada
            val txtFechaSeleccionada = findViewById<TextView>(R.id.txtDiaSemana)
            txtFechaSeleccionada.text = "Sábado, $fechaFormateada"
        }

        // Agregar el nuevo botón de confirmar reserva
        findViewById<Button>(R.id.ConfirmabtnReserva).setOnClickListener {
            Log.d("ReservaDebug", "Botón confirmar presionado")
            confirmarReserva()
        }

    }

    // Obtienelos elementos del menú desde Firebase
    private fun fetchMenuItems() {
        // Realiza una consulta a la base de datos Firebase para obtener los datos de "bloqueHorarios"
        database.child("bloqueHorarios").get()
            .addOnSuccessListener { snapshot -> // Listener que se ejecuta si la consulta tiene éxito
                val bloques = mutableListOf<BloqueHorario>() // Lista mutable para almacenar los bloques horarios
                for (itemSnapshot in snapshot.children) { // Itera sobre cada hijo en el snapshot
                    val id = itemSnapshot.key ?: "" // Obtiene el ID del nodo (bloque horario)
                    val dia = itemSnapshot.child("dia").getValue(String::class.java) ?: "" // Obtiene el día
                    val horaInicio = itemSnapshot.child("hora_inicio").getValue(String::class.java) ?: "" // Obtiene la hora de inicio
                    val horaFinal = itemSnapshot.child("hora_final").getValue(String::class.java) ?: "" // Obtiene la hora final
                    val cuposDisponibles = itemSnapshot.child("cupos_disponibles").getValue(Int::class.java) ?: 0 // Obtiene los cupos disponibles

                    // Crea un nuevo objeto BloqueHorario con los datos extraídos
                    val bloque = BloqueHorario(
                        id = id,
                        dia = dia,
                        hora_inicio = horaInicio,
                        hora_final = horaFinal,
                        cupos_disponibles = cuposDisponibles
                    )

                    // Filtra los bloques para que solo se agreguen los del día "Lunes" con cupos disponibles
                    if (bloque.dia == "Lunes" && bloque.cupos_disponibles > 0) {
                        bloques.add(bloque) // Añade el bloque a la lista
                    }
                }
                updateUI(bloques) // Actualiza la interfaz de usuario con los bloques filtrados
            }.addOnFailureListener {
                // Listener que se ejecuta si la consulta falla
                Log.e("Firebase", "Error al obtener los datos", it) // Registra el error
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show() // Muestra un mensaje de error
            }
    }

    // Actualiza la interfaz de usuario con la lista de elementos del bloque
    private fun updateUI(bloqueHorarios: List<BloqueHorario>) {
        adapter.updateBloques(bloqueHorarios) // Actualiza el adaptador con la nueva lista de bloques
    }

    //  filterBloqueHorarios para incluir el ID
    private fun filterbloqueHorarios(dia: String) {
        // Realiza una consulta a la base de datos Firebase para obtener los datos de "bloqueHorarios"
        database.child("bloqueHorarios").get()
            .addOnSuccessListener { snapshot -> // Listener que se ejecuta si la consulta tiene éxito
                val bloques = mutableListOf<BloqueHorario>() // Lista mutable para almacenar los bloques horarios
                for (itemSnapshot in snapshot.children) { // Itera sobre cada hijo en el snapshot
                    val id = itemSnapshot.key ?: "" // Obtiene el ID del nodo (bloque horario)
                    val diaBloque = itemSnapshot.child("dia").getValue(String::class.java) ?: "" // Obtiene el día
                    val horaInicio = itemSnapshot.child("hora_inicio").getValue(String::class.java) ?: "" // Obtiene la hora de inicio
                    val horaFinal = itemSnapshot.child("hora_final").getValue(String::class.java) ?: "" // Obtiene la hora final
                    val cuposDisponibles = itemSnapshot.child("cupos_disponibles").getValue(Int::class.java) ?: 0 // Obtiene los cupos disponibles
                    val estadoReserva = itemSnapshot.child("estadoReserva").getValue(String::class.java)?: ""
                    // Crea un nuevo objeto BloqueHorario con los datos extraídos
                    val bloque = BloqueHorario(
                        id = id,
                        dia = diaBloque,
                        hora_inicio = horaInicio,
                        hora_final = horaFinal,
                        cupos_disponibles = cuposDisponibles,
                        estadoReserva = estadoReserva
                    )

                    // Filtra los bloques para que solo se agreguen los del día especificado con cupos disponibles
                    if (bloque.dia == dia && bloque.cupos_disponibles > 0) {
                        bloques.add(bloque) // Añade el bloque a la lista
                    }
                }
                updateUI(bloques) // Actualiza la interfaz de usuario con los bloques filtrados
            }.addOnFailureListener {
                // Listener que se ejecuta si la consulta falla
                Log.e("Firebase", "Error al obtener los datos", it) // Registra el error
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show() // Muestra un mensaje de error
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
            diferencia += 7 // Suma 7 días para ir a la próxima semana
        }

        // Añade los días de diferencia a la fecha actual y retorna la nueva fecha
        return fechaActual.plusDays(diferencia.toLong())
    }

    // Función que se ejecuta cuando se confirma una reserva
    private fun confirmarReserva() {
        if (selectedBloque == null) {
            // Verifica si se ha seleccionado un bloque
            Toast.makeText(this, "Por favor, selecciona un horario primero", Toast.LENGTH_SHORT).show()
            return // Salir si no hay bloque seleccionado
        }

        // Obtener referencia directa al bloque seleccionado usando su ID
        val bloqueRef = database.child("bloqueHorarios").child(selectedBloque!!.id)

        // Verificar cupos disponibles
        bloqueRef.get().addOnSuccessListener { snapshot -> // Listener que se ejecuta si la consulta tiene éxito
            val cuposActuales = snapshot.child("cupos_disponibles").getValue(Int::class.java) ?: 0 // Obtiene los cupos disponibles actuales

            if (cuposActuales > 0) { // Verifica si hay cupos disponibles
                // Actualizar cupos disponibles
                bloqueRef.child("cupos_disponibles").setValue(cuposActuales - 1) // Reduce el número de cupos disponibles
                    .addOnSuccessListener {
                        // Proceder con la creación de la reserva
                        crearReserva() // Llama a la función para crear la reserva
                    }
                    .addOnFailureListener { e -> // Listener que se ejecuta si falla la actualización
                        Toast.makeText(
                            this,
                            "Error al actualizar cupos: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show() // Muestra un mensaje de error
                    }
            } else {
                Toast.makeText(
                    this,
                    "No hay cupos disponibles en este horario",
                    Toast.LENGTH_SHORT
                ).show() // Muestra un mensaje si no hay cupos disponibles
            }
        }.addOnFailureListener { e -> // Listener que se ejecuta si falla la consulta
            Toast.makeText(
                this,
                "Error al verificar cupos disponibles: ${e.message}",
                Toast.LENGTH_SHORT
            ).show() // Muestra un mensaje de error
        }
    }

    // Nueva función auxiliar para crear la reserva
    private fun crearReserva() {
        val fechaSeleccionada = calcularFechaSeleccionada(
            when (selectedBloque?.dia) {
                "Lunes" -> 1
                "Martes" -> 2
                "Miercoles" -> 3
                "Jueves" -> 4
                "Viernes" -> 5
                "Sabado" -> 6
                else -> 1 // Si el día no coincide, por defecto se asigna Lunes
            }
        )

        val formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Formato de fecha para la base de datos
        val formatoFechaMensaje = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "ES")) // Formato de fecha para mostrar al usuario

        val fechaFormateada = fechaSeleccionada.format(formatoFecha) // Formatea la fecha para la base de datos
        val fechaFormateadaMensaje = fechaSeleccionada.format(formatoFechaMensaje) // Formatea la fecha para el mensaje al usuario

        // Obtiene las reservas existentes del usuario
        database.child("reservas")
            .child("usuario1")
            .get()
            .addOnSuccessListener { snapshot -> // Listener que se ejecuta si la consulta tiene éxito
                val sumaReserva = snapshot.childrenCount + 1 // Cuenta el número de reservas existentes y suma 1 para la nueva
                val estadoReservadefault = "Activo"
                // Crea un mapa con los detalles de la reserva
                val reservaMap = hashMapOf(
                    "dia" to selectedBloque?.dia,
                    "fecha" to fechaFormateada,
                    "hora_final" to selectedBloque?.hora_final,
                    "hora_inicio" to selectedBloque?.hora_inicio,
                    "estado" to estadoReservadefault
                )

                // Añade la nueva reserva a la base de datos
                database.child("reservas")
                    .child("usuario1")
                    .child("reserva$sumaReserva")
                    .setValue(reservaMap)
                    .addOnSuccessListener {
                        // Muestra un mensaje de confirmación
                        Toast.makeText(
                            this,
                            "Reserva confirmada para: $fechaFormateadaMensaje",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Actualizar la UI para reflejar los cambios
                        fetchMenuItems() // Vuelve a cargar los bloques de horario
                    }
                    .addOnFailureListener { e -> // Listener que se ejecuta si falla la creación de la reserva
                        Toast.makeText(
                            this,
                            "Error al confirmar la reserva: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show() // Muestra un mensaje de error
                    }
            }
    }
}