package com.astract.saludapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
class NoticiasAdapter(
    private var noticias: MutableList<Noticia>
) : RecyclerView.Adapter<NoticiasAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.cardTitleN)
        val subtitleTextView: TextView = itemView.findViewById(R.id.cardSubtitleN)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardviewnoticias, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val noticia = noticias[position]
        holder.titleTextView.text = noticia.title
        holder.subtitleTextView.text = noticia.description
    }

    override fun getItemCount(): Int = noticias.size

    // Método para actualizar la lista de noticias
    fun updateNoticias(nuevasNoticias: List<Noticia>) {
        noticias.clear() // Limpia la lista actual
        noticias.addAll(nuevasNoticias) // Agrega las nuevas noticias
        notifyDataSetChanged() // Notifica que los datos han cambiado
    }
}
