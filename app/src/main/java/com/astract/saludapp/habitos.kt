package com.astract.saludapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class habitos : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Puedes manejar argumentos aquí si es necesario.
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout para este fragmento
        return inflater.inflate(R.layout.fragment_habitos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el TextView para la fecha
        val fechaTextView: TextView = view.findViewById(R.id.fecha)
        val fechaActual = obtenerFechaActual()
        fechaTextView.text = fechaActual

        // Configura el RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inicializa y configura el adaptador con MutableList
        val items: MutableList<String> = mutableListOf("Hábito 1", "Hábito 2", "Hábito 3") // Reemplaza con tus datos reales
        adapter = HabitosAdapter(items)
        recyclerView.adapter = adapter
    }

    // Función para obtener la fecha en el formato deseado
    private fun obtenerFechaActual(): String {
        val locale = Locale("es", "ES")
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", locale)
        val fecha = Date()
        return formatoFecha.format(fecha)
    }

    companion object {
        @JvmStatic
        fun newInstance() = habitos()
    }
}
