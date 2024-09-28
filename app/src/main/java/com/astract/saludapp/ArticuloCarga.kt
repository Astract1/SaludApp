package com.astract.saludapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.astract.saludapp.database.MyDatabaseHelper
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class ArticuloCarga : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var abstractTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var btnGuardar: Button
    private lateinit var btnVerArticulo: Button
    private lateinit var btnVolver: ImageView

    private var articuloUrl: String? = null // Para almacenar la URL del artículo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_articulo_carga)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar las vistas
        titleTextView = findViewById(R.id.tituloArticulo)
        dateTextView = findViewById(R.id.fechaArticulo)
        abstractTextView = findViewById(R.id.informacionArticulo)
        imageView = findViewById(R.id.imageViewA)
        btnGuardar = findViewById(R.id.btnguardarArt)
        btnVerArticulo = findViewById(R.id.btnVerArticulo)
        btnVolver = findViewById(R.id.btnVolverArticulos_Carga)

        // Obtener el ID del artículo desde el Intent
        val articuloId = intent.getIntExtra("ARTICULO_ID", -1)
        if (articuloId == -1) {
            showToast("ID de artículo no válido")
            finish()
            return
        }
        cargarArticulo(articuloId)

        // Funcionalidad para el botón de Volver
        btnVolver.setOnClickListener {
            finish() // Cierra la actividad
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // Aplica las animaciones
        }

        // Funcionalidad para el botón Guardar
        btnGuardar.setOnClickListener {
            showToast("Artículo guardado")
        }

        // Funcionalidad para el botón Ver Artículo
        btnVerArticulo.setOnClickListener {
            if (articuloUrl.isNullOrEmpty()) {
                showToast("URL no disponible")
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(articuloUrl))
                startActivity(intent)
            }
        }
    }

    private fun cargarArticulo(id: Int) {
        val dbHelper = MyDatabaseHelper(this)
        val articulo = dbHelper.getArticuloById(id)

        if (articulo != null) {
            titleTextView.text = articulo.title


            // Aplicar formato a la fecha
            val formattedDate = formatDate(articulo.publishedAt)
            dateTextView.text = "Fecha: $formattedDate"

            abstractTextView.text = articulo.abstract
            articuloUrl = articulo.url // Asignar la URL

            // Cargar la imagen usando Glide
            Glide.with(this)
                .load(articulo.imagenurl)
                .placeholder(R.drawable.no_image)
                .into(imageView)
        } else {
            titleTextView.text = "Artículo no encontrado"
            dateTextView.text = ""
            abstractTextView.text = ""
            showToast("No se pudo cargar el artículo")
        }
    }

    private fun formatDate(fecha: String): String {
        // Ajustar el formato de entrada para que coincida con la fecha que estás recibiendo
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()) // Formato correcto para la fecha
        val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        return try {
            val date = inputFormat.parse(fecha)
            outputFormat.format(date ?: fecha)
        } catch (e: Exception) {
            Log.e("ArticuloCarga", "Error al formatear la fecha: ${e.message}")
            fecha // En caso de error, retornar la fecha sin formato
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
