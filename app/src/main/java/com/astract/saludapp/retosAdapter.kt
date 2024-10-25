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

data class Disponibilidad(
    val fecha_inicio: String,
    val fecha_fin: String
)

data class IMCRecomendado(
    val minimo: Double,
    val maximo: Double?
)

data class Reto(
    val titulo: String,
    val descripcion: String,
    val disponibilidad: Disponibilidad,
    val imc_recomendado: IMCRecomendado?
)

// Adapter para el RecyclerView
class retosAdapter(
    private val context: Context,
    private val retos: List<Reto>,
    private val onUnirseClick: (Reto) -> Unit, // Callback para el botón "Unirse"
    private val ultimoIMC: Double // Último IMC del usuario
) : RecyclerView.Adapter<retosAdapter.RetoViewHolder>() {

    class RetoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trofeoIcon: ImageView = view.findViewById(R.id.trofeoIcon)
        val tituloReto: TextView = view.findViewById(R.id.tituloReto)
        val descripcionReto: TextView = view.findViewById(R.id.descripcionReto)
        val unirseButton: Button = view.findViewById(R.id.unirseButton)
        val fechaInicio: TextView = view.findViewById(R.id.fechaInicio)
        val fechaFin: TextView = view.findViewById(R.id.fechaFin)
        val colorBarra: View = view.findViewById(R.id.ColorBarra) // Referencia a ColorBarra
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_reto, parent, false)
        return RetoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RetoViewHolder, position: Int) {
        val reto = retos[position]
        holder.tituloReto.text = reto.titulo
        holder.descripcionReto.text = reto.descripcion
        holder.fechaInicio.text = "Fecha Inicio: ${reto.disponibilidad.fecha_inicio}"
        holder.fechaFin.text = "Fecha Fin: ${reto.disponibilidad.fecha_fin}"

        // Cambiar el color de la barra según el IMC
        when {
            ultimoIMC < 18.5 -> holder.colorBarra.setBackgroundColor(context.resources.getColor(R.color.blue))
            ultimoIMC in 18.5..24.9 -> holder.colorBarra.setBackgroundColor(context.resources.getColor(R.color.green))
            ultimoIMC in 25.0..29.9 -> holder.colorBarra.setBackgroundColor(context.resources.getColor(R.color.yellow))
            else -> holder.colorBarra.setBackgroundColor(context.resources.getColor(R.color.red))
        }

        holder.unirseButton.setOnClickListener {
            onUnirseClick(reto) // Llama al callback
            holder.unirseButton.text = "Inscrito"
            Toast.makeText(context, "Te has unido al reto: ${reto.titulo}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return retos.size
    }
}
