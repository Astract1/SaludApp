package com.astract.saludapp


import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class perfil : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var verMasButton: ImageView
    private var isExpanded = false
    private lateinit var imcValueTextView: TextView
    private lateinit var imcEstadoTextView: TextView
    private lateinit var imcColorIndicator: View
    private lateinit var pieChart: PieChart
    private var retosCompletados = 0
    private var retosTotales = 0
    private lateinit var noticiasRecyclerView: RecyclerView
    private lateinit var articulosRecyclerView: RecyclerView
    private lateinit var noticiasAdapter: GuardarNoticiaAdapter
    private lateinit var articulosAdapter: GuardarArticuloAdapter
    private lateinit var noNoticiasText: TextView
    private lateinit var noArticulosText: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImage: ImageView
    private var listenerRegistration: ListenerRegistration? = null


    data class RetoSimple(
        val titulo: String,
        val descripcion: String,
        var completado: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_perfil)

        // Inicializar Firebase primero
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Luego el resto de inicializaciones
        inicializarRecyclersGuardados()
        inicializarVistas()
        configurarVolver()
        setupPieChart()

        val userId = intent.getStringExtra("userId")
        if (userId != null) {
            Log.d("Perfil", "UID del usuario: $userId")
            obtenerUsuarioPorUID(userId)
            setupMenuOptions()
        } else {
            Log.e("Perfil", "No se encontró UID del usuario")
        }

        profileImage = findViewById(R.id.profile_image)
        loadProfileImage()

        actualizarIMC()
        cargarDatos()
    }

    private fun inicializarVistas() {
        recyclerView = findViewById(R.id.retos_recycler_view)
        verMasButton = findViewById(R.id.ver_mas_retos)
        imcValueTextView = findViewById(R.id.imc_value)
        imcEstadoTextView = findViewById(R.id.imc_estado)
        imcColorIndicator = findViewById(R.id.imc_color_indicator)
        pieChart = findViewById(R.id.pieChart)

        // Configurar animaciones iniciales
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Animar el contenedor de IMC
        findViewById<View>(R.id.imc_card).startAnimation(fadeIn)

        // Animar el PieChart
        pieChart.startAnimation(slideUp)

        // Configurar animación del RecyclerView
        val controller = LayoutAnimationController(
            AnimationUtils.loadAnimation(this, R.anim.fade_in),
            0.15f
        )
        recyclerView.layoutAnimation = controller
    }

    private fun configurarVolver() {
        val volver = findViewById<ImageView>(R.id.back_arrow)
        volver.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun cargarDatos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Referencia a la colección de retos inscritos del usuario
        val retosRef = db.collection("users").document(userId)
            .collection("retos_inscritos")

        retosRef.get().addOnSuccessListener { documents ->
            val listaRetos = documents.map { document ->
                RetoSimple(
                    titulo = document.getString("titulo") ?: "",
                    descripcion = "Inscrito el: ${document.getString("fechaInscripcion") ?: ""}",
                    completado = document.getBoolean("completado") ?: false
                )
            }

            retosTotales = listaRetos.size
            retosCompletados = listaRetos.count { it.completado }

            val totalRetosTextView = findViewById<TextView>(R.id.total_retos)
            totalRetosTextView.text = "Total de retos: $retosTotales"

            if (listaRetos.isNotEmpty()) {
                val retoActualTitulo = findViewById<TextView>(R.id.reto_actual_titulo)
                val retoActualDescripcion = findViewById<TextView>(R.id.reto_actual_descripcion)
                retoActualTitulo.text = listaRetos[0].titulo
                retoActualDescripcion.text = listaRetos[0].descripcion
            }

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = PerfilRetosAdapter(
                listaRetos.toMutableList(),
                { titulo, completado -> actualizarEstadoReto(titulo, completado) },
                { titulo -> eliminarRetoDeFirebase(titulo) }
            )

            configurarBotonVerMas()
        }.addOnFailureListener { exception ->
            Log.e("Perfil", "Error getting documents: ", exception)
            Toast.makeText(this, "Error al cargar los retos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarBotonVerMas() {
        verMasButton.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                recyclerView.visibility = View.VISIBLE
                recyclerView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start()
            } else {
                recyclerView.animate()
                    .alpha(0f)
                    .translationY(50f)
                    .setDuration(300)
                    .withEndAction {
                        recyclerView.visibility = View.GONE
                    }
                    .start()
            }
            verMasButton.animate()
                .rotation(if (isExpanded) 180f else 0f)
                .setDuration(300)
                .start()
        }
    }

    private fun actualizarDatosPieChart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            pieChart.setNoDataText("No hay datos disponibles")
            pieChart.invalidate()
            return
        }

        // Cancelar el listener anterior si existe
        listenerRegistration?.remove()

        // Crear nuevo listener
        listenerRegistration = db.collection("users")
            .document(userId)
            .collection("retos_inscritos")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Perfil", "Error al escuchar cambios", e)
                    pieChart.setNoDataText("Error al cargar datos")
                    pieChart.invalidate()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                val retosTotales = snapshot.size()
                var retosCompletados = 0

                // Contar retos completados
                for (document in snapshot.documents) {
                    val completado = document.getBoolean("completado") ?: false
                    if (completado) {
                        retosCompletados++
                    }
                }

                if (retosTotales == 0) {
                    pieChart.setNoDataText("No hay retos inscritos")
                    pieChart.invalidate()
                    return@addSnapshotListener
                }

                val entries = ArrayList<PieEntry>().apply {
                    val porcentajeCompletados = if (retosTotales > 0) {
                        (retosCompletados.toFloat() / retosTotales.toFloat()) * 100f
                    } else 0f

                    val porcentajePendientes = if (retosTotales > 0) {
                        ((retosTotales - retosCompletados).toFloat() / retosTotales.toFloat()) * 100f
                    } else 0f

                    if (porcentajeCompletados > 0) {
                        add(PieEntry(porcentajeCompletados, "Completados"))
                    }
                    if (porcentajePendientes > 0) {
                        add(PieEntry(porcentajePendientes, "Pendientes"))
                    }
                }

                if (entries.isEmpty()) {
                    pieChart.setNoDataText("No hay retos inscritos")
                    pieChart.invalidate()
                    return@addSnapshotListener
                }

                val colors = ArrayList<Int>().apply {
                    add(ContextCompat.getColor(this@perfil, R.color.completado_background))
                    add(ContextCompat.getColor(this@perfil, R.color.pendiente_background))
                }

                val dataSet = PieDataSet(entries, "").apply {
                    setColors(colors)
                    setDrawValues(true)
                    valueTextSize = 12f
                    valueTextColor = Color.BLACK
                }

                pieChart.apply {
                    data = PieData(dataSet).apply {
                        setValueFormatter(PercentFormatter())
                        setValueTextSize(12f)
                        setValueTextColor(Color.BLACK)
                    }
                    invalidate()
                }

                // Actualizar el texto del total de retos
                findViewById<TextView>(R.id.total_retos).text = "Total de retos: $retosTotales"
            }
    }

    private fun setupPieChart() {
        pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 61f
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
            }
        }
        actualizarDatosPieChart()
    }


    private fun eliminarRetoDeFirebase(titulo: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("retos_inscritos")
            .whereEqualTo("titulo", titulo)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Reto eliminado", Toast.LENGTH_SHORT).show()
                            cargarDatos()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Perfil", "Error deleting reto: ", e)
                            Toast.makeText(this, "Error al eliminar el reto", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }


    private fun actualizarEstadoReto(titulo: String, completado: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("retos_inscritos")
            .whereEqualTo("titulo", titulo)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("completado", completado)
                        .addOnSuccessListener {
                            // Actualizar UI si es necesario
                            cargarDatos()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Perfil", "Error updating reto: ", e)
                            Toast.makeText(this, "Error al actualizar el reto", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun actualizarIMC() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            // Si no hay usuario, mostrar valores por defecto
            mostrarIMCPorDefecto()
            return
        }

        db.collection("users")
            .document(userId)
            .collection("historial_imc")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultimoIMC = documents.documents[0].getDouble("imc")
                    if (ultimoIMC != null) {
                        // Animación para el valor del IMC
                        val animator = ValueAnimator.ofFloat(0f, ultimoIMC.toFloat())
                        animator.duration = 1000
                        animator.addUpdateListener { animation ->
                            imcValueTextView.text = String.format("%.1f", animation.animatedValue as Float)
                        }
                        animator.start()

                        val (estado, colorTo) = when {
                            ultimoIMC < 18.5 -> Pair("Bajo peso", getColor(R.color.yellow))
                            ultimoIMC < 25 -> Pair("Normal", getColor(R.color.green))
                            ultimoIMC < 30 -> Pair("Sobrepeso", getColor(R.color.yellow))
                            else -> Pair("Obesidad", getColor(R.color.red))
                        }

                        imcEstadoTextView.text = estado

                        // Animar el cambio de color
                        val colorFrom = (imcColorIndicator.background as? ColorDrawable)?.color
                            ?: Color.TRANSPARENT
                        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                            duration = 500
                            addUpdateListener { animator ->
                                imcColorIndicator.setBackgroundColor(animator.animatedValue as Int)
                            }
                            start()
                        }
                    } else {
                        mostrarIMCPorDefecto()
                    }
                } else {
                    mostrarIMCPorDefecto()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Perfil", "Error al obtener IMC", e)
                mostrarIMCPorDefecto()
            }
    }

    private fun mostrarIMCPorDefecto() {
        imcValueTextView.text = "---"
        imcEstadoTextView.text = "Sin datos"
        imcColorIndicator.setBackgroundColor(getColor(R.color.default_cyan))
    }




    private fun inicializarRecyclersGuardados() {
        // Inicializar RecyclerViews y vistas
        noticiasRecyclerView = findViewById(R.id.noticias_recycler_view)
        articulosRecyclerView = findViewById(R.id.articulos_recycler_view)
        noNoticiasText = findViewById(R.id.no_noticias_text)
        noArticulosText = findViewById(R.id.no_articulos_text)

        // Configurar layouts
        noticiasRecyclerView.layoutManager = LinearLayoutManager(this)
        articulosRecyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar adaptadores
        noticiasAdapter = GuardarNoticiaAdapter(
            noticias = emptyList(),
            onItemClick = { noticia -> abrirDetalleNoticia(noticia) },
            onDeleteClick = { noticia -> eliminarNoticia(noticia) }
        )

        articulosAdapter = GuardarArticuloAdapter(
            articulos = emptyList(),
            onItemClick = { articulo -> abrirDetalleArticulo(articulo) },
            onDeleteClick = { articulo -> eliminarArticulo(articulo) }
        )

        // Asignar adaptadores
        noticiasRecyclerView.adapter = noticiasAdapter
        articulosRecyclerView.adapter = articulosAdapter

        // Cargar datos iniciales
        cargarNoticiasYArticulos()
    }

    private fun cargarNoticiasYArticulos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Para noticias
        db.collection("users").document(userId).collection("noticias_guardadas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Perfil", "Error al cargar noticias: ${e.message}")
                    Toast.makeText(this, "Error al cargar noticias", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val noticiasGuardadas = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val noticia = doc.toObject(Noticia::class.java)
                        Log.d("Perfil", "Noticia cargada: $noticia")
                        noticia
                    } catch (e: Exception) {
                        Log.e("Perfil", "Error al convertir noticia: ${e.message}")
                        null
                    }
                } ?: emptyList()

                noticiasAdapter.actualizarNoticias(noticiasGuardadas)
                noNoticiasText.visibility = if (noticiasGuardadas.isEmpty()) View.VISIBLE else View.GONE
            }


        db.collection("users").document(userId).collection("articulos_guardados")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Perfil", "Error al cargar artículos: ${e.message}")
                    Toast.makeText(this, "Error al cargar artículos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val articulosGuardados = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val articulo = doc.toObject(Articulo::class.java)
                        Log.d("Perfil", "Artículo cargado: $articulo")
                        articulo
                    } catch (e: Exception) {
                        Log.e("Perfil", "Error al convertir artículo: ${e.message}")
                        null
                    }
                } ?: emptyList()

                articulosAdapter.actualizarArticulos(articulosGuardados)
                noArticulosText.visibility = if (articulosGuardados.isEmpty()) View.VISIBLE else View.GONE
            }
    }


    private fun eliminarNoticia(noticia: Noticia) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val documentId = generateDocumentIdFromUrl(noticia.url)

        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que deseas eliminar esta noticia?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("users")
                    .document(userId)
                    .collection("noticias_guardadas")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Noticia eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar la noticia", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun eliminarArticulo(articulo: Articulo) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val documentId = generateDocumentIdFromUrl(articulo.url)

        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que deseas eliminar este artículo?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("users")
                    .document(userId)
                    .collection("articulos_guardados")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Artículo eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar el artículo", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun generateDocumentIdFromUrl(url: String): String {
        return url.replace("https://", "")
            .replace("http://", "")
            .replace("/", "_")
            .replace(".", "_")
    }


    private fun abrirDetalleNoticia(noticia: Noticia) {
        val intent = Intent(this, Noticias_Carga::class.java).apply {
            putExtra("NOTICIA_URL", noticia.url)
        }
        Log.d("Perfil", "Abriendo noticia: ${noticia.url}")
        startActivity(intent)
    }

    private fun abrirDetalleArticulo(articulo: Articulo) {
        val intent = Intent(this, ArticuloCarga::class.java).apply {
            putExtra("ARTICULO_URL", articulo.url)
        }
        Log.d("Perfil", "Abriendo artículo: ${articulo.url}")
        startActivity(intent)
    }


    private fun obtenerUsuarioPorUID(uuid: String) {
        val userDocRef = db.collection("users").document(uuid)

        userDocRef.get().addOnSuccessListener { document: DocumentSnapshot? ->
            if (document != null) {
                val nombre = document.getString("name")
                val apellido = document.getString("lastName")

                val usernameTextView = findViewById<TextView>(R.id.username_text)
                usernameTextView.text = "$nombre $apellido"
            } else {
                Log.e("Perfil", "No se encontró el documento del usuario")
            }
        }.addOnFailureListener { e: Exception ->
            Log.e("Perfil", "Error al obtener el usuario", e)
        }
    }


    private fun setupMenuOptions() {
        findViewById<ImageView>(R.id.menu_options).setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.profile_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.logout -> {
                        showLogoutConfirmation()
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    private fun openImagePicker() {
        if (checkAndRequestPermissions()) {
            showImageSourceDialog()
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        // Permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.CAMERA)
        }

        // Permisos de almacenamiento según la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
            return false
        }

        return true
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto", "Elegir de galería")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                startActivityForResult(intent, CAMERA_REQUEST)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun logout() {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        FirebaseAuth.getInstance().signOut()
        redirectToLogin()
    }


    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                ) {
                    showImageSourceDialog()
                } else {
                    Toast.makeText(
                        this,
                        "Se requieren permisos para esta funcionalidad",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }



    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }
    private fun updateProfileImage(imageUrl: String) {
        val userId = intent.getStringExtra("userId") ?: run {
            Log.e("ProfileActivity", "userId is null")
            return
        }

        db.collection("users").document(userId)
            .update("profileImage", imageUrl)
            .addOnSuccessListener {
                findViewById<ImageView>(R.id.profile_image)?.let { imageView ->
                    loadImageWithCoil(imageView, imageUrl)
                }
                Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error updating document", e)
                Toast.makeText(this, "Error al actualizar la foto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImageWithCoil(imageView: ImageView, url: String?) {
        try {
            // Use a safe default drawable
            val defaultDrawable = ResourcesCompat.getDrawable(
                resources,
                android.R.drawable.ic_menu_gallery,
                null
            )

            if (url.isNullOrEmpty()) {
                imageView.setImageDrawable(defaultDrawable)
                return
            }

            imageView.load(url) {
                crossfade(true)
                placeholder(defaultDrawable)
                error(defaultDrawable)
                transformations(CircleCropTransformation())
                listener(
                    onStart = {
                        Log.d("ProfileActivity", "Started loading image from $url")
                    },
                    onSuccess = { _, _ ->
                        Log.d("ProfileActivity", "Successfully loaded image from $url")
                    },
                    onError = { _, error ->
                        Log.e("ProfileActivity", "Error loading image from $url", error.throwable)
                        imageView.setImageDrawable(defaultDrawable)
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error in loadImageWithCoil", e)
            // Fallback to a very basic drawable if everything else fails
            try {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            } catch (e2: Exception) {
                Log.e("ProfileActivity", "Failed to set fallback image", e2)
            }
        }
    }


    private fun loadProfileImage() {
        val userId = intent.getStringExtra("userId") ?: run {
            Log.e("ProfileActivity", "userId is null")
            return
        }

        val imageView = findViewById<ImageView>(R.id.profile_image) ?: run {
            Log.e("ProfileActivity", "profile_image view not found")
            return
        }

        try {

            ResourcesCompat.getDrawable(resources, android.R.drawable.ic_menu_gallery, null)?.let {
                imageView.setImageDrawable(it)
            }
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error setting initial image", e)
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val imageUrl = document.getString("profileImage")
                loadImageWithCoil(imageView, imageUrl)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error getting document", e)
                try {
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery)
                } catch (e2: Exception) {
                    Log.e("ProfileActivity", "Failed to set fallback image", e2)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }




    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val CAMERA_REQUEST = 1
        private const val GALLERY_REQUEST = 2
    }

    override fun onResume() {
        super.onResume()
        cargarNoticiasYArticulos()
    }
}