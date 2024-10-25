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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private val dbHelper = MyDatabaseHelper(this)

    private var articuloUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articulo_carga)

        // Configurar la barra de estado
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        initializeViews()
        setupArticulo()
        setupClickListeners()
    }

    private fun initializeViews() {
        titleTextView = findViewById(R.id.tituloArticulo)
        dateTextView = findViewById(R.id.fechaArticulo)
        abstractTextView = findViewById(R.id.informacionArticulo)
        imageView = findViewById(R.id.imageViewA)
        btnGuardar = findViewById(R.id.btnguardarArt)
        btnVerArticulo = findViewById(R.id.btnVerArticulo)
        btnVolver = findViewById(R.id.btnVolverArticulos_Carga)
    }

    private fun setupArticulo() {
        val articuloId = intent.getIntExtra("ARTICULO_ID", -1)
        if (articuloId == -1) {
            showToast("ID de artículo no válido")
            finish()
            return
        }
        cargarArticulo(articuloId)
    }

    private fun setupClickListeners() {
        btnVolver.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnGuardar.setOnClickListener {
            val articuloId = intent.getIntExtra("ARTICULO_ID", -1)
            Log.d(articuloId.toString(), "Articulo ID")
            if (articuloId != -1) {
                // Verificar si el artículo ya está guardado
                if (dbHelper.isAriculoSaved(articuloId)) {
                    dbHelper.unSaveArticulo(articuloId)
                    btnGuardar.text = "Guardar"
                    btnGuardar.setBackgroundColor(getColor(R.color.cyan_book)) // Para establecer el color de guardado
                    showToast("Artículo eliminado")
                } else {
                    dbHelper.saveArticulo(articuloId)
                    btnGuardar.text = "Guardado"
                    btnGuardar.setBackgroundColor(getColor(R.color.guarado_cyan)) // Para establecer el color de guardado
                    showToast("Artículo guardado")
                }
            }
        }

        btnVerArticulo.setOnClickListener {
            if (articuloUrl.isNullOrEmpty()) {
                showToast("URL no disponible")
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(articuloUrl)))
            }
        }
    }

    private fun cargarArticulo(id: Int) {
        val articulo = dbHelper.getArticuloById(id)

        articulo?.let {
            titleTextView.text = it.title
            dateTextView.text = "Fecha: ${formatDate(it.publishedAt)}"
            abstractTextView.text = it.abstract
            articuloUrl = it.url

            Glide.with(this)
                .load(it.imagenurl)
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(imageView)

            // Cambiar el texto y color del botón según el estado guardado
            if (dbHelper.isAriculoSaved(id)) {
                btnGuardar.text = "Guardada"
                btnGuardar.setBackgroundColor(ContextCompat.getColor(this, R.color.guarado_cyan)) // Color de artículo guardado
            } else {
                btnGuardar.text = "Guardar"
                btnGuardar.setBackgroundColor(ContextCompat.getColor(this, R.color.cyan_book)) // Color por defecto
            }

        } ?: run {
            // Manejo de artículo no encontrado
            titleTextView.text = "Artículo no encontrado"
            dateTextView.text = ""
            abstractTextView.text = ""
            showToast("No se pudo cargar el artículo")
        }
    }


    private fun formatDate(fecha: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        return try {
            val date = inputFormat.parse(fecha)
            outputFormat.format(date ?: return fecha)
        } catch (e: Exception) {
            Log.e("ArticuloCarga", "Error al formatear la fecha: ${e.message}")
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
