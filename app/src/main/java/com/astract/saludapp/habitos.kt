package com.astract.saludapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // Aquí puedes inicializar el adaptador con una lista vacía si es necesario
        adapter = HabitosAdapter(mutableListOf()) { updateProgress(it) }
        recyclerView.adapter = adapter

        // Inicializa el progreso (esto puede estar vacío o tener un valor predeterminado)
        updateProgress(adapter.getItems())

        // Encuentra el botón y configura el OnClickListener
        val addButton: Button = view.findViewById(R.id.btn_add)
        addButton.setOnClickListener {
            mostrarDialogoPersonalizado()
        }
    }


    private fun mostrarDialogoPersonalizado() {
        // Infla la vista del diálogo personalizado
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom, null)

        // Encuentra los elementos en la vista del diálogo
        val nombreHábitoEditText: TextInputEditText = dialogView.findViewById(R.id.text_input_edit_text_nombre_habito)
        val tiempoSpinner: Spinner = dialogView.findViewById(R.id.spinner_tiempo)
        val frecuenciaSpinner: Spinner = dialogView.findViewById(R.id.spinner_frecuencia)
        val guardarButton: MaterialButton = dialogView.findViewById(R.id.boton_guardar_habito)
        val closeButton: ImageView = dialogView.findViewById(R.id.icon_close)

        // Configura los datos para el Spinner de tiempo
        val tiempoOptions = arrayOf("1 Mes (30 Dias)", "2 Meses (60 Dias)", "3 Meses (90 Dias)")
        val tiempoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiempoOptions)
        tiempoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tiempoSpinner.adapter = tiempoAdapter
        tiempoSpinner.setSelection(0)

        // Configura los datos para el Spinner de frecuencia
        val frecuenciaOptions = arrayOf("Diario", "Semanal", "Mensual")
        val frecuenciaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frecuenciaOptions)
        frecuenciaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frecuenciaSpinner.adapter = frecuenciaAdapter
        frecuenciaSpinner.setSelection(1) // Establece "Media" como valor predeterminado

        // Crea el diálogo personalizado
        val customDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Configura el botón de guardar
        guardarButton.setOnClickListener {
            val nombreHábito = nombreHábitoEditText.text.toString().trim()
            val tiempo = tiempoSpinner.selectedItem.toString()
            val frecuencia = frecuenciaSpinner.selectedItem.toString()

            if (nombreHábito.isNotEmpty()) {
                // Añade el nuevo hábito a la lista
                val nuevoHábito = Habito(nombreHábito, false)
                adapter.addHabit(nuevoHábito)

                // Actualiza el progreso y cierra el diálogo
                updateProgress(adapter.getItems())
                customDialog.dismiss()
            } else {
                nombreHábitoEditText.error = "El nombre del hábito no puede estar vacío"
            }
        }

        // Configura el botón de cerrar (si existe)
        closeButton.setOnClickListener {
            customDialog.dismiss()
        }

        // Muestra el diálogo
        customDialog.show()
    }





    private fun updateProgress(items: List<Habito>) {
        val totalHabitos = items.size
        val completados = items.count { it.completado }
        val porcentaje = if (totalHabitos > 0) (completados * 100) / totalHabitos else 0

        // Animar el progreso del CircularProgressIndicator y el porcentaje
        animateProgress(progressIndicator.progress, porcentaje)
        animatePercentage(progressPercentageTextView.text.toString().replace("%", "").toInt(), porcentaje)

        // Actualiza el TextView con los hábitos completados
        habitsCompletedTextView.text = "$completados de $totalHabitos hábitos habilitados"
    }

    private fun animateProgress(currentValue: Int, newValue: Int) {
        val progressAnimator = ValueAnimator.ofInt(currentValue, newValue)
        progressAnimator.duration = 1000 // Duración de la animación en milisegundos
        progressAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            progressIndicator.progress = animatedValue
        }
        progressAnimator.start()
    }

    private fun animatePercentage(currentValue: Int, newValue: Int) {
        val percentageAnimator = ValueAnimator.ofInt(currentValue, newValue)
        percentageAnimator.duration = 1000 // Duración de la animación
        percentageAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            progressPercentageTextView.text = "$animatedValue%"
        }
        percentageAnimator.start()
    }

    private fun obtenerFechaActual(): String {
        val locale = Locale("es", "ES")
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", locale)
        val fecha = Date()
        val fechaFormateada = formatoFecha.format(fecha)

        return fechaFormateada.replaceFirstChar { it.titlecase(locale) }
    }

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

data class Habito(val nombre: String, var completado: Boolean)
