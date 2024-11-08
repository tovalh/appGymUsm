package com.example.gymapp

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class AdminActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var currentDaySelected: String = ""
    private lateinit var adaptador: AdaptadorUsuario
    private var usuarios = mutableListOf<Usuario>()
    private lateinit var recyclerView: RecyclerView
    private var horarioSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admi)
        FirebaseApp.initializeApp(this)
        initializeDatabase()

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

        initializeRecyclerView()
        crearSpinner()
        botonMenu()
        setupButtons()
        initializeTextView()
        bloquearBotones()
    }

    private fun crearSpinner() {
        val horarios = arrayOf(
            "8:20-9:30",
            "9:35-10:45",
            "10:50-12:00",
            "12:05-13:15",
            "13:20-14:30",
            "14:35-15:45",
            "15:50-17:00",
            "17:05-18:15",
            "18:20-19:30",
            "19:35-20:45",
            "20:50-22:00"
        )

        val spinner = findViewById<Spinner>(R.id.spinnerBloques)

        // Configura el adaptador para que utilice dos layouts diferentes
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,       // Vista principal con el ícono
            R.id.spinnerText,            // ID del TextView para ambos layouts
            horarios
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Vista desplegable sin el ícono
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                horarioSeleccionado = horarios[position]
                cargarUsuarios() // Cargar usuarios cuando cambie el horario
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                horarioSeleccionado = ""
            }
        }
    }

    private fun cargarUsuarios() {
        val fechaFormateada = calcularFechaSeleccionada(
            when (currentDaySelected) {
                "Lunes" -> 1
                "Martes" -> 2
                "Miercoles" -> 3
                "Jueves" -> 4
                "Viernes" -> 5
                "Sabado" -> 6
                else -> 1
            }
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        database.child("asistencias")
            .child(fechaFormateada)
            .child(horarioSeleccionado)
            .child("usuarios")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaUsuarios = mutableListOf<Usuario>()
                    for (usuarioSnapshot in snapshot.children) {
                        val nombreUsuario = usuarioSnapshot.key ?: ""
                        val asistio = usuarioSnapshot.child("asistio").getValue(Boolean::class.java) ?: false
                        val horaMarcacion = usuarioSnapshot.child("hora_marcacion").getValue(String::class.java) ?: ""

                        listaUsuarios.add(Usuario(nombreUsuario, asistio, horaMarcacion))
                    }
                    actualizarRecyclerView(listaUsuarios)
                    Log.d("Firebase", "Usuarios cargados: ${listaUsuarios.size}") // Para debug
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al cargar usuarios", error.toException())
                }
            })
    }

    // Agrega esta función para inicializar el RecyclerView en onCreate
    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerAdmi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorUsuario(
            usuarios,
            onAsistenciaClick = { usuario ->
                marcarAsistencia(usuario.username)
            },
            onCancelarClick = { usuario ->
                marcarInasistencia(usuario.username)
            }
        )
        recyclerView.adapter = adaptador
    }

    private fun marcarAsistencia(username: String) {
        // Obtener la fecha formateada para la base de datos
        val fechaFormateada = calcularFechaSeleccionada(
            when (currentDaySelected) {
                "Lunes" -> 1
                "Martes" -> 2
                "Miercoles" -> 3
                "Jueves" -> 4
                "Viernes" -> 5
                "Sabado" -> 6
                else -> 1
            }
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // Actualizar asistencia
        val asistenciaRef = database.child("asistencias")
            .child(fechaFormateada)
            .child(horarioSeleccionado)
            .child("usuarios")
            .child(username)

        val updates = hashMapOf<String, Any>(
            "asistio" to true,
            "hora_marcacion" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        )

        asistenciaRef.updateChildren(updates)
            .addOnSuccessListener {
                // Actualizar estado de la reserva
                actualizarEstadoReserva(username, fechaFormateada, "Asistido")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al marcar asistencia: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun marcarInasistencia(username: String) {
        // Obtener la fecha formateada para la base de datos
        val fechaFormateada = calcularFechaSeleccionada(
            when (currentDaySelected) {
                "Lunes" -> 1
                "Martes" -> 2
                "Miercoles" -> 3
                "Jueves" -> 4
                "Viernes" -> 5
                "Sabado" -> 6
                else -> 1
            }
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // Actualizar asistencia
        val asistenciaRef = database.child("asistencias")
            .child(fechaFormateada)
            .child(horarioSeleccionado)
            .child("usuarios")
            .child(username)

        val updates = hashMapOf<String, Any>(
            "asistio" to false,
            "hora_marcacion" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        )

        asistenciaRef.updateChildren(updates)
            .addOnSuccessListener {
                // Actualizar estado de la reserva
                actualizarEstadoReserva(username, fechaFormateada, "Inasistido")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al marcar inasistencia: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarEstadoReserva(username: String, fecha: String, nuevoEstado: String) {
        // Buscar la reserva correspondiente
        database.child("reservas")
            .child(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Buscar la reserva que coincida con la fecha
                    for (reservaSnapshot in snapshot.children) {
                        val fechaReserva = reservaSnapshot.child("fecha").getValue(String::class.java)
                        if (fechaReserva == fecha) {
                            // Actualizar el estado
                            reservaSnapshot.ref.child("estado").setValue(nuevoEstado)
                                .addOnSuccessListener {
                                    Toast.makeText(this@AdminActivity,
                                        "Estado actualizado correctamente",
                                        Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@AdminActivity,
                                        "Error al actualizar estado: ${e.message}",
                                        Toast.LENGTH_SHORT).show()
                                }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminActivity,
                        "Error al buscar reserva: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarRecyclerView(nuevosUsuarios: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(nuevosUsuarios)
        adaptador.notifyDataSetChanged()
        Log.d("RecyclerView", "Actualizando con usuarios: ${nuevosUsuarios.map { it.username }}")
    }


    private fun initializeDatabase() {
        database = FirebaseDatabase.getInstance().reference
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


    private fun setupButtons() {
        findViewById<ImageButton>(R.id.btnAjustes).setOnClickListener {
            showAjustesDialog()
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

    private fun showAjustesDialog() {
        Dialog(this).apply {
            setContentView(R.layout.asistencia)

            // Configurar la ventana del diálogo
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setGravity(Gravity.CENTER)
                setWindowAnimations(android.R.style.Animation_Dialog)
                setDimAmount(0.5f)  // Oscurecer el fondo
            }

            // Si quieres manejar el click fuera del diálogo
            setCanceledOnTouchOutside(true)

            // Para agregar un botón de cerrar dentro del diálogo:
            findViewById<View>(R.id.btnCerrar)?.setOnClickListener {
                dismiss()
            }

            show()
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

    private fun filterbloqueHorarios(dia: String) {
        currentDaySelected = dia
        actualizarTextViewFecha(dia)
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