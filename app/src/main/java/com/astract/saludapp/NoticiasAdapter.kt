package com.astract.saludapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NoticiasAdapter(
    private var noticias: MutableList<Noticia>,
    private val onClick: (Noticia) -> Unit
) : RecyclerView.Adapter<NoticiasAdapter.NoticiaViewHolder>() {

    private val firestore: FirebaseFirestore = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardviewnoticias, parent, false)
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

    fun fetchNoticiasFromFirestore() {
        firestore.collection("noticias")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedNoticias = mutableListOf<Noticia>()
                for (document in documents) {

                    val noticia = document.toObject(Noticia::class.java).copy(id = document.id.toInt())
                    fetchedNoticias.add(noticia)
                }
                updateNoticias(fetchedNoticias) // Actualiza la lista del adaptador
                Log.d("NoticiasAdapter", "Noticias obtenidas: ${fetchedNoticias.size}")
            }
            .addOnFailureListener { exception ->
                Log.w("NoticiasAdapter", "Error al obtener las noticias.", exception)
            }
    }

    class NoticiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.cardTitleN)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.cardSubtitleN)
        private val imageView: ImageView = itemView.findViewById(R.id.cardImageN)
        private val cardButton: View = itemView.findViewById(R.id.cardButton)

        fun bind(noticia: Noticia, onClick: (Noticia) -> Unit) {
            titleTextView.text = noticia.title
            subtitleTextView.text = noticia.description

            Log.d("NoticiasAdapter", "Cargando imagen desde: ${noticia.urlToImage}")

            if (noticia.urlToImage.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(noticia.urlToImage)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.no_image)
            }

            cardButton.setOnClickListener {
                onClick(noticia)
                openDetailActivity(itemView.context, noticia)
            }
        }

        private fun openDetailActivity(context: Context, noticia: Noticia) {
            val intent = Intent(context, Noticias_Carga::class.java)
            intent.putExtra("NOTICIA_URL", noticia.url)
            Log.d("NoticiasAdapter", "Abriendo noticia: ${noticia.url}")
            context.startActivity(intent)
        }

    }
}
