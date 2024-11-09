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

    // Creacion de Variables
    private var userEmail: String? = null
    private var userName: String? = null
    private var userIsAdmin: Boolean = false

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

        // Obtener los extras del Intent
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")
        userIsAdmin = intent.getBooleanExtra("userIsAdmin", false)
    }

    // Spinner Principal

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

        // Configurar el adaptador para que utilice dos layouts diferentes
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
                actualizarEstadoReserva(username, fechaFormateada, "Inasistido")
                verificarYAplicarPenalizacion(username)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al marcar inasistencia: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verificarYAplicarPenalizacion(username: String) {
        // Obtener el número de inasistencias permitidas de la configuración
        database.child("configuracion_sistema")
            .child("restricciones")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(configSnapshot: DataSnapshot) {
                    val inasistenciasMaximas = configSnapshot.child("inasistencias_maximas").getValue(Int::class.java) ?: 3
                    val diasRestriccion = configSnapshot.child("dias_restriccion").getValue(Int::class.java) ?: 7

                    // Contar inasistencias del usuario en el último mes
                    contarInasistenciasRecientes(username) { inasistenciasActuales ->
                        if (inasistenciasActuales >= inasistenciasMaximas) {
                            aplicarPenalizacion(username, inasistenciasActuales, diasRestriccion)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al obtener configuración", error.toException())
                }
            })
    }

    private fun contarInasistenciasRecientes(username: String, callback: (Int) -> Unit) {
        // Obtener fecha hace un mes
        val fechaInicio = LocalDateTime.now().minusMonths(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val fechaFin = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        database.child("reservas")
            .child(username)
            .orderByChild("fecha")
            .startAt(fechaInicio)
            .endAt(fechaFin)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val inasistencias = snapshot.children.count {
                        it.child("estado").getValue(String::class.java) == "Inasistido"
                    }
                    callback(inasistencias)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al contar inasistencias", error.toException())
                    callback(0)
                }
            })
    }

    private fun aplicarPenalizacion(username: String, inasistencias: Int, diasRestriccion: Int) {
        val fechaInicio = LocalDateTime.now()
        val fechaFin = fechaInicio.plusDays(diasRestriccion.toLong())

        val penalizacion = hashMapOf(
            "fecha_inicio" to fechaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            "fecha_fin" to fechaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            "inasistencias_acumuladas" to inasistencias,
            "motivo" to "Exceso de inasistencias"
        )

        database.child("penalizaciones_activas")
            .child(username)
            .setValue(penalizacion)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Penalización aplicada por exceso de inasistencias",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al aplicar penalización", e)
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

    // ABRIR MODAL

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
                setDimAmount(0.5f)
            }

            // Obtener referencias de las vistas
            val etHorasMaximas = findViewById<EditText>(R.id.HorasMaximas)
            val etInasistencias = findViewById<EditText>(R.id.Inasistencias)
            val spinnerRestriccion = findViewById<Spinner>(R.id.spinnerRestriccion)
            val btnGuardar = findViewById<Button>(R.id.btnGuardar)

            // Configurar el spinner con valores del 1 al 31
            val diasList = (1..31).map { it.toString() }
            val spinnerAdapter = ArrayAdapter(this@AdminActivity, android.R.layout.simple_spinner_item, diasList)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRestriccion.adapter = spinnerAdapter

            // Cargar valores de configuración
            cargarConfiguracion { config ->
                etHorasMaximas.setText(config.horasMaximas.toString())
                etInasistencias.setText(config.inasistenciasMaximas.toString())
                // Establecer el valor del spinner (restamos 1 porque los índices empiezan en 0)
                spinnerRestriccion.setSelection(config.diasRestriccion - 1)
            }

            // Configurar el botón de guardar
            btnGuardar.setOnClickListener {
                val horasMaximas = etHorasMaximas.text.toString().toIntOrNull() ?: 2
                val inasistenciasMaximas = etInasistencias.text.toString().toIntOrNull() ?: 3
                val diasRestriccion = spinnerRestriccion.selectedItem.toString().toInt()

                guardarConfiguracion(
                    horasMaximas,
                    inasistenciasMaximas,
                    diasRestriccion
                ) { exitoso ->
                    if (exitoso) {
                        Toast.makeText(context, "Configuración guardada exitosamente", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(context, "Error al guardar la configuración", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            findViewById<View>(R.id.btnCerrar)?.setOnClickListener {
                dismiss()
            }

            setCanceledOnTouchOutside(true)
            show()
        }
    }

    // Modelo de Item -> Pasarlo a Modelo

    private data class ConfiguracionSistema(
        val horasMaximas: Int,
        val inasistenciasMaximas: Int,
        val diasRestriccion: Int
    )


    // Cargar Configuracion desde BBDD

    private fun cargarConfiguracion(onComplete: (ConfiguracionSistema) -> Unit) {
        database.child("configuracion_sistema")
            .child("restricciones")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val horasMaximas = snapshot.child("horas_maximas_cancelacion").getValue(Int::class.java) ?: 2
                    val inasistenciasMaximas = snapshot.child("inasistencias_maximas").getValue(Int::class.java) ?: 3
                    val diasRestriccion = snapshot.child("dias_restriccion").getValue(Int::class.java) ?: 7

                    onComplete(ConfiguracionSistema(horasMaximas, inasistenciasMaximas, diasRestriccion))
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al cargar configuración", error.toException())
                    onComplete(ConfiguracionSistema(2, 3, 7)) // Valores por defecto
                }
            })
    }

    //Boton Guardar

    private fun guardarConfiguracion(
        horasMaximas: Int,
        inasistenciasMaximas: Int,
        diasRestriccion: Int,
        onComplete: (Boolean) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "horas_maximas_cancelacion" to horasMaximas,
            "inasistencias_maximas" to inasistenciasMaximas,
            "dias_restriccion" to diasRestriccion
        )

        database.child("configuracion_sistema")
            .child("restricciones")
            .updateChildren(updates)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }



    private fun botonMenu() {
        val menuNavegacion = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        menuNavegacion.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intentHome = Intent(this, HomeActivity::class.java)
                    intent.putExtra("userEmail", userEmail)
                    intent.putExtra("userName", userName)
                    intent.putExtra("userIsAdmin", userIsAdmin)
                    startActivity(intentHome)
                    true
                }

                R.id.nav_calendar -> {
                    val intentCalendar = Intent(this, ReservasActivity::class.java)
                    intent.putExtra("userEmail", userEmail)
                    intent.putExtra("userName", userName)
                    intent.putExtra("userIsAdmin", userIsAdmin)
                    startActivity(intentCalendar)
                    true
                }

                R.id.nav_clock -> {
                    val intentReloj = Intent(this, HorarioActivity::class.java)
                    intent.putExtra("userEmail", userEmail)
                    intent.putExtra("userName", userName)
                    intent.putExtra("userIsAdmin", userIsAdmin)
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