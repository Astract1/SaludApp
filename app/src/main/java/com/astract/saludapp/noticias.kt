package com.astract.saludapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        noticiasViewModel.noticias.observe(viewLifecycleOwner) { listaDeNoticias ->
            adapter.updateNoticias(listaDeNoticias)
        }

        noticiasViewModel.fetchAndUpdateNews(requireContext())

        noticiasViewModel.loadNoticias()

        return view
    }

    private fun openNoticiasCarga() {
        val intent = Intent(requireContext(), Noticias_Carga::class.java)
        startActivity(intent)
    }
}
