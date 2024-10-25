package com.astract.saludapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private val dbHelper = MyDatabaseHelper(this)

    private var noticiaUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noticias_carga)


        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Inicializar vistas
        initializeViews()




        // Cargar datos
        val noticiaId = intent.getIntExtra("NOTICIA_ID", -1)
        if (noticiaId != -1) {
            loadNoticiaDetails(noticiaId)
        } else {
            showToast("ID de noticia no válido")
            finish()
            return
        }

        setupClickListeners()
    }

    private fun initializeViews() {
        titleTextView = findViewById(R.id.tituloNoticia)
        dateTextView = findViewById(R.id.fechaNoticia)
        infoTextView = findViewById(R.id.informacionNoticia)
        imageView = findViewById(R.id.imageView)
        btnGuardar = findViewById(R.id.btnguardar)
        btnVerNoticia = findViewById(R.id.btnVerNoticia)
        btnVolver = findViewById(R.id.btnVolver)
    }

    private fun setupClickListeners() {
        btnVolver.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }


        btnGuardar.setOnClickListener {
            val noticiaId = intent.getIntExtra("NOTICIA_ID", -1)
            Log.d(noticiaId.toString(), "NOTICIA ID")

            if (noticiaId != -1) {
                if (dbHelper.isNoticiaSaved(noticiaId)) {
                    // Si ya está guardada, quitarla
                    dbHelper.unSaveNoticia(noticiaId)
                    btnGuardar.text = "Guardar"
                    btnGuardar.setBackgroundColor(getColor(R.color.cyan_book)) // Para establecer el color directamente

                    showToast("Noticia eliminada")
                } else {
                    // Si no está guardada, guardarla
                    dbHelper.saveNoticia(noticiaId)
                    btnGuardar.text = "Guardada"
                    btnGuardar.setBackgroundColor(getColor(R.color.guarado_cyan)) // Para establecer el color directamente

                    showToast("Noticia guardada")
                }
            }
        }


        btnVerNoticia.setOnClickListener {
            noticiaUrl?.let { url ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } ?: showToast("URL no disponible")
        }
    }

    private fun loadNoticiaDetails(id: Int) {
        val dbHelper = MyDatabaseHelper(this)
        val noticia = dbHelper.getNoticiaById(id)

        noticia?.let {
            titleTextView.text = it.title
            val formattedDate = formatDate(it.publishedAt)
            dateTextView.text = "Fecha: $formattedDate"
            infoTextView.text = it.content
            noticiaUrl = it.url

            Glide.with(this)
                .load(it.urlToImage)
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(imageView)


            if (dbHelper.isNoticiaSaved(id)) {
                btnGuardar.text = "Guardada"
                btnGuardar.setBackgroundColor(getColor(R.color.guarado_cyan))
            } else {
                btnGuardar.text = "Guardar"
                btnGuardar.setBackgroundColor(getColor(R.color.cyan_book))

            }
        } ?: run {
            titleTextView.text = "Noticia no encontrada"
            dateTextView.text = ""
            infoTextView.text = ""
            showToast("No se pudo cargar la noticia")
        }
    }

    private fun formatDate(fecha: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        return try {
            val date = inputFormat.parse(fecha)
            outputFormat.format(date ?: return fecha)
        } catch (e: Exception) {
            Log.e("Noticias_Carga", "Error al formatear la fecha: ${e.message}")
            fecha
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
