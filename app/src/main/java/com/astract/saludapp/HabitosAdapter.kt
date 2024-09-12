package com.astract.saludapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.CheckBox
import android.widget.TextView

// Define el adaptador para el RecyclerView
class HabitosAdapter(private val items: List<String>) : RecyclerView.Adapter<HabitosAdapter.ViewHolder>() {

    // ViewHolder para manejar cada ítem del RecyclerView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textview_habitos)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_habitos)
    }

    // Crea un nuevo ViewHolder para un ítem en la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardviewhabitos, parent, false)
        return ViewHolder(view)
    }

    // Asocia datos con el ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item
        // Puedes agregar lógica adicional para el CheckBox si es necesario
    }

    // Retorna el número total de ítems en la lista
    override fun getItemCount(): Int {
        return items.size
    }
}
