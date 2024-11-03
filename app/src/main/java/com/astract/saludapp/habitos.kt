package com.astract.saludapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class habitos : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitosAdapter
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var progressPercentageTextView: TextView
    private lateinit var habitsCompletedTextView: TextView
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habitos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupViews(view: View) {
        // Configurar fecha
        val fechaTextView: TextView = view.findViewById(R.id.fecha)
        fechaTextView.text = obtenerFechaActual()

        // Configurar vistas de progreso
        progressIndicator = view.findViewById(R.id.circular_progress)
        progressPercentageTextView = view.findViewById(R.id.progress_percentage)
        habitsCompletedTextView = view.findViewById(R.id.habitos_completados)

        // Configurar botón de agregar
        val addButton: Button = view.findViewById(R.id.btn_add)
        addButton.setOnClickListener {
            mostrarDialogoPersonalizado()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = requireView().findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = HabitosAdapter(mutableListOf()) { habits ->
            updateProgress(habits)
        }
        recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        sharedViewModel.userId.observe(viewLifecycleOwner) { userId ->
            if (userId != null) {
                adapter.setUserId(userId)
                obtenerHabitosUsuario(userId)
            } else {
                Log.e("habitos", "No hay UID disponible en el ViewModel")
            }
        }
    }

    private fun mostrarDialogoPersonalizado() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_custom, null)

        val nombreHábitoEditText: TextInputEditText =
            dialogView.findViewById(R.id.text_input_edit_text_nombre_habito)
        val guardarButton: MaterialButton =
            dialogView.findViewById(R.id.boton_guardar_habito)

        val customDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        guardarButton.setOnClickListener {
            val nombreHábito = nombreHábitoEditText.text.toString().trim()
            if (nombreHábito.isNotEmpty()) {
                val nuevoHábito = Habito(nombreHábito, false)
                adapter.addHabit(nuevoHábito)
                guardarHábitoEnFirestore(nuevoHábito)
                updateProgress(adapter.getItems())
                updateEmptyViewVisibility()
                customDialog.dismiss()
            } else {
                nombreHábitoEditText.error = "El nombre del hábito no puede estar vacío"
            }
        }

        customDialog.show()
    }

    private fun guardarHábitoEnFirestore(habito: Habito) {
        sharedViewModel.userId.value?.let { userId ->
            val habitoData = mapOf(
                "nombre" to habito.nombre,
                "completado" to habito.completado
            )

            val habitoRef = db.collection("users").document(userId)
                .collection("habitos").document(habito.nombre)

            habitoRef.set(habitoData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Firestore", "Hábito guardado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al guardar el hábito", e)
                }
        }
    }

    private fun obtenerHabitosUsuario(userId: String) {
        val habitosRef = db.collection("users").document(userId)
            .collection("habitos")

        habitosRef.get()
            .addOnSuccessListener { documents ->
                val habitosList = mutableListOf<Habito>()
                for (document in documents) {
                    val nombre = document.getString("nombre") ?: ""
                    val completado = document.getBoolean("completado") ?: false
                    habitosList.add(Habito(nombre, completado))
                }
                adapter.updateHabits(habitosList)
                updateProgress(habitosList)
                updateEmptyViewVisibility()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener hábitos", e)
            }
    }

    private fun updateProgress(items: List<Habito>) {
        val totalHabitos = items.size
        val completados = items.count { it.completado }
        val porcentaje = if (totalHabitos > 0) (completados * 100) / totalHabitos else 0

        animateProgress(progressIndicator.progress, porcentaje)
        animatePercentage(
            progressPercentageTextView.text.toString().replace("%", "").toInt(),
            porcentaje
        )

        habitsCompletedTextView.text = "$completados de $totalHabitos hábitos completados"
    }

    private fun animateProgress(startValue: Int, endValue: Int) {
        ObjectAnimator.ofInt(progressIndicator, "progress", startValue, endValue).apply {
            duration = 500
            start()
        }
    }

    private fun animatePercentage(startValue: Int, endValue: Int) {
        ValueAnimator.ofInt(startValue, endValue).apply {
            duration = 500
            addUpdateListener { animation ->
                progressPercentageTextView.text = "${animation.animatedValue}%"
            }
            start()
        }
    }

    private fun obtenerFechaActual(): String {
        val locale = Locale("es", "ES")
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", locale)
        return formatoFecha.format(Date()).replaceFirstChar { it.titlecase(locale) }
    }

    private fun updateEmptyViewVisibility() {
        val emptyViewContainer: View = view?.findViewById(R.id.empty_habito_container) ?: return
        emptyViewContainer.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}