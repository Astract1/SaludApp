package com.astract.saludapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavegacion()
    }

    private fun setupNavegacion() {
        try {
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val navController = navHostFragment.navController

            // Configurar el BottomNavigationView con el NavController
            NavigationUI.setupWithNavController(bottomNavigationView, navController)

            // Establecer el listener para el BottomNavigationView
            bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
                val currentDestinationId = navController.currentDestination?.id

                when (menuItem.itemId) {
                    R.id.habitos -> {
                        navigateToFragment(navController, currentDestinationId, R.id.habitos)
                        true
                    }
                    R.id.desafios -> {
                        navigateToFragment(navController, currentDestinationId, R.id.desafios)
                        true
                    }
                    R.id.imc -> {
                        navigateToFragment(navController, currentDestinationId, R.id.imc)
                        true
                    }
                    R.id.noticias -> {
                        navigateToFragment(navController, currentDestinationId, R.id.noticias)
                        true
                    }
                    R.id.Sellos_Negros -> {
                        navigateToFragment(navController, currentDestinationId, R.id.Sellos_Negros)
                        true
                    }
                    R.id.historial_imc -> {
                        navigateToFragment(navController, currentDestinationId, R.id.historial_imc)
                        true
                    }
                    else -> NavigationUI.onNavDestinationSelected(menuItem, navController)
                }
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error configurando la navegación", e)
        }
    }

    private fun navigateToFragment(navController: NavController, currentDestinationId: Int?, targetFragmentId: Int) {
        if (currentDestinationId == targetFragmentId) {
            // Si ya estamos en el fragmento objetivo, volver a cargarlo
            navController.popBackStack(targetFragmentId, false)
        }
        navController.navigate(targetFragmentId)
    }
}
