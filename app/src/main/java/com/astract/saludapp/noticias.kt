package com.astract.saludapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astract.saludapp.database.MyDatabaseHelper
import com.astract.saludapp.viewmodel.NoticiasViewModel
import com.astract.saludapp.viewmodel.NoticiasViewModelFactory

class noticias : Fragment() {

    private lateinit var noticiasViewModel: NoticiasViewModel
    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var adapter: NoticiasAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var noResultsTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonArticulos: Button // Nueva referencia al botón

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_noticias, container, false)

        dbHelper = MyDatabaseHelper(requireContext())
        noticiasViewModel = NoticiasViewModelFactory(dbHelper).create(NoticiasViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerNoticias)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NoticiasAdapter(mutableListOf()) { _ -> openNoticiasCarga() }
        recyclerView.adapter = adapter

        searchView = view.findViewById(R.id.searchNoticias)
        searchView.setSuggestionsAdapter(null)

        noResultsTextView = view.findViewById(R.id.noResultsTextView)
        progressBar = view.findViewById(R.id.progressBar)
        buttonArticulos = view.findViewById(R.id.buttonArticulos) // Inicializa el botón

        // Inicialmente ocultar el ProgressBar y el mensaje de no resultados
        progressBar.visibility = View.GONE
        noResultsTextView.visibility = View.GONE

        // Configura el OnClickListener para el botón
        buttonArticulos.setOnClickListener {
            openArticulosCarga() // Método para abrir la actividad de artículos
        }

        // Iniciar la carga de noticias después de un breve retraso
        loadNoticiasWithDelay()

        setupSearchView()

        return view
    }

    private fun loadNoticiasWithDelay() {
        // Muestra el ProgressBar y luego carga las noticias después de un retraso
        progressBar.visibility = View.VISIBLE

        android.os.Handler().postDelayed({
            noticiasViewModel.fetchAndUpdateNews(requireContext())
            noticiasViewModel.loadNoticias()

            // Observa el LiveData de noticias
            noticiasViewModel.noticias.observe(viewLifecycleOwner) { listaDeNoticias ->
                progressBar.visibility = View.GONE // Oculta el ProgressBar al recibir datos
                val noticiasFiltradas = listaDeNoticias.filterNot { noticia ->
                    noticia.title.contains("removed", ignoreCase = true) ||
                            noticia.description.contains("removed", ignoreCase = true) ||
                            noticia.title.contains("[Removed]", ignoreCase = true)
                }
                adapter.updateNoticias(noticiasFiltradas)

                // Mostrar u ocultar el TextView de "No se encontraron noticias"
                noResultsTextView.visibility =
                    if (noticiasFiltradas.isEmpty()) View.VISIBLE else View.GONE
            }
        }, 1000) // Retraso de 1 segundo (1000 ms)
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredNoticias = noticiasViewModel.noticias.value?.filter { noticia ->
                    noticia.title.contains(newText ?: "", ignoreCase = true) ||
                            noticia.description.contains(newText ?: "", ignoreCase = true)
                } ?: emptyList()

                adapter.updateNoticias(filteredNoticias)
                noResultsTextView.visibility =
                    if (filteredNoticias.isEmpty()) View.VISIBLE else View.GONE

                return true
            }
        })
    }

    private fun openNoticiasCarga() {
        val intent = Intent(requireContext(), Noticias_Carga::class.java)
        startActivity(intent)
        // Aplica las animaciones
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun openArticulosCarga() {
        val intent = Intent(requireContext(), Articulos::class.java)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
