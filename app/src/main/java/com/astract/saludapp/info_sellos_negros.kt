package com.astract.saludapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class info_sellos_negros : AppCompatActivity() {

    private lateinit var ivSello: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvResumen: TextView
    private lateinit var tvCaracteristicas: TextView
    private lateinit var tvDetalles: TextView
    private lateinit var tvRecomendaciones: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info_sellos_negros)

        // Inicializa las vistas
        ivSello = findViewById(R.id.iv_sello)
        tvTitulo = findViewById(R.id.tv_titulo)
        tvResumen = findViewById(R.id.tv_resumen)
        tvCaracteristicas = findViewById(R.id.tv_caracteristicas)
        tvDetalles = findViewById(R.id.tv_detalles)
        tvRecomendaciones = findViewById(R.id.tv_recomendaciones)

        // Obtiene el ID del sello desde el Intent
        val selloId = intent.getStringExtra("sello_id") ?: return

        // Carga los detalles del sello
        loadSelloDetails(selloId)

        // Listener para el botón de regreso
        findViewById<ImageButton>(R.id.btn_volver).setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rectangulo)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadSelloDetails(selloId: String) {
        val inputStream = assets.open("sellos.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = bufferedReader.use { it.readText() }

        val gson = Gson()
        val listType = object : TypeToken<SellosResponse>() {}.type
        val response: SellosResponse = gson.fromJson(jsonString, listType)

        // Verifica el selloId recibido
        println("selloId: $selloId")

        // Encuentra el sello por ID
        val sello = response.sellos.find { it.id.toString() == selloId }

        // Depuración adicional
        if (sello == null) {
            println("No se encontró el sello con ID: $selloId")
            return
        } else {
            println("Sello encontrado: ${sello.nombre}")
        }

        // Establece los datos en las vistas
        tvTitulo.text = sello.nombre
        tvResumen.text = sello.resumen
        tvCaracteristicas.text = "Características:\n${sello.caracteristicas.joinToString("\n")}"
        tvDetalles.text = "Detalles:\n${sello.detalles}"
        tvRecomendaciones.text = "Recomendaciones:\n${sello.recomendaciones}"

        // Carga la imagen usando Glide
        Glide.with(this)
            .load(sello.imagen_url)
            .into(ivSello)
    }
}
