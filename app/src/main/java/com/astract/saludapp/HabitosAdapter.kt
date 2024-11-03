package com.astract.saludapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
            "completado" to habito.completado
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
    }

    fun updateHabits(habitos: List<Habito>) {
        items.clear()
        items.addAll(habitos)
        notifyDataSetChanged()
    }

    fun getItems(): List<Habito> = items.toList()
}