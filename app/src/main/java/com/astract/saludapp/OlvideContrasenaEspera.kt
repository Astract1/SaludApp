package com.astract.saludapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OlvideContrasenaEspera : AppCompatActivity() {
    private lateinit var textViewEmail: TextView
    private lateinit var textViewCountdown: TextView
    private lateinit var buttonBackEspera: ImageButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_olvide_contrasena_espera)

        // Obtiene el correo electrónico enviado desde la actividad anterior
        val email = intent.getStringExtra("EMAIL")

        // Actualiza el TextView con el correo electrónico
        textViewEmail = findViewById(R.id.textViewEmail)
        textViewEmail.text = "Correo: $email"

        // Inicializa el botón de volver
        buttonBackEspera = findViewById(R.id.buttonBackEspera)
        buttonBackEspera.setOnClickListener {
            finish() //
        }

        // Inicializa el ProgressBar
        progressBar = findViewById(R.id.progressBar)

        // Inicializa el TextView para el conteo regresivo
        textViewCountdown = findViewById(R.id.textViewGoBack)

        startCountdown(10)
    }

    private fun startCountdown(seconds: Int) {
        object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textViewCountdown.text = "Regresando en ${millisUntilFinished / 1000} segundos..."
            }

            override fun onFinish() {
                // Redirigir a la actividad de login
                val intent = Intent(this@OlvideContrasenaEspera, Login::class.java)
                startActivity(intent)
                finish() // Finaliza la actividad actual
            }
        }.start()
    }
}
