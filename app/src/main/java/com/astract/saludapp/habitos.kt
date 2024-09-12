package com.astract.saludapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class habitos : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitosAdapter
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var progressPercentageTextView: TextView
    private lateinit var habitsCompletedTextView: TextView

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

        // Configura el CircularProgressIndicator y TextViews
        progressIndicator = view.findViewById(R.id.circular_progress)
        progressPercentageTextView = view.findViewById(R.id.progress_percentage)
        habitsCompletedTextView = view.findViewById(R.id.habitos_completados)

        // Inicializa y configura el adaptador con MutableList
        val items: MutableList<Habito> = mutableListOf(
            Habito("Hábito 1", false),
            Habito("Hábito 2", true),
            Habito("Hábito 3", false)
        )
        adapter = HabitosAdapter(items) { updateProgress(it) }
        recyclerView.adapter = adapter

        // Inicializa el progreso
        updateProgress(items)

        // Llama a la función de animación para los views
        animateViews()
    }

    private fun updateProgress(items: List<Habito>) {
        val totalHabitos = items.size
        val completados = items.count { it.completado }
        val porcentaje = if (totalHabitos > 0) (completados * 100) / totalHabitos else 0

        // Animar el progreso del CircularProgressIndicator y el porcentaje
        animateProgress(progressIndicator.progress, porcentaje)
        animatePercentage(progressPercentageTextView.text.toString().replace("%", "").toInt(), porcentaje)

        // Actualiza el TextView con los hábitos completados
        habitsCompletedTextView.text = "$completados de $totalHabitos hábitos completados"
    }

    // Función para animar el CircularProgressIndicator
    private fun animateProgress(currentValue: Int, newValue: Int) {
        val progressAnimator = ValueAnimator.ofInt(currentValue, newValue)
        progressAnimator.duration = 1000 // Duración de la animación en milisegundos
        progressAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            progressIndicator.progress = animatedValue
        }
        progressAnimator.start()
    }

    // Función para animar el porcentaje del progreso
    private fun animatePercentage(currentValue: Int, newValue: Int) {
        val percentageAnimator = ValueAnimator.ofInt(currentValue, newValue)
        percentageAnimator.duration = 1000 // Duración de la animación
        percentageAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            progressPercentageTextView.text = "$animatedValue%"
        }
        percentageAnimator.start()
    }

    // Función para obtener la fecha en el formato deseado
    private fun obtenerFechaActual(): String {
        val locale = Locale("es", "ES")
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", locale)
        val fecha = Date()
        val fechaFormateada = formatoFecha.format(fecha)

        return fechaFormateada.replaceFirstChar { it.titlecase(locale) }
    }

    // Función para animar los views iniciales
    private fun animateViews() {
        val fadeInAnimator = ObjectAnimator.ofFloat(view?.findViewById(R.id.recycler_view), "alpha", 0f, 1f)
        fadeInAnimator.duration = 500 // Duración de la animación
        fadeInAnimator.start()
    }

    companion object {
        @JvmStatic
        fun newInstance() = habitos()
    }
}

// Clase de datos para representar un hábito
data class Habito(val nombre: String, var completado: Boolean)
