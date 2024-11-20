// LoginActivity.kt
package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance().reference

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
        // Validación básica de campos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Sanitizar el email para usarlo como clave en Firebase
        val sanitizedEmail = email.replace(".", "_")

        database.child("users").child(sanitizedEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val dbPassword = snapshot.child("password").getValue(String::class.java)
                        val dbIsAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                        val dbUsername = snapshot.child("username").getValue(String::class.java) ?: ""

                        if (dbPassword == password) {
                            // Inicio de sesión exitoso
                            redirectUser(email, dbUsername, dbIsAdmin)
                        } else {
                            Toast.makeText(this@LoginActivity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun redirectUser(email: String, username: String, isAdmin: Boolean) {
        val intent = Intent(this,
            if (isAdmin) AdminActivity::class.java
            else HomeActivity::class.java
        )

        intent.putExtra("userEmail", email)
        intent.putExtra("userName", username)
        intent.putExtra("userIsAdmin", isAdmin)

        startActivity(intent)
        finish()
    }
}