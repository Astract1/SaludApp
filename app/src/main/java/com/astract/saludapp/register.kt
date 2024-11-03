package com.astract.saludapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.astract.saludapp.databinding.ActivityRegisterBinding

class register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Botón de registro con email
        binding.buttonRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        // Botón de registro con Google

        // Botón volver
        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }

        // Link para ir al login
        binding.textViewLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.editTextName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()

        if (name.isEmpty()) {
            binding.textInputLayoutName.error = "Ingrese su nombre"
            return false
        }
        if (lastName.isEmpty()) {
            binding.textInputLayoutLastName.error = "Ingrese su apellido"
            return false
        }
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = "Ingrese su correo electrónico"
            return false
        }
        if (password.isEmpty()) {
            binding.textInputLayoutPassword.error = "Ingrese una contraseña"
            return false
        }
        if (password.length < 8) {
            binding.textInputLayoutPassword.error = "La contraseña debe tener al menos 8 caracteres"
            return false
        }
        if (password != confirmPassword) {
            binding.textInputLayoutConfirmPassword.error = "Las contraseñas no coinciden"
            return false
        }
        if (!binding.checkBoxTerms.isChecked) {
            Toast.makeText(this, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(this, "Se ha enviado un correo de verificación a $email", Toast.LENGTH_SHORT).show()
                            saveUserData()
                        } else {
                            Toast.makeText(this, "Error al enviar el correo de verificación: ${verificationTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData() {
        val user = auth.currentUser
        if (user != null) {
            val firstName = binding.editTextName.text.toString().trim()
            val lastName = binding.editTextLastName.text.toString().trim()
            val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${firstName}+${lastName}&background=random&size=200"

            val userData = hashMapOf(
                "name" to firstName,
                "lastName" to lastName,
                "email" to user.email,
                "createdAt" to System.currentTimeMillis(),
                "profileImage" to defaultAvatarUrl
            )

            FirebaseFirestore.getInstance().collection("users")
                .document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }




}
