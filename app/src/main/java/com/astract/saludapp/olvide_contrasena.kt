package com.astract.saludapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class olvide_contrasena : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_olvide_contrasena)

        // Inicializa FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Obtiene referencias de vista
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val buttonSendCode = findViewById<Button>(R.id.buttonSendCode)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBackOlvideContrasena)

        // Configura el listener para el botón de enviar
        buttonSendCode.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (isValidEmail(email)) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para el botón de volver
        buttonBack.setOnClickListener {
            // Lógica para volver a la actividad anterior
            onBackPressed()
        }

        // Ajustes para el padding del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Verifica si el correo electrónico no está vacío y es válido
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo de restablecimiento enviado a $email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, OlvideContrasenaEspera::class.java)
                    intent.putExtra("EMAIL", email)
                    startActivity(intent)
                } else {
                    // Manejo de errores detallado
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    handleAuthErrors(errorMessage)
                }
            }
    }

    private fun handleAuthErrors(error: String) {
        // Maneja diferentes tipos de errores de autenticación
        when {
            error.contains("There is no user record corresponding to this identifier.") -> {
                Toast.makeText(this, "No hay usuario asociado a este correo electrónico.", Toast.LENGTH_SHORT).show()
            }
            error.contains("The email address is badly formatted.") -> {
                Toast.makeText(this, "El formato del correo electrónico es incorrecto.", Toast.LENGTH_SHORT).show()
            }
            error.contains("The user may have been deleted.") -> {
                Toast.makeText(this, "Este usuario ha sido eliminado.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Error al enviar el correo: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
