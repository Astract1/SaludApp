package com.astract.saludapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class Sellos_Negros : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SellosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sellos__negros, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSellos)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Cargar los sellos desde el archivo JSON
        val sellos = loadSellosFromJson()

        // Comprobar si se cargaron sellos
        if (sellos.isNotEmpty()) {
            adapter = SellosAdapter(sellos)
            recyclerView.adapter = adapter
        } else {
            Toast.makeText(context, "No se encontraron sellos", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadSellosFromJson(): List<SelloNegro> {
        val inputStream = requireContext().assets.open("sellos.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = bufferedReader.use { it.readText() }

        // Parsear el JSON usando Gson
        val gson = Gson()
        val listType = object : TypeToken<SellosResponse>() {}.type
        val response: SellosResponse = gson.fromJson(jsonString, listType)

        return response.sellos
    }
}
