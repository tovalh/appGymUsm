package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gymapp.model.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance().reference

    private var userEmail: String? = null
    private var userName: String? = null
    private var userIsAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        database.child("users").child(email.replace(".", "_")).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Leer valores directamente del snapshot
                    val dbPassword = snapshot.child("password").getValue(String::class.java)
                    val dbIsAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                    val dbUsername = snapshot.child("username").getValue(String::class.java) ?: ""

                    Log.d("LoginActivity", "Valores leídos - isAdmin: $dbIsAdmin, username: $dbUsername")

                    if (dbPassword == password) {
                        userEmail = email
                        userName = dbUsername
                        userIsAdmin = dbIsAdmin

                        if (userIsAdmin) {
                            Toast.makeText(this@LoginActivity, "Bienvenido administrador $userName", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LoginActivity, "Bienvenido usuario $userName", Toast.LENGTH_SHORT).show()
                        }

                        startHomeActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Error de inicio de sesión: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("userEmail", userEmail)
        intent.putExtra("userName", userName)
        intent.putExtra("userIsAdmin", userIsAdmin)
        startActivity(intent)
        finish()
    }
}