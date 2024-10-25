package com.astract.saludapp

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astract.saludapp.database.MyDatabaseHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.formatter.PercentFormatter

class perfil : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var verMasButton: ImageView
    private var isExpanded = false
    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var imcValueTextView: TextView
    private lateinit var imcEstadoTextView: TextView
    private lateinit var imcColorIndicator: View
    private lateinit var pieChart: PieChart
    private var retosCompletados = 0
    private var retosTotales = 0

    data class RetoSimple(
        val titulo: String,
        val descripcion: String,
        var completado: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        inicializarVistas()
        configurarVolver()
        cargarDatos()
        setupPieChart()
    }

    private fun inicializarVistas() {
        dbHelper = MyDatabaseHelper(this)
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
        actualizarIMC()

        val retosInscritos = dbHelper.obtenerRetosInscritos()
        val listaRetos = retosInscritos.map { titulo ->
            RetoSimple(
                titulo = titulo,
                descripcion = "Inscrito el: ${obtenerFechaInscripcion(titulo)}",
                completado = dbHelper.isRetoCompletado(titulo)
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
            { titulo -> eliminarRetoDeBaseDeDatos(titulo) }
        )

        configurarBotonVerMas()
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

    private fun actualizarDatosPieChart() {
        if (retosTotales == 0) {
            // Si no hay retos, mostrar un gráfico vacío o un mensaje
            pieChart.setNoDataText("No hay retos inscritos")
            pieChart.invalidate()
            return
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
            return
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

    private fun actualizarEstadoReto(tituloReto: String, completado: Boolean) {
        dbHelper.actualizarEstadoReto(tituloReto, completado)
        if (completado) retosCompletados++ else retosCompletados--
        actualizarDatosPieChart()
    }

    private fun actualizarIMC() {
        val imc = dbHelper.obtenerUltimoIMC()
        if (imc != null) {
            // Animación para el valor del IMC
            val animator = ValueAnimator.ofFloat(0f, imc.toFloat())
            animator.duration = 1000
            animator.addUpdateListener { animation ->
                imcValueTextView.text = String.format("%.1f", animation.animatedValue as Float)
            }
            animator.start()

            val (estado, colorTo) = when {
                imc < 18.5 -> Pair("Bajo peso", getColor(R.color.yellow))
                imc < 25 -> Pair("Normal", getColor(R.color.green))
                imc < 30 -> Pair("Sobrepeso", getColor(R.color.yellow))
                else -> Pair("Obesidad", getColor(R.color.red))
            }

            imcEstadoTextView.text = estado

            // Animar el cambio de color
            val colorFrom = (imcColorIndicator.background as? ColorDrawable)?.color ?: Color.TRANSPARENT
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                duration = 500
                addUpdateListener { animator ->
                    imcColorIndicator.setBackgroundColor(animator.animatedValue as Int)
                }
                start()
            }
        } else {
            imcValueTextView.text = "---"
            imcEstadoTextView.text = "Sin datos"
            imcColorIndicator.setBackgroundColor(getColor(R.color.default_cyan))
        }
    }

    private fun obtenerFechaInscripcion(tituloReto: String): String {
        val db = dbHelper.readableDatabase
        var fecha = ""

        val cursor = db.query(
            MyDatabaseHelper.TABLE_NAME_INSCRIPCIONES,
            arrayOf(MyDatabaseHelper.COLUMN_FECHA_INSCRIPCION),
            "${MyDatabaseHelper.COLUMN_TITULO_RETO} = ?",
            arrayOf(tituloReto),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                fecha = it.getString(it.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_FECHA_INSCRIPCION))
            }
        }

        return fecha
    }

    private fun eliminarRetoDeBaseDeDatos(tituloReto: String) {
        val eraCompletado = dbHelper.isRetoCompletado(tituloReto)

        if (dbHelper.eliminarInscripcionReto(tituloReto)) {
            if (retosTotales > 0) retosTotales--
            if (eraCompletado && retosCompletados > 0) retosCompletados--

            actualizarDatosPieChart()
            Toast.makeText(this, "Reto eliminado correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al eliminar el reto", Toast.LENGTH_SHORT).show()
        }
    }

}
