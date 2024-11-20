package com.astract.saludapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore
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
            adapter.showHabitCreationDialog(requireContext()) { nuevoHabito ->
                adapter.addHabit(nuevoHabito)
                updateProgress(adapter.getItems())
                updateEmptyViewVisibility()
            }
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

    private fun obtenerHabitosUsuario(userId: String) {
        val habitosRef = db.collection("users").document(userId)
            .collection("habitos")

        habitosRef.get()
            .addOnSuccessListener { documents ->
                val habitosList = mutableListOf<Habito>()
                for (document in documents) {
                    val nombre = document.getString("nombre") ?: ""
                    val completado = document.getBoolean("completado") ?: false
                    val tiempo = document.getString("tiempo") ?: ""
                    val frecuencia = document.getString("frecuencia") ?: ""
                    habitosList.add(Habito(nombre, completado, tiempo, frecuencia))
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