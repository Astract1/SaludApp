package com.astract.saludapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.astract.saludapp.viewmodel.ArticuloViewModel
import com.astract.saludapp.viewmodel.ArticulosViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import android.widget.ProgressBar
import android.widget.TextView

class Articulos : AppCompatActivity() {

    private lateinit var articuloViewModel: ArticuloViewModel
    private lateinit var adapter: ArticuloAdapter // Asegúrate de tener un adaptador de artículos
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var noResultsTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar // Cambia el nombre de btnVolver a toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articulos) // Asegúrate de que este es tu layout

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        articuloViewModel = ViewModelProvider(this, ArticulosViewModelFactory()).get(ArticuloViewModel::class.java)

        recyclerView = findViewById(R.id.recyclerArticulos) // Asegúrate de tener este ID en tu layout
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ArticuloAdapter(mutableListOf()) { articulo ->
            openArticuloCarga(articulo)
        }
        recyclerView.adapter = adapter

        searchView = findViewById(R.id.searchArticulos) // Asegúrate de tener este ID en tu layout
        searchView.setSuggestionsAdapter(null)

        noResultsTextView = findViewById(R.id.noResultsTextView)
        progressBar = findViewById(R.id.progressBar)

        // Inicialmente ocultar el ProgressBar y el mensaje de no resultados
        progressBar.visibility = View.GONE
        noResultsTextView.visibility = View.GONE

        loadArticulosWithDelay()

        setupSearchView()
    }

    private fun loadArticulosWithDelay() {
        // Muestra el ProgressBar y luego carga los artículos después de un retraso
        progressBar.visibility = View.VISIBLE

        // Usar Handler para retrasar la carga
        Handler().postDelayed({
            articuloViewModel.fetchAndUpdateArticulos(this)
            // Observa el LiveData de artículos
            articuloViewModel.articulos.observe(this) { listaDeArticulos ->
                progressBar.visibility = View.GONE // Oculta el ProgressBar al recibir datos

                val articulosFiltrados = listaDeArticulos.filterNot { articulo ->
                    articulo.title.contains("removed", ignoreCase = true) ||
                            articulo.abstract.contains("removed", ignoreCase = true) ||
                            articulo.title.contains("[Removed]", ignoreCase = true)
                }
                adapter.updateArticulos(articulosFiltrados)

                // Mostrar u ocultar el TextView de "No se encontraron artículos"
                noResultsTextView.visibility =
                    if (articulosFiltrados.isEmpty()) View.VISIBLE else View.GONE
            }
        }, 1000) // Retraso de 1 segundo (1000 ms)
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredArticulos = articuloViewModel.articulos.value?.filter { articulo ->
                    articulo.title.contains(newText ?: "", ignoreCase = true) ||
                            articulo.abstract.contains(newText ?: "", ignoreCase = true)
                } ?: emptyList()

                adapter.updateArticulos(filteredArticulos)
                noResultsTextView.visibility =
                    if (filteredArticulos.isEmpty()) View.VISIBLE else View.GONE

                return true
            }
        })
    }

    private fun openArticuloCarga(articulo: Articulo) {
        val intent = Intent(this, ArticuloCarga::class.java)
        intent.putExtra("ARTICULO_URL", articulo.url) // Pasa la URL
        startActivity(intent)
        // Aplica las animaciones
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
