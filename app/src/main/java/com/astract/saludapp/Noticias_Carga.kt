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

class Noticias_Carga : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var infoTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var btnGuardar: Button
    private lateinit var btnVerNoticia: Button
    private lateinit var btnVolver: ImageView

    private var noticiaUrl: String? = null // Para almacenar la URL de la noticia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noticias_carga)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        titleTextView = findViewById(R.id.tituloNoticia)
        dateTextView = findViewById(R.id.fechaNoticia)
        infoTextView = findViewById(R.id.informacionNoticia)
        imageView = findViewById(R.id.imageView)
        btnGuardar = findViewById(R.id.btnguardar)
        btnVerNoticia = findViewById(R.id.btnVerNoticia)
        btnVolver = findViewById(R.id.btnVolver)

        val noticiaId = intent.getIntExtra("NOTICIA_ID", -1)
        if (noticiaId == -1) {
            showToast("ID de noticia no válido")
            finish()
            return
        }
        loadNoticiaDetails(noticiaId)

        // Funcionalidad para el botón de Volver
        btnVolver.setOnClickListener {
            finish() // Cierra la actividad
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            ) // Aplica las animaciones
        }

        // Funcionalidad para el botón Guardar
        btnGuardar.setOnClickListener {
            showToast("Noticia guardada")
        }

        // Funcionalidad para el botón Ver Noticia
        btnVerNoticia.setOnClickListener {
            if (noticiaUrl.isNullOrEmpty()) {
                showToast("URL no disponible")
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(noticiaUrl))
                startActivity(intent)
            }
        }
    }

    private fun loadNoticiaDetails(id: Int) {
        val dbHelper = MyDatabaseHelper(this)
        val noticia = dbHelper.getNoticiaById(id)

        if (noticia != null) {
            titleTextView.text = noticia.title

            // Log para depuración de la fecha
            Log.d("Noticias_Carga", "Fecha recibida: ${noticia.publishedAt}")

            // Aplicar formato a la fecha
            val formattedDate = formatDate(noticia.publishedAt)
            dateTextView.text = "Fecha: $formattedDate"

            infoTextView.text = noticia.content
            noticiaUrl = noticia.url // Asignar la URL

            // Cargar la imagen usando Glide
            Glide.with(this)
                .load(noticia.urlToImage)
                .placeholder(R.drawable.no_image)
                .into(imageView)
        } else {
            titleTextView.text = "Noticia no encontrada"
            dateTextView.text = ""
            infoTextView.text = ""
            showToast("No se pudo cargar la noticia")
        }
    }

    private fun formatDate(fecha: String): String {
        val inputFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()) // Formato ISO 8601
        val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        return try {
            val date = inputFormat.parse(fecha)
            outputFormat.format(date ?: fecha)
        } catch (e: Exception) {
            Log.e("Noticias_Carga", "Error al formatear la fecha: ${e.message}")
            fecha // En caso de error, retornar la fecha sin formato
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
