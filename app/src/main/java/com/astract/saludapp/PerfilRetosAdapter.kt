package com.astract.saludapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class PerfilRetosAdapter(
    private val retos: MutableList<perfil.RetoSimple>,
    private val onRetoCompletadoListener: (String, Boolean) -> Unit,
    private val onRetoEliminadoListener: (String) -> Unit

) : RecyclerView.Adapter<PerfilRetosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.titulo_reto)
        val descripcionTextView: TextView = view.findViewById(R.id.descripcion_reto)
        val checkboxCompletado: CheckBox = view.findViewById(R.id.checkbox_completado)
        val cardView: CardView = view.findViewById(R.id.reto_card)
        val deleteButton: ImageView = view.findViewById(R.id.delete_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reto_perfil, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reto = retos[position]
        holder.tituloTextView.text = reto.titulo
        holder.descripcionTextView.text = reto.descripcion

        // Establecer el estado inicial del checkbox sin activar el listener
        holder.checkboxCompletado.setOnCheckedChangeListener(null)
        holder.checkboxCompletado.isChecked = reto.completado

        // Actualizar el fondo de la tarjeta según el estado inicial
        actualizarFondoTarjeta(holder.cardView, reto.completado, holder.itemView.context)

        // Configurar el listener del checkbox
        holder.checkboxCompletado.setOnCheckedChangeListener { _, isChecked ->
            reto.completado = isChecked
            onRetoCompletadoListener(reto.titulo, isChecked)
            actualizarFondoTarjeta(holder.cardView, isChecked, holder.itemView.context)
        }

        holder.deleteButton.setOnClickListener {
            mostrarDialogoConfirmacion(holder.itemView.context, reto, position)
        }
    }

    private fun actualizarFondoTarjeta(cardView: CardView, completado: Boolean, context: android.content.Context) {
        val colorRes = if (completado) {
            R.color.completado_background // Debes definir este color en colors.xml
        } else {
            R.color.white
        }
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, colorRes))
    }

    private fun mostrarDialogoConfirmacion(context: Context, reto: perfil.RetoSimple, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar este reto de tu lista? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarReto(position)
                onRetoEliminadoListener(reto.titulo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarReto(position: Int) {
        retos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, retos.size)
    }

    override fun getItemCount() = retos.size
}
