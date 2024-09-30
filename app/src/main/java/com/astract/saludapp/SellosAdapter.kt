package com.astract.saludapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SellosAdapter(private val sellos: List<SelloNegro>) : RecyclerView.Adapter<SellosAdapter.SelloViewHolder>() {

    inner class SelloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.cardImageSello)
        val nombre: TextView = view.findViewById(R.id.cardTitleSello)
        val resumen: TextView = view.findViewById(R.id.cardSubtitleSello)

        init {
            // Agregar el listener para el clic en el item
            itemView.setOnClickListener {
                // Recuperar la información del sello al hacer clic
                val context = itemView.context
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    val sello = sellos[position]
                    val intent = Intent(context, info_sellos_negros::class.java)
                    // Convierte el id a String antes de pasarlo
                    intent.putExtra("sello_id", sello.id.toString())
                    context.startActivity(intent)
                }
            }
        }
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardviewsellos, parent, false)
        return SelloViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelloViewHolder, position: Int) {
        val sello = sellos[position]
        holder.nombre.text = sello.nombre
        holder.resumen.text = sello.resumen

        // Cargar la imagen con Glide
        Glide.with(holder.itemView.context)
            .load(sello.imagen_url)
            .into(holder.imagen)
    }

    override fun getItemCount(): Int {
        return sellos.size
    }
}
