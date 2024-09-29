package com.astract.saludapp

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astract.saludapp.database.MyDatabaseHelper

// Adapter para el RecyclerView
class HistorialIMCAdapter(
    private var historialIMCList: MutableList<HistorialIMCData>,
    context: Context,
    private val onItemDeleted: () -> Unit
) : RecyclerView.Adapter<HistorialIMCAdapter.HistorialIMCViewHolder>() {

    // Instancia de la base de datos, con el contexto pasado desde el constructor
    private val myDatabaseHelper: MyDatabaseHelper = MyDatabaseHelper(context)

    // ViewHolder para cada elemento del historial
    class HistorialIMCViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textFecha: TextView = itemView.findViewById(R.id.textFecha)
        val textPeso: TextView = itemView.findViewById(R.id.textPeso)
        val textAltura: TextView = itemView.findViewById(R.id.textAltura)
        val textResultadoIMC: TextView = itemView.findViewById(R.id.textIMCValor)
        val iconoEliminar: ImageView = itemView.findViewById(R.id.iconoEliminar)
    }

    // Inflar el layout del ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialIMCViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardviewhistorial, parent, false)
        return HistorialIMCViewHolder(view)
    }

    // Vincular los datos al ViewHolder
    override fun onBindViewHolder(holder: HistorialIMCViewHolder, position: Int) {
        // Obtener el historialIMC usando la posición pasada
        val historialIMC = historialIMCList[position]
        Log.d("HistorialIMCAdapter", "Posición: $position, Fecha: ${historialIMC.fecha}, Peso: ${historialIMC.peso}, Altura: ${historialIMC.altura}, IMC: ${historialIMC.resultadoIMC}")

        holder.textFecha.text = "Fecha: ${historialIMC.fecha}"
        holder.textPeso.text = "Peso: ${historialIMC.peso} kg"
        holder.textAltura.text = "Altura: ${historialIMC.altura} m"

        // Determinar el color de fondo según el IMC
        val imc = historialIMC.resultadoIMC
        when {
            imc < 18.5 -> {
                holder.textResultadoIMC.setBackgroundColor(Color.BLUE) // Color azul
            }
            imc in 18.5..24.9 -> {
                holder.textResultadoIMC.setBackgroundColor(Color.GREEN) // Color verde
            }
            imc in 25.0..29.9 -> {
                holder.textResultadoIMC.setBackgroundColor(Color.YELLOW) // Color amarillo
            }
            else -> {
                holder.textResultadoIMC.setBackgroundColor(Color.RED) // Color rojo
            }
        }

        // Establecer solo el valor del IMC sin alterar el color del texto
        holder.textResultadoIMC.text = "%.2f".format(historialIMC.resultadoIMC)
        holder.textResultadoIMC.setTextColor(Color.BLACK)

        // Configurar el icono de eliminar
        holder.iconoEliminar.setOnClickListener {
            // Obtener la posición actual usando holder.adapterPosition
            val currentPosition = holder.adapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                // Crear y cargar la animación para el CardView
                val cardViewAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.anim_cardview_destruir)
                holder.itemView.startAnimation(cardViewAnimation)

                // Crear y cargar la animación para el ícono de eliminación
                val iconoAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.anim_icono_destruir)
                holder.iconoEliminar.startAnimation(iconoAnimation)

                // Listener para saber cuándo termina la animación del CardView
                cardViewAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        // Eliminar el registro de la base de datos
                        val deleted = myDatabaseHelper.deleteHistorialIMCById(historialIMCList[currentPosition].id)
                        if (deleted) {
                            // Eliminar de la lista y notificar al adaptador
                            historialIMCList.removeAt(currentPosition)
                            notifyItemRemoved(currentPosition)
                            notifyItemRangeChanged(currentPosition, historialIMCList.size)
                            Log.d("HistorialIMCAdapter", "Registro eliminado: ${historialIMC.fecha}")

                            // Notificar al fragmento para actualizar el gráfico
                            onItemDeleted()

                        } else {
                            Log.e("HistorialIMCAdapter", "Error al eliminar el registro con ID: ${historialIMCList[currentPosition].id}")
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }
    }

    // Retornar el tamaño de la lista
    override fun getItemCount(): Int {
        return historialIMCList.size
    }
}
