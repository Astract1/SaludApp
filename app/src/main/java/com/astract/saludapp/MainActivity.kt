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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private var userId: String? = null
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        Log.d("MainActivity", "Firebase inicializado")

        // Verificar sesión
        if (!checkSession()) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_main)
        solicitarPermisos()
        setupNavegacion()
        setupProfileIcon()
    }

    private fun checkSession(): Boolean {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        userId = sharedPreferences.getString("userId", null)

        if (isLoggedIn && userId != null) {
            Log.d("MainActivity", "Sesión activa - UID del usuario: $userId")
            sharedViewModel.setUserId(userId)
            return true
        }

        Log.d("MainActivity", "No hay sesión activa")
        return false
    }

    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupProfileIcon() {
        val profileIcon = findViewById<ImageView>(R.id.profile_icon)

        // Cargar la imagen del perfil desde Firebase
        userId?.let { uid ->
            FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileImageUrl = document.getString("profileImage")

                        // Cargar la imagen usando Glide
                        Glide.with(this)
                            .load(profileImageUrl)
                            .circleCrop() // Para hacer la imagen circular
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .into(profileIcon)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error loading profile image: ${e.message}")
                    // Cargar imagen por defecto en caso de error
                    profileIcon.setImageResource(R.drawable.avatar)
                }
        }

        // Mantener el OnClickListener
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
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val navController = navHostFragment.navController

            NavigationUI.setupWithNavController(bottomNavigationView, navController)

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

    private fun navigateToFragment(
        navController: NavController,
        currentDestinationId: Int?,
        destinationId: Int
    ) {
        if (currentDestinationId != destinationId) {
            navController.navigate(destinationId)
        }
    }

    private fun solicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        // Verificar y solicitar permiso para SCHEDULE_EXACT_ALARM
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Por favor, habilita los permisos de alarma exacta para las notificaciones",
                    Toast.LENGTH_LONG
                ).show()
                // Abrir configuración de permisos de alarma
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "Permiso de notificaciones concedido",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Permiso de notificaciones denegado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    fun logout() {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        FirebaseAuth.getInstance().signOut()
        redirectToLogin()
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1
    }
}
