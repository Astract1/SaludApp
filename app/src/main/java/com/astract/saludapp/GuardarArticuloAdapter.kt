package com.astract.saludapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astract.saludapp.R
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class GuardarArticuloAdapter(
    private var articulos: List<Articulo>,
    private val onItemClick: (Articulo) -> Unit,
    private val onDeleteClick: (Articulo) -> Unit
) : RecyclerView.Adapter<GuardarArticuloAdapter.ArticuloViewHolder>() {

    class ArticuloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.titulo_articulo_guardado)
        val autorTextView: TextView = view.findViewById(R.id.autor_articulo_guardado)
        val fechaTextView: TextView = view.findViewById(R.id.fecha_articulo_guardado)
        val imagenArticulo: ImageView = view.findViewById(R.id.imagen_articulo_guardado)
        val btnEliminar: ImageView = view.findViewById(R.id.btn_eliminar_articulo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticuloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulo_guardado, parent, false)
        return ArticuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticuloViewHolder, position: Int) {
        val articulo = articulos[position]

        holder.tituloTextView.text = articulo.title
        holder.autorTextView.text = articulo.author ?: "Autor desconocido"
        holder.fechaTextView.text = formatearFecha(articulo.publishedAt)

        Glide.with(holder.imagenArticulo.context)
            .load(articulo.imagenurl)
            .placeholder(R.drawable.no_image)
            .error(R.drawable.no_image)
            .into(holder.imagenArticulo)

        holder.itemView.setOnClickListener { onItemClick(articulo) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(articulo) }
    }

    override fun getItemCount() = articulos.size

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

    fun actualizarArticulos(nuevosArticulos: List<Articulo>) {
        articulos = nuevosArticulos
        notifyDataSetChanged()
    }
}