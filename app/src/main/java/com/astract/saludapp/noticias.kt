package com.astract.saludapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astract.saludapp.database.MyDatabaseHelper
import com.astract.saludapp.viewmodel.NoticiasViewModel
import com.astract.saludapp.viewmodel.NoticiasViewModelFactory

class noticias : Fragment() {

    private lateinit var noticiasViewModel: NoticiasViewModel
    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var adapter: NoticiasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_noticias, container, false)

        dbHelper = MyDatabaseHelper(requireContext())
        noticiasViewModel = ViewModelProvider(this, NoticiasViewModelFactory(dbHelper)).get(NoticiasViewModel::class.java)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerNoticias)
        adapter = NoticiasAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        noticiasViewModel.noticias.observe(viewLifecycleOwner) { listaDeNoticias ->
            adapter.updateNoticias(listaDeNoticias)
        }

        // Carga las noticias de la base de datos
        noticiasViewModel.loadNoticias()

        noticiasViewModel.fetchAndUpdateNews(requireContext())


        return view


    }
}
