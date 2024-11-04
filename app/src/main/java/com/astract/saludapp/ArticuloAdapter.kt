package com.astract.saludapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ArticuloAdapter(
    private var articulos: MutableList<Articulo>,
    private val onClick: (Articulo) -> Unit
) : RecyclerView.Adapter<ArticuloAdapter.ArticuloViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticuloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardviewarticulos, parent, false)
        return ArticuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticuloViewHolder, position: Int) {
        val articulo = articulos[position]
        holder.bind(articulo, onClick)
    }

    override fun getItemCount(): Int = articulos.size

    fun updateArticulos(nuevosArticulos: List<Articulo>) {
        articulos.clear()
        articulos.addAll(nuevosArticulos)
        notifyDataSetChanged()
    }

    class ArticuloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.cardTitleArticulo)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.cardSubtitleArticulo)
        private val imageView: ImageView = itemView.findViewById(R.id.cardImageArticulo)
        private val cardButton: View = itemView.findViewById(R.id.cardButtonArticulo)

        fun bind(articulo: Articulo, onClick: (Articulo) -> Unit) {
            titleTextView.text = articulo.title
            subtitleTextView.text = articulo.abstract

            // Verificar si la URL de la imagen es nula o vacía
            if (articulo.imagenurl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(articulo.imagenurl)
                    .placeholder(R.drawable.no_image) // Imagen de carga
                    .error(R.drawable.no_image) // Imagen en caso de error
                    .into(imageView)
            } else {
                // Si no hay URL, establece una imagen predeterminada
                imageView.setImageResource(R.drawable.no_image)
            }

            // Asignar el OnClickListener al cardButton
            cardButton.setOnClickListener {
                onClick(articulo)
            }
        }

        private fun openDetailActivity(context: Context, articulo: Articulo) {
            val intent = Intent(context, ArticuloCarga::class.java)
            intent.putExtra("ARTICULO_URL", articulo.url)
            context.startActivity(intent)
        }
    }
}
