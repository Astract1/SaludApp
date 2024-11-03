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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
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
    private val firestore = FirebaseFirestore.getInstance()
    private val sharedViewModel: SharedViewModel by viewModels()

    private var articuloUrl: String? = null
    private var userId: String? = null
    private var isArticuloGuardado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articulo_carga)

        // Configurar la barra de estado
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        userId = sharedViewModel.getUserId()
        if (userId == null) {
            showToast("Error: Usuario no identificado")
            finish()
            return
        }

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
        articuloUrl = intent.getStringExtra("ARTICULO_URL")
        if (articuloUrl.isNullOrEmpty()) {
            showToast("URL de artículo no válida")
            finish()
            return
        }
        cargarArticulo(articuloUrl!!)
        checkIfArticuloGuardado()
    }

    private fun setupClickListeners() {
        btnVolver.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnGuardar.setOnClickListener {
            toggleGuardarArticulo()
        }

        btnVerArticulo.setOnClickListener {
            if (articuloUrl.isNullOrEmpty()) {
                showToast("URL no disponible")
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(articuloUrl)))
            }
        }
    }

    private fun sanitizeUrl(url: String): String {
        return url.replace("https://", "").replace("http://", "").replace("/", "_")
    }

    private fun cargarArticulo(url: String) {
        val documentId = sanitizeUrl(url)
        Log.d("ArticuloCarga", "Intentando cargar documento con ID: $documentId")

        firestore.collection("articulos").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val titulo = document.getString("title") ?: "Título no disponible"
                    val fecha = document.getString("publishedAt") ?: "Fecha no disponible"
                    val abstracto = document.getString("abstract") ?: "Resumen no disponible"

                    titleTextView.text = titulo
                    dateTextView.text = "Fecha: ${formatDate(fecha)}"
                    abstractTextView.text = abstracto

                    val imagenUrl = document.getString("imagenurl")
                    Glide.with(this)
                        .load(imagenUrl)
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(imageView)
                } else {
                    showToast("No se pudo cargar el artículo")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ArticuloCarga", "Error al cargar artículo: ${e.message}")
                showToast("Error al cargar el artículo")
            }
    }


    private fun checkIfArticuloGuardado() {
        articuloUrl?.let { url ->
            userId?.let { uid ->
                firestore.collection("users")
                    .document(uid)
                    .collection("articulos_guardados")
                    .document(generateDocumentIdFromUrl(url))
                    .get()
                    .addOnSuccessListener { document ->
                        isArticuloGuardado = document.exists()
                        updateGuardarButtonState()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ArticuloCarga", "Error checking saved status: ${e.message}")
                    }
            }
        }
    }

    private fun updateGuardarButtonState() {
        if (isArticuloGuardado) {
            btnGuardar.text = "Guardado"
            btnGuardar.setBackgroundColor(getColor(R.color.guarado_cyan))
        } else {
            btnGuardar.text = "Guardar"
            btnGuardar.setBackgroundColor(getColor(R.color.cyan_book))
        }
    }

    private fun toggleGuardarArticulo() {
        userId?.let { uid ->
            articuloUrl?.let { url ->
                val documentId = generateDocumentIdFromUrl(url)
                val articuloRef = firestore.collection("users")
                    .document(uid)
                    .collection("articulos_guardados")
                    .document(documentId)

                if (isArticuloGuardado) {
                    // Eliminar artículo guardado
                    articuloRef.delete()
                        .addOnSuccessListener {
                            isArticuloGuardado = false
                            updateGuardarButtonState()
                            showToast("Artículo eliminado de guardados")
                        }
                        .addOnFailureListener { e ->
                            showToast("Error al eliminar el artículo")
                            Log.e("ArticuloCarga", "Error eliminando artículo: ${e.message}")
                        }
                } else {
                    // Guardar artículo
                    val articuloData = hashMapOf(
                        "url" to url,
                        "title" to titleTextView.text.toString(),
                        "fecha" to dateTextView.text.toString(),
                        "abstract" to abstractTextView.text.toString(),
                        "timestamp" to System.currentTimeMillis()
                    )

                    articuloRef.set(articuloData)
                        .addOnSuccessListener {
                            isArticuloGuardado = true
                            updateGuardarButtonState()
                            showToast("Artículo guardado exitosamente")
                        }
                        .addOnFailureListener { e ->
                            showToast("Error al guardar el artículo")
                            Log.e("ArticuloCarga", "Error guardando artículo: ${e.message}")
                        }
                }
            } ?: showToast("Error: URL no disponible")
        } ?: showToast("Error: Usuario no identificado")
    }

    private fun generateDocumentIdFromUrl(url: String): String {
        return url.replace("https://", "")
            .replace("http://", "")
            .replace("/", "_")
            .replace(".", "_")
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
