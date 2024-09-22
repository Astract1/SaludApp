package com.astract.saludapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView

class ArticulosAdapter(
    private val articulos: List<Articulo>
) : RecyclerView.Adapter<ArticulosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iconImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val subtitleTextView: TextView = itemView.findViewById(R.id.subtitleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardviewarticulos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val articulo = articulos[position]
        holder.titleTextView.text = articulo.title
        holder.subtitleTextView.text = articulo.description
        holder.imageView.setImageResource(R.drawable.icon_book)
    }

    override fun getItemCount(): Int = articulos.size
}
