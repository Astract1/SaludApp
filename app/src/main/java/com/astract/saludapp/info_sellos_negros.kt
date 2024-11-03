package com.astract.saludapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore

class info_sellos_negros : AppCompatActivity() {

    private lateinit var ivSello: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvResumen: TextView
    private lateinit var tvCaracteristicas: TextView
    private lateinit var tvDetalles: TextView
    private lateinit var tvRecomendaciones: TextView
    private val db = FirebaseFirestore.getInstance()


    companion object {
        private const val IMAGE_WIDTH =400
        private const val IMAGE_HEIGHT = 400
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info_sellos_negros)

        initializeViews()
        setupImageView()
        setupWindowInsets()
        setupBackButton()

        intent.getStringExtra("sello_id")?.let { selloId ->
            loadSelloDetails(selloId)
        } ?: run {
            showError("ID del sello no encontrado")
            finish()
        }
    }

    private fun initializeViews() {
        ivSello = findViewById(R.id.iv_sello)
        tvTitulo = findViewById(R.id.tv_titulo)
        tvResumen = findViewById(R.id.tv_resumen)
        tvCaracteristicas = findViewById(R.id.tv_caracteristicas)
        tvDetalles = findViewById(R.id.tv_detalles)
        tvRecomendaciones = findViewById(R.id.tv_recomendaciones)
    }

    private fun setupImageView() {
        // Configurar el ImageView con los parámetros deseados
        ivSello.apply {
            layoutParams = layoutParams.apply {
                width = IMAGE_WIDTH
                height = IMAGE_HEIGHT
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rectangulo)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_volver).setOnClickListener {
            finish()
        }
    }

    private fun loadSelloDetails(selloId: String) {
        db.collection("sellos")
            .document(selloId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val sello = document.toObject(SelloNegro::class.java)
                    sello?.let {
                        updateUI(it)
                    } ?: run {
                        showError("Error al convertir los datos del sello")
                        finish()
                    }
                } else {
                    showError("Sello no encontrado")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                showError("Error al cargar los datos: ${e.message}")
                finish()
            }
    }

    private fun updateUI(sello: SelloNegro) {
        with(sello) {
            tvTitulo.text = nombre
            tvResumen.text = resumen
            tvCaracteristicas.text = buildString {
                append("Características:\n")
                append(caracteristicas.joinToString("\n"))
            }
            tvDetalles.text = buildString {
                append("Detalles:\n")
                append(detalles)
            }
            tvRecomendaciones.text = buildString {
                append("Recomendaciones:\n")
                append(recomendaciones)
            }

            loadImage(imagen_url)
        }
    }

    private fun loadImage(imageUrl: String) {
        val requestOptions = RequestOptions()
            .override(IMAGE_WIDTH, IMAGE_HEIGHT)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.no_image)
            .error(R.drawable.no_image)

        Glide.with(this)
            .load(imageUrl)
            .apply(requestOptions)
            .into(ivSello)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
