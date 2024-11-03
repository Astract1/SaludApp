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
import android.content.Context
import android.widget.TextView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class Historialimc : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historialIMCAdapter: HistorialIMCAdapter
    private lateinit var lineChart: LineChart
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    // Configurar el formateador de fecha con la zona horaria local
    private val dateFormatter = SimpleDateFormat("EEEE d 'de' MMMM, yyyy 'a las' HH:mm", Locale("es", "ES")).apply {
        timeZone = Calendar.getInstance().timeZone
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_historialimc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = sharedViewModel.getUserId()

        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lineChart = view.findViewById(R.id.lineChart)

        cargarDatosHistorial()

        val volverTextView: View = view.findViewById(R.id.ver_historial_imc)
        val iconArrow: View = view.findViewById(R.id.icon_arrow)

        volverTextView.setOnClickListener { iniciarAnimacionDeScroll(view) }
        iconArrow.setOnClickListener { iniciarAnimacionDeScroll(view) }
    }

    private fun obtenerFechaActualFormateada(): String {
        val calendar = Calendar.getInstance()
        return dateFormatter.format(calendar.time)
    }

    private fun formatearFecha(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
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
                        try {
                            val imc = document.getDouble("imc") ?: 0.0
                            val peso = document.getDouble("peso") ?: 0.0
                            val altura = document.getDouble("altura") ?: 0.0
                            val timestamp = document.getTimestamp("timestamp")
                            val fechaStr = document.getString("fecha") ?: ""

                            historialList.add(
                                HistorialIMCData(
                                    id = document.id,
                                    peso = peso,
                                    altura = altura,
                                    resultadoIMC = imc,
                                    timestamp = timestamp ?: Timestamp.now(),
                                    fecha = fechaStr
                                )
                            )
                        } catch (e: Exception) {
                            println("Error al procesar documento: ${e.message}")
                        }
                    }

                    historialIMCAdapter = HistorialIMCAdapter(
                        historialList,
                        requireContext(),
                        uid
                    ) { onItemDeleted() }
                    recyclerView.adapter = historialIMCAdapter

                    configurarGraficoIMC(historialList)
                }
                .addOnFailureListener { exception ->
                    println("Error al cargar el historial: ${exception.message}")
                }
        }
    }

    private fun configurarGraficoIMC(historialIMCList: List<HistorialIMCData>) {
        val entries = ArrayList<Entry>()
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

    // Función actualizada para guardar nuevo registro con timestamp
    fun guardarNuevoRegistroIMC(peso: Double, altura: Double, imc: Double) {
        // Obtener la fecha y hora actual en la zona horaria local
        val calendar = Calendar.getInstance()
        val timestamp = Timestamp(Date(calendar.timeInMillis))
        val fechaActual = dateFormatter.format(calendar.time)

        val nuevoRegistro = hashMapOf(
            "peso" to peso,
            "altura" to altura,
            "imc" to imc,
            "fecha" to fechaActual,
            "timestamp" to timestamp
        )

        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("historial_imc")
                .add(nuevoRegistro)
                .addOnSuccessListener {
                    cargarDatosHistorial()
                }
                .addOnFailureListener { e ->
                    println("Error al guardar el registro: ${e.message}")
                }
        }
    }

    fun actualizarRegistroIMC(id: String, peso: Double, altura: Double, imc: Double) {
        val calendar = Calendar.getInstance()
        val timestamp = Timestamp(Date(calendar.timeInMillis))
        val fechaActual = dateFormatter.format(calendar.time)

        val actualizacionRegistro = hashMapOf(
            "peso" to peso,
            "altura" to altura,
            "imc" to imc,
            "fecha" to fechaActual,
            "timestamp" to timestamp
        )

        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("historial_imc")
                .document(id)
                .update(actualizacionRegistro as Map<String, Any>)
                .addOnSuccessListener {
                    cargarDatosHistorial()
                }
                .addOnFailureListener { e ->
                    println("Error al actualizar el registro: ${e.message}")
                }
        }
    }
}
