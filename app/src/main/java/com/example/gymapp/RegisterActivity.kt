package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            registerUser(email, password, name)
        }
    }

    private fun registerUser(email: String, password: String, name: String) {
        val newUser = hashMapOf(
            "email" to email,
            "password" to password,
            "username" to name,
            "isAdmin" to false
        )

        database.child("users").child(email.replace(".", "_")).setValue(newUser)
            .addOnSuccessListener {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startHomeActivity(email, name)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error de registro: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startHomeActivity(email: String, name: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("userEmail", email)
        intent.putExtra("userName", name)
        startActivity(intent)
        finish()
    }
}