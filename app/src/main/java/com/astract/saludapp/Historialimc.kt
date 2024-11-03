package com.astract.saludapp

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.navigation.fragment.findNavController

class Historialimc : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historialIMCAdapter: HistorialIMCAdapter
    private lateinit var lineChart: LineChart
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_historialimc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = sharedViewModel.getUserId()

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Configurar LineChart
        lineChart = view.findViewById(R.id.lineChart)

        // Cargar los datos desde Firebase
        cargarDatosHistorial()

        // Configurar listeners para volver a la calculadora IMC
        val volverTextView: View = view.findViewById(R.id.ver_historial_imc)
        val iconArrow: View = view.findViewById(R.id.icon_arrow)

        volverTextView.setOnClickListener { iniciarAnimacionDeScroll(view) }
        iconArrow.setOnClickListener { iniciarAnimacionDeScroll(view) }
    }

    private fun cargarDatosHistorial() {
        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("historial_imc")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val historialList = mutableListOf<HistorialIMCData>()

                    for (document in documents) {
                        val imc = document.getDouble("imc") ?: 0.0
                        val fecha = document.getString("fecha") ?: ""
                        val peso = document.getDouble("peso") ?: 0.0
                        val altura = document.getDouble("altura") ?: 0.0

                        historialList.add(
                            HistorialIMCData(
                                id = document.id,
                                fecha = fecha,
                                peso = peso,
                                altura = altura,
                                resultadoIMC = imc
                            )
                        )
                    }

                    // Configurar el adaptador con los nuevos datos, pasando el userId
                    historialIMCAdapter = HistorialIMCAdapter(
                        historialList,
                        requireContext(),
                        uid,  // Pasamos el userId al adaptador
                        { onItemDeleted() }
                    )
                    recyclerView.adapter = historialIMCAdapter

                    // Configurar el gráfico
                    configurarGraficoIMC(historialList)
                }
                .addOnFailureListener { exception ->
                    // Manejar el error
                    println("Error al cargar el historial: ${exception.message}")
                }
        }
    }

    private fun configurarGraficoIMC(historialIMCList: List<HistorialIMCData>) {
        val entries = ArrayList<Entry>()
        // Invertir la lista para que el gráfico muestre la progresión cronológica
        val listaOrdenada = historialIMCList.reversed()

        for (i in listaOrdenada.indices) {
            entries.add(Entry(i.toFloat(), listaOrdenada[i].resultadoIMC.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "Historial IMC").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = Color.BLUE
            fillAlpha = 100
        }

        val lineData = LineData(lineDataSet)
        lineChart.apply {
            data = lineData
            description.isEnabled = false
            animateXY(1000, 1000)
            invalidate()
        }
    }

    private fun onItemDeleted() {
        cargarDatosHistorial()
    }

    private fun iniciarAnimacionDeScroll(rootView: View) {
        val scrollAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
        rootView.startAnimation(scrollAnimation)

        scrollAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                val action = HistorialimcDirections.actionGlobalImc()
                findNavController().navigate(action)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}