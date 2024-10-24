package com.astract.saludapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

// Clase de datos para un Reto
data class Reto(
    val titulo: String,
    val descripcion: String,
    val fechaDisponibilidad: String // Agregada para incluir la disponibilidad
)

// Adapter para el RecyclerView
class retosAdapter(
    private val context: Context,
    private val retos: List<Reto>,
    private val onUnirseClick: (Reto) -> Unit // Callback para el botón "Unirse"
) : RecyclerView.Adapter<retosAdapter.RetoViewHolder>() {

    // ViewHolder que mantiene las vistas de cada elemento de la lista
    class RetoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trofeoIcon: ImageView = view.findViewById(R.id.trofeoIcon)
        val tituloReto: TextView = view.findViewById(R.id.tituloReto)
        val descripcionReto: TextView = view.findViewById(R.id.descripcionReto)
        val fechaDisponibilidad: TextView = view.findViewById(R.id.fechaDisponibilidad) // Nueva referencia para la disponibilidad
        val unirseButton: Button = view.findViewById(R.id.unirseButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_reto, parent, false)
        return RetoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RetoViewHolder, position: Int) {
        val reto = retos[position]
        holder.tituloReto.text = reto.titulo
        holder.descripcionReto.text = reto.descripcion
        holder.fechaDisponibilidad.text = reto.fechaDisponibilidad // Establecer el texto de disponibilidad

        holder.unirseButton.setOnClickListener {
            onUnirseClick(reto)
            holder.unirseButton.text = "Inscrito"
            Toast.makeText(context, "Te has unido al reto: ${reto.titulo}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return retos.size
    }
}
