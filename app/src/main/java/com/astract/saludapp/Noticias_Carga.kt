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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    private val sharedViewModel: SharedViewModel by viewModels()

    private var noticiaUrl: String? = null
    private var userId: String? = null
    private var isNoticiaGuardada = false
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noticias_carga)

        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        userId = sharedViewModel.getUserId()
        if (userId == null) {
            showToast("Error: Usuario no identificado")
            finish()
            return
        }

        db = Firebase.firestore
        initializeViews()

        noticiaUrl = intent.getStringExtra("NOTICIA_URL")
        if (noticiaUrl != null) {
            loadNoticiaDetailsByUrl(noticiaUrl!!)
            checkIfNoticiaGuardada()
        } else {
            showToast("URL de noticia no válida")
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
            noticiaUrl?.let { url ->
                toggleGuardarNoticia(url)
            } ?: showToast("URL no disponible")
        }

        btnVerNoticia.setOnClickListener {
            noticiaUrl?.let { url ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } ?: showToast("URL no disponible")
        }
    }

    private fun loadNoticiaDetailsByUrl(noticiaUrl: String) {
        Log.d("Noticias_Carga", "Cargando detalles para la noticia URL: $noticiaUrl")

        db.collection("noticias")
            .whereEqualTo("url", noticiaUrl)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    Log.d("Noticias_Carga", "Documento encontrado: ${document.id}")

                    val noticia = document.toObject(Noticia::class.java)
                    noticia?.let {
                        titleTextView.text = it.title ?: "Título no disponible"
                        dateTextView.text = "Fecha: ${formatDate(it.publishedAt)}"
                        infoTextView.text = it.content ?: "Contenido no disponible"
                        this.noticiaUrl = it.url

                        Glide.with(this)
                            .load(it.urlToImage)
                            .placeholder(R.drawable.no_image)
                            .error(R.drawable.no_image)
                            .into(imageView)

                        Log.d("Noticias_Carga", "Noticia cargada: ${it.title}")
                    } ?: run {
                        showToast("No se pudo cargar la noticia")
                    }
                } else {
                    Log.d("Noticias_Carga", "No se encontró la noticia con URL: $noticiaUrl")
                    showToast("Noticia no encontrada")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Noticias_Carga", "Error al cargar la noticia: ${e.message}")
                showToast("Error al cargar la noticia")
            }
    }

    private fun checkIfNoticiaGuardada() {
        noticiaUrl?.let { url ->
            userId?.let { uid ->
                db.collection("users")
                    .document(uid)
                    .collection("noticias_guardadas")
                    .document(url.hashCode().toString())
                    .get()
                    .addOnSuccessListener { document ->
                        isNoticiaGuardada = document.exists()
                        updateGuardarButtonState()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Noticias_Carga", "Error checking saved status: ${e.message}")
                    }
            }
        }
    }

    private fun updateGuardarButtonState() {
        if (isNoticiaGuardada) {
            btnGuardar.text = "Guardada"
            btnGuardar.setBackgroundColor(getColor(R.color.guarado_cyan))
        } else {
            btnGuardar.text = "Guardar"
            btnGuardar.setBackgroundColor(getColor(R.color.cyan_book))
        }
    }

    private fun toggleGuardarNoticia(noticiaUrl: String) {
        userId?.let { uid ->
            val documentId = noticiaUrl.hashCode().toString()
            val noticiaRef = db.collection("users")
                .document(uid)
                .collection("noticias_guardadas")
                .document(documentId)

            if (isNoticiaGuardada) {
                // Eliminar noticia guardada
                noticiaRef.delete()
                    .addOnSuccessListener {
                        isNoticiaGuardada = false
                        updateGuardarButtonState()
                        showToast("Noticia eliminada de guardados")
                    }
                    .addOnFailureListener { e ->
                        showToast("Error al eliminar la noticia")
                        Log.e("Noticias_Carga", "Error eliminando noticia: ${e.message}")
                    }
            } else {
                // Guardar noticia
                val noticiaData = hashMapOf(
                    "url" to noticiaUrl,
                    "title" to titleTextView.text.toString(),
                    "fecha" to dateTextView.text.toString(),
                    "content" to infoTextView.text.toString(),
                    "timestamp" to System.currentTimeMillis()
                )

                noticiaRef.set(noticiaData)
                    .addOnSuccessListener {
                        isNoticiaGuardada = true
                        updateGuardarButtonState()
                        showToast("Noticia guardada exitosamente")
                    }
                    .addOnFailureListener { e ->
                        showToast("Error al guardar la noticia")
                        Log.e("Noticias_Carga", "Error guardando noticia: ${e.message}")
                    }
            }
        } ?: showToast("Error: Usuario no identificado")
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