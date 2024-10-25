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
import com.astract.saludapp.database.MyDatabaseHelper
import org.json.JSONArray
import org.json.JSONObject
import retosAdapter
import java.io.IOException
import java.io.InputStream

class desafios : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var retosAdapter: retosAdapter
    private val retosList = mutableListOf<Reto>()
    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var noRetosMessage: TextView  // Agregar esta línea

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios, container, false)
        dbHelper = MyDatabaseHelper(requireContext())

        // Inicializa el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inicializa el TextView para el mensaje de no retos
        noRetosMessage = view.findViewById(R.id.noRetosMessage)

        // Obtén el último IMC
        val ultimoIMC = dbHelper.obtenerUltimoIMC() ?: 0.0

        // Verifica si el último IMC es 0.0 o está vacío
        if (ultimoIMC <= 0.0) {
            noRetosMessage.text = "No hay IMC asignado. Completa tu perfil para acceder a retos."
            noRetosMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            cargarRetos()  // Cargar retos solo si hay un IMC válido

            // Inicializa el adapter y pasa el callback
            retosAdapter = retosAdapter(requireContext(), retosList, { reto ->
                Log.d("desafios", "Te has unido al reto: ${reto.titulo}")
            }, ultimoIMC)

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
        }

        return view
    }

    private fun cargarRetos() {
        val jsonString = cargarJSONDesdeAssets(requireContext())
        val ultimoIMC = dbHelper.obtenerUltimoIMC() ?: 0.0 // Obtén el último IMC
        Log.d("desafios", "Ultimo IMC: $ultimoIMC")
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
                val imcRecomendado = if (imcRecomendadoObj != null) {
                    IMCRecomendado(
                        minimo = imcRecomendadoObj.getDouble("minimo"),
                        maximo = imcRecomendadoObj.optDouble("maximo", Double.NaN) // Manejar valor nulo
                    )
                } else {
                    null
                }

                // Verificar que imcRecomendado no sea nulo
                if (imcRecomendado != null) {
                    Log.d("desafios", "IMC Recomendado: Minimo: ${imcRecomendado.minimo}, Maximo: ${imcRecomendado.maximo}")

                    // Verifica si el último IMC está dentro del rango del reto
                    if (ultimoIMC >= imcRecomendado.minimo) {
                        // Comprobar el límite máximo (usando el valor predeterminado si es nulo)
                        val maximoComparar = if (imcRecomendado.maximo != null && !imcRecomendado.maximo.isNaN()) {
                            imcRecomendado.maximo
                        } else {
                            Double.MAX_VALUE // Si es nulo, consideramos como un valor muy alto
                        }

                        // Comprobamos si el último IMC es menor o igual que el máximo permitido
                        if (ultimoIMC <= maximoComparar) {
                            val reto = Reto(titulo, descripcion, disponibilidad, imcRecomendado)
                            retosList.add(reto)
                            Log.d("desafios", "Agregando reto: $titulo, IMC: $ultimoIMC")
                        }
                    } else {
                        Log.d("desafios", "El IMC $ultimoIMC no está en el rango recomendado para el reto: $titulo")
                    }
                } else {
                    Log.d("desafios", "imcRecomendado es null para el reto: $titulo")
                }
            }
        } catch (e: Exception) {
            Log.e("desafios", "Error al cargar retos desde JSON", e)
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

