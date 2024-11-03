package com.astract.saludapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private var userId: String? = null
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        Log.d("MainActivity", "Firebase inicializado")

        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return
        } else {
            userId = sharedPreferences.getString("userId", null) // Asignar a la variable de clase
            if (userId != null) {
                Log.d("MainActivity", "UID del usuario: $userId")
                sharedViewModel.setUserId(userId) // Establecer userId en el ViewModel
            } else {
                Log.e("MainActivity", "No se encontró UID del usuario en SharedPreferences")
            }
        }

        setContentView(R.layout.activity_main)
        solicitarPermisos()
        setupNavegacion()

        val profileIcon = findViewById<ImageView>(R.id.profile_icon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, perfil::class.java)
            intent.putExtra("userId", userId)
            Log.d("MainActivity", "Enviando userId a perfil: $userId")
            startActivity(intent)
        }
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

    private fun solicitarPermisos() {
        // Para Android 13 (API 33) y superiores - Notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    123
                )
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent().apply {
                    action = "android.settings.REQUEST_SCHEDULE_EXACT_ALARM"
                }
                startActivity(intent)
            }
        }
    }

    private fun navigateToFragment(navController: NavController, currentDestinationId: Int?, targetFragmentId: Int) {
        if (currentDestinationId == targetFragmentId) {
            navController.popBackStack(targetFragmentId, false)
        } else {
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            navController.navigate(targetFragmentId, bundle)
        }
    }
}
