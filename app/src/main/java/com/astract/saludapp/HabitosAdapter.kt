package com.astract.saludapp

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions



class HabitosAdapter(
    private val items: MutableList<Habito>,
    private val onHabitChanged: (List<Habito>) -> Unit
) : RecyclerView.Adapter<HabitosAdapter.ViewHolder>() {

    private lateinit var db: FirebaseFirestore
    private var userId: String? = null

    fun setUserId(newUserId: String) {
        userId = newUserId
        db = FirebaseFirestore.getInstance()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textview_habitos)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_habitos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardviewhabitos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.nombre
        holder.checkBox.isChecked = item.completado

        // Removemos el listener anterior para evitar llamadas múltiples
        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.completado = isChecked
            userId?.let { uid ->
                updateHabitInFirestore(uid, item)
            }
            onHabitChanged(items)
        }
    }

    private fun updateHabitInFirestore(userId: String, habito: Habito) {
        val habitRef = db.collection("users").document(userId)
            .collection("habitos").document(habito.nombre)

        val habitData = mapOf(
            "nombre" to habito.nombre,
            "completado" to habito.completado,
            "tiempo" to habito.tiempo,
            "frecuencia" to habito.frecuencia
        )

        habitRef.set(habitData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("HabitosAdapter", "Hábito actualizado exitosamente: ${habito.nombre}")
            }
            .addOnFailureListener { e ->
                Log.e("HabitosAdapter", "Error actualizando hábito: ${habito.nombre}", e)
            }
    }

    override fun getItemCount(): Int = items.size

    fun addHabit(habit: Habito) {
        items.add(habit)
        userId?.let { uid ->
            updateHabitInFirestore(uid, habit)
        }
        notifyItemInserted(items.size - 1)
        onHabitChanged(items)
    }

    fun updateHabits(habitos: List<Habito>) {
        items.clear()
        items.addAll(habitos)
        notifyDataSetChanged()
    }

    fun getItems(): List<Habito> = items.toList()

    fun showHabitCreationDialog(context: android.content.Context, onHabitCreated: (Habito) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null)

        // Find views
        val nombreHábitoEditText: TextInputEditText = dialogView.findViewById(R.id.text_input_edit_text_nombre_habito)
        val spinnerTiempo: Spinner = dialogView.findViewById(R.id.spinner_tiempo)
        val spinnerFrecuencia: Spinner = dialogView.findViewById(R.id.spinner_frecuencia)
        val customTimeLayout: TextInputLayout = dialogView.findViewById(R.id.custom_time_layout)
        val customTimeEditText: TextInputEditText = dialogView.findViewById(R.id.custom_time_edit_text)
        val customFrequencyLayout: TextInputLayout = dialogView.findViewById(R.id.custom_frequency_layout)
        val customFrequencyEditText: TextInputEditText = dialogView.findViewById(R.id.custom_frequency_edit_text)
        val guardarButton: MaterialButton = dialogView.findViewById(R.id.boton_guardar_habito)

        // Setup spinners
        val tiempoAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.tiempo_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerTiempo.adapter = tiempoAdapter

        val frecuenciaAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.frecuencia_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerFrecuencia.adapter = frecuenciaAdapter

        // Handle spinner selections for tiempo
        spinnerTiempo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                customTimeLayout.visibility = if (pos == parent?.count?.minus(1)) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Handle spinner selections for frecuencia
        spinnerFrecuencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                customFrequencyLayout.visibility = if (pos == parent?.count?.minus(1)) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Create and show dialog
        val customDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        guardarButton.setOnClickListener {
            val nombreHábito = nombreHábitoEditText.text.toString().trim()
            val tiempo = if (spinnerTiempo.selectedItemPosition == spinnerTiempo.count - 1) {
                customTimeEditText.text.toString().trim()
            } else {
                spinnerTiempo.selectedItem.toString()
            }
            val frecuencia = if (spinnerFrecuencia.selectedItemPosition == spinnerFrecuencia.count - 1) {
                customFrequencyEditText.text.toString().trim()
            } else {
                spinnerFrecuencia.selectedItem.toString()
            }

            // Validate inputs
            when {
                nombreHábito.isEmpty() -> {
                    nombreHábitoEditText.error = "El nombre del hábito no puede estar vacío"
                    return@setOnClickListener
                }
                tiempo.isEmpty() -> {
                    Toast.makeText(context, "Seleccione un tiempo", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                frecuencia.isEmpty() -> {
                    Toast.makeText(context, "Seleccione una frecuencia", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else -> {
                    val nuevoHábito = Habito(nombreHábito, false, tiempo, frecuencia)
                    onHabitCreated(nuevoHábito)
                    customDialog.dismiss()
                }
            }
        }

        customDialog.show()
    }
}