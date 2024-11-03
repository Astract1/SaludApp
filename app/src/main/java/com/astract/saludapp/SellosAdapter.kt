package com.astract.saludapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SellosAdapter(private val sellos: List<SelloNegro>) : RecyclerView.Adapter<SellosAdapter.SelloViewHolder>() {

    inner class SelloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.cardImageSello)
        val nombre: TextView = view.findViewById(R.id.cardTitleSello)
        val resumen: TextView = view.findViewById(R.id.cardSubtitleSello)

        init {
            // Configurar el tamaño de la ImageView
            imagen.layoutParams = imagen.layoutParams.apply {
                width = 300
                height = 300
            }
            imagen.scaleType = ImageView.ScaleType.CENTER_CROP

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val sello = sellos[position]
                    val intent = Intent(itemView.context, info_sellos_negros::class.java)
                    intent.putExtra("sello_id", sello.id.toString())
                    itemView.context.startActivity(intent)
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

        // Configurar opciones de Glide para el tamaño y el formato
        val requestOptions = RequestOptions()
            .override(300, 300)
            .centerCrop()
            .placeholder(R.drawable.no_image)
            .error(R.drawable.no_image)

        // Cargar la imagen con Glide usando las opciones configuradas
        Glide.with(holder.itemView.context)
            .load(sello.imagen_url)
            .apply(requestOptions)
            .into(holder.imagen)
    }

    override fun getItemCount(): Int = sellos.size
}
