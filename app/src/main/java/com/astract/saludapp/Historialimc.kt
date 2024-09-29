package com.astract.saludapp

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astract.saludapp.database.MyDatabaseHelper
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Historialimc : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historialIMCAdapter: HistorialIMCAdapter
    private lateinit var lineChart: LineChart
    private val db by lazy { MyDatabaseHelper(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_historialimc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Configurar LineChart
        lineChart = view.findViewById(R.id.lineChart)

        // Cargar los datos de la base de datos
        cargarDatosHistorial()

        // Configurar listeners para volver a la calculadora IMC
        val volverTextView: View = view.findViewById(R.id.ver_historial_imc)
        val iconArrow: View = view.findViewById(R.id.icon_arrow)

        volverTextView.setOnClickListener { iniciarAnimacionDeScroll(view) }
        iconArrow.setOnClickListener { iniciarAnimacionDeScroll(view) }
    }

    private fun cargarDatosHistorial() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Cargar datos en un hilo de fondo
            val historialIMCList = withContext(Dispatchers.IO) {
                db.getAllHistorialIMC()
            }
            // Inicializar y asignar el adaptador al RecyclerView
            historialIMCAdapter = HistorialIMCAdapter(historialIMCList.toMutableList(), requireContext(), ::onItemDeleted)
            recyclerView.adapter = historialIMCAdapter

            // Configurar el gráfico de IMC
            configurarGraficoIMC(historialIMCList)


        }
    }

    private fun onItemDeleted() {
        cargarDatosHistorial()
    }

    private fun configurarGraficoIMC(historialIMCList: List<HistorialIMCData>) {
        // Crear entradas para el gráfico a partir del historial IMC
        val entries = ArrayList<Entry>()
        for (i in historialIMCList.indices) {
            // Convertir resultadoIMC a Float
            entries.add(Entry(i.toFloat(), historialIMCList[i].resultadoIMC.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "Historial IMC").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawFilled(true) // Habilitar relleno
            fillColor = Color.BLUE // Color de relleno
            fillAlpha = 100 // Transparencia del relleno
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Animar el gráfico
        lineChart.animateXY(1000, 1000) // 1000 ms para la animación en ambos ejes

        lineChart.invalidate() // Actualizar el gráfico
    }
    private fun iniciarAnimacionDeScroll(rootView: View) {

        val scrollAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)

        // Aplicar la animación a la vista raíz del fragmento
        rootView.startAnimation(scrollAnimation)

        // Listener para saber cuándo termina la animación
        scrollAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Navegar de regreso a la calculadora IMC
                val action = imcDirections.actionGlobalImc()
                findNavController().navigate(action)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}
