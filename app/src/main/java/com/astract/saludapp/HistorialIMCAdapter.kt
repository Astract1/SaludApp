package com.astract.saludapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HistorialIMCAdapter(
    private var historialIMCList: MutableList<HistorialIMCData>,
    private val context: Context,
    private val userId: String, // Añadido userId como parámetro del constructor
    private val onItemDeleted: () -> Unit
) : RecyclerView.Adapter<HistorialIMCAdapter.HistorialIMCViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class HistorialIMCViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textFecha: TextView = itemView.findViewById(R.id.textFecha)
        val textPeso: TextView = itemView.findViewById(R.id.textPeso)
        val textAltura: TextView = itemView.findViewById(R.id.textAltura)
        val textResultadoIMC: TextView = itemView.findViewById(R.id.textIMCValor)
        val iconoEliminar: ImageView = itemView.findViewById(R.id.iconoEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialIMCViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardviewhistorial, parent, false)
        return HistorialIMCViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialIMCViewHolder, position: Int) {
        val historialIMC = historialIMCList[position]

        holder.textFecha.text = "Fecha: ${historialIMC.fecha}"
        holder.textPeso.text = "Peso: ${historialIMC.peso} kg"
        holder.textAltura.text = "Altura: ${historialIMC.altura} cm"

        // Determinar el color según el IMC
        val imc = historialIMC.resultadoIMC
        val backgroundColor = when {
            imc < 18.5 -> Color.BLUE
            imc in 18.5..24.9 -> Color.GREEN
            imc in 25.0..29.9 -> Color.YELLOW
            else -> Color.RED
        }

        holder.textResultadoIMC.apply {
            setBackgroundColor(backgroundColor)
            text = "%.2f".format(imc)
            setTextColor(Color.BLACK)
        }

        // Configurar eliminación
        holder.iconoEliminar.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                // Animaciones
                val cardViewAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_cardview_destruir)
                val iconoAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_icono_destruir)

                holder.itemView.startAnimation(cardViewAnimation)
                holder.iconoEliminar.startAnimation(iconoAnimation)

                cardViewAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        val itemToDelete = historialIMCList[currentPosition]
                        db.collection("users")
                            .document(userId)
                            .collection("historial_imc")
                            .document(itemToDelete.id.toString())
                            .delete()
                            .addOnSuccessListener {
                                historialIMCList.removeAt(currentPosition)
                                notifyItemRemoved(currentPosition)
                                notifyItemRangeChanged(currentPosition, historialIMCList.size)
                                onItemDeleted()
                            }
                            .addOnFailureListener { e ->
                                // Manejar el error
                                println("Error al eliminar el registro: ${e.message}")
                            }
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }
    }

    override fun getItemCount() = historialIMCList.size


    private fun getUserId(): String {
        return userId //
    }
}