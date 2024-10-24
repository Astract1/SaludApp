package com.astract.saludapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.io.IOException
import java.io.InputStream

class desafios : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var retosAdapter: retosAdapter
    private val retosList = mutableListOf<Reto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Manejo de argumentos si es necesario
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_desafios, container, false)

        // Inicializa el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)


        cargarRetos()

        retosAdapter = retosAdapter(requireContext(), retosList) { reto ->

        }
        recyclerView.adapter = retosAdapter

        return view
    }

    private fun cargarRetos() {
        val jsonString = cargarJSONDesdeAssets(requireContext())
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val titulo = jsonObject.getString("titulo")
                val descripcion = jsonObject.getString("descripcion")
                val disponibilidad = jsonObject.getString("disponibilidad")

                // Crea un nuevo objeto Reto y agrégalo a la lista
                val reto = Reto(titulo, descripcion, disponibilidad)
                retosList.add(reto)
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


    companion object {
        // Puedes agregar métodos estáticos aquí si es necesario
    }
}
