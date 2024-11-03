package com.astract.saludapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class Sellos_Negros : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SellosAdapter
    private lateinit var progressBar: ProgressBar
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sellos__negros, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSellos)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        loadSellosFromFirebase()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
    }

    private fun loadSellosFromFirebase() {
        progressBar.visibility = View.VISIBLE

        db.collection("sellos")
            .get()
            .addOnSuccessListener { documents ->
                val sellosList = mutableListOf<SelloNegro>()

                for (document in documents) {
                    val sello = document.toObject(SelloNegro::class.java)
                    sellosList.add(sello)
                }

                // Ordenar los sellos por ID
                sellosList.sortBy { it.id }

                adapter = SellosAdapter(sellosList)
                recyclerView.adapter = adapter

                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    context,
                    "Error al cargar los sellos: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
