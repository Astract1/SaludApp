package com.astract.saludapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.CheckBox
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Define el adaptador para el RecyclerView
class HabitosAdapter(
    private val items: MutableList<Habito>,

    private val onHabitChanged: (List<Habito>) -> Unit
) : RecyclerView.Adapter<HabitosAdapter.ViewHolder>() {

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
        holder.textView.text = item.nombre
        holder.checkBox.isChecked = item.completado

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.completado = isChecked
            // Llama a la función para animar el CheckBox cuando se marca
            if (isChecked) {
                animateCheckBox(holder.checkBox)
            }
            // Actualiza el progreso en el fragmento
            onHabitChanged(items)
        }
    }

    // Retorna el número total de ítems en la lista
    override fun getItemCount(): Int {
        return items.size
    }

    // Método para animar el CheckBox
    private fun animateCheckBox(checkBox: CheckBox) {
        val scaleX = ObjectAnimator.ofFloat(checkBox, "scaleX", 1.0f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(checkBox, "scaleY", 1.0f, 1.2f, 1.0f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 300 // Duración de la animación en milisegundos
        animatorSet.start()
    }

    // Método para añadir un nuevo hábito y notificar cambios
    fun addHabit(habit: Habito) {
        items.add(habit)
        notifyItemInserted(items.size - 1)
    }

    // Método para obtener la lista de hábitos
    fun getItems(): List<Habito> {
        return items
    }


    private fun obtenerFechaActual(): String {
        val locale = Locale("es", "ES")
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", locale)
        val fecha = Date()
        val fechaFormateada = formatoFecha.format(fecha)

        return fechaFormateada.replaceFirstChar { it.titlecase(locale) }
    }
}
