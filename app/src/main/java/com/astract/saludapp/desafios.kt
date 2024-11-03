package com.astract.saludapp

import Disponibilidad
import IMCRecomendado
import Reto
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import retosAdapter
import java.io.IOException
import java.io.InputStream

class Desafios : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var retosAdapter: retosAdapter
    private val retosList = mutableListOf<Reto>()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userId: String? = null

    private lateinit var noRetosMessage: TextView  // Mensaje para cuando no hay retos

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios, container, false)

        // Inicializa el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inicializa el TextView para el mensaje de no retos
        noRetosMessage = view.findViewById(R.id.noRetosMessage)

        // Obtener el último IMC
        obtenerUltimoIMC()

        return view
    }

    private fun obtenerUltimoIMC() {
        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("historial_imc")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val ultimoIMC = documents.documents[0].getDouble("imc") ?: 0.0
                        Log.d("Desafios", "Último IMC: $ultimoIMC")
                        cargarRetos(ultimoIMC)  // Cargar retos con el último IMC
                    } else {
                        mostrarMensajeSinIMC()
                    }
                }
                .addOnFailureListener {
                    Log.e("Desafios", "Error al cargar el último IMC", it)
                }
        }
    }

    private fun mostrarMensajeSinIMC() {
        noRetosMessage.text = "No hay IMC asignado. Completa tu perfil para acceder a retos."
        noRetosMessage.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun cargarRetos(ultimoIMC: Double) {
        val jsonString = cargarJSONDesdeAssets(requireContext())
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val titulo = jsonObject.getString("titulo")
                val descripcion = jsonObject.getString("descripcion")

                // Extraer el objeto de disponibilidad
                val disponibilidadObj = jsonObject.getJSONObject("disponibilidad")
                val disponibilidad = Disponibilidad(
                    fecha_inicio = disponibilidadObj.getString("fecha_inicio"),
                    fecha_fin = disponibilidadObj.getString("fecha_fin")
                )

                // Extraer el objeto de IMC recomendado, si existe
                val imcRecomendadoObj = jsonObject.optJSONObject("imc_recomendado")
                val imcRecomendado = imcRecomendadoObj?.let {
                    IMCRecomendado(
                        minimo = it.getDouble("minimo"),
                        maximo = it.optDouble("maximo", Double.NaN) // Manejar valor nulo
                    )
                }

                // Verificar que imcRecomendado no sea nulo y esté en el rango
                if (imcRecomendado != null && ultimoIMC >= imcRecomendado.minimo) {
                    val maximoComparar = imcRecomendado.maximo.takeIf { it.isNaN().not() } ?: Double.MAX_VALUE

                    if (ultimoIMC <= maximoComparar) {
                        val reto = Reto(titulo, descripcion, disponibilidad, imcRecomendado)
                        retosList.add(reto)
                        Log.d("Desafios", "Agregando reto: $titulo, IMC: $ultimoIMC")
                    }
                }
            }

            // Inicializa el adapter y pasa el callback
            retosAdapter = retosAdapter(requireContext(), retosList) { reto ->
                Log.d("Desafios", "Te has unido al reto: ${reto.titulo}")
            }

            recyclerView.adapter = retosAdapter

            // Verifica si la lista de retos está vacía
            if (retosList.isEmpty()) {
                noRetosMessage.text = "No hay retos disponibles."
                noRetosMessage.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                noRetosMessage.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            Log.e("Desafios", "Error al cargar retos desde JSON", e)
        }
    }

    private fun cargarJSONDesdeAssets(context: Context): String? {
        return try {
            val inputStream: InputStream = context.assets.open("retos.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}
