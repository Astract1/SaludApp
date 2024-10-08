package com.astract.saludapp

import android.content.Context
import android.content.Intent
import android.util.Log // Importar Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NoticiasAdapter(
    private var noticias: MutableList<Noticia>,
    private val onClick: (Noticia) -> Unit
) : RecyclerView.Adapter<NoticiasAdapter.NoticiaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardviewnoticias, parent, false)
        return NoticiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) {
        val noticia = noticias[position]
        holder.bind(noticia, onClick)
    }

    override fun getItemCount(): Int = noticias.size

    fun updateNoticias(nuevasNoticias: List<Noticia>) {
        noticias.clear()
        noticias.addAll(nuevasNoticias)
        notifyDataSetChanged()
    }

    class NoticiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.cardTitleN)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.cardSubtitleN)
        private val imageView: ImageView = itemView.findViewById(R.id.cardImageN) // ImageView para la imagen
        private val cardButton: View = itemView.findViewById(R.id.cardButton) // Usando el LinearLayout como botón

        fun bind(noticia: Noticia, onClick: (Noticia) -> Unit) {
            titleTextView.text = noticia.title
            subtitleTextView.text = noticia.description

            // Imprimir la URL de la imagen antes de cargarla
            Log.d("NoticiasAdapter", "Cargando imagen desde: ${noticia.urlToImage}")

            // Verificar si la URL de la imagen es nula o vacía
            if (noticia.urlToImage.isNotEmpty()) {
                // Cargar la imagen usando Glide
                Glide.with(itemView.context)
                    .load(noticia.urlToImage)
                    .placeholder(R.drawable.no_image) // Imagen de carga o error
                    .error(R.drawable.no_image) // Imagen en caso de error
                    .into(imageView)
            } else {
                // Si no hay URL, establece una imagen predeterminada
                imageView.setImageResource(R.drawable.no_image)
            }

            // Asignar el OnClickListener al cardButton
            cardButton.setOnClickListener {
                onClick(noticia) // Llamar al callback con la noticia
                openDetailActivity(itemView.context, noticia) // Abrir nueva actividad
            }
        }

        private fun openDetailActivity(context: Context, noticia: Noticia) {
            val intent = Intent(context, Noticias_Carga::class.java)
            intent.putExtra("NOTICIA_ID", noticia.id) // O cualquier otro dato que necesites pasar
            context.startActivity(intent) // Iniciar la nueva actividad
        }
    }
}
