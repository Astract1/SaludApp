package com.astract.saludapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class GuardarNoticiaAdapter(
    private var noticias: List<Noticia>,
    private val onItemClick: (Noticia) -> Unit,
    private val onDeleteClick: (Noticia) -> Unit
) : RecyclerView.Adapter<GuardarNoticiaAdapter.NoticiaViewHolder>() {

    class NoticiaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.titulo_noticia_guardada)
        val fuenteTextView: TextView = view.findViewById(R.id.fuente_noticia_guardada)
        val fechaTextView: TextView = view.findViewById(R.id.fecha_noticia_guardada)
        val imagenNoticia: ImageView = view.findViewById(R.id.imagen_noticia_guardada)
        val btnEliminar: ImageView = view.findViewById(R.id.btn_eliminar_noticia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_noticia_guardada, parent, false)
        return NoticiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) {
        val noticia = noticias[position]

        holder.tituloTextView.text = noticia.title
        holder.fuenteTextView.text = noticia.source?.name ?: "Fuente desconocida"
        holder.fechaTextView.text = formatearFecha(noticia.publishedAt)

        Glide.with(holder.imagenNoticia.context)
            .load(noticia.urlToImage)
            .placeholder(R.drawable.no_image)
            .error(R.drawable.no_image)
            .into(holder.imagenNoticia)

        holder.itemView.setOnClickListener { onItemClick(noticia) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(noticia) }
    }

    override fun getItemCount() = noticias.size

    private fun formatearFecha(fechaString: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fecha = inputFormat.parse(fechaString)
            return fecha?.let { outputFormat.format(it) } ?: fechaString
        } catch (e: Exception) {
            return fechaString
        }
    }

    fun actualizarNoticias(nuevasNoticias: List<Noticia>) {
        noticias = nuevasNoticias
        notifyDataSetChanged()
    }
}