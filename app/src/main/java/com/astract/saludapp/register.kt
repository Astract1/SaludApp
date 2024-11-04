package com.astract.saludapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.astract.saludapp.databinding.ActivityRegisterBinding

class register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    // Variables para el seguimiento de requisitos de contraseña
    private var hasMinLength = false
    private var hasUpperCase = false
    private var hasLowerCase = false
    private var hasNumber = false
    private var hasSpecialChar = false

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

        setupPasswordValidation()
        setupClickListeners()
        updatePasswordHelperText()
    }

    private fun setupPasswordValidation() {
        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()

                // Actualizar estados
                hasMinLength = password.length >= 8
                hasUpperCase = password.contains(Regex("[A-Z]"))
                hasLowerCase = password.contains(Regex("[a-z]"))
                hasNumber = password.contains(Regex("[0-9]"))
                hasSpecialChar = password.contains(Regex("[!@#\$%^&*(),.?\":{}|<>]"))

                updatePasswordHelperText()
            }
        })
    }

    private fun updatePasswordHelperText() {
        val helperText = SpannableStringBuilder()

        // Función auxiliar para agregar requisitos con color
        fun addRequirement(text: String, isMet: Boolean) {
            val color = if (isMet) {
                ContextCompat.getColor(this, R.color.green)
            } else {
                Color.GRAY
            }

            val start = helperText.length
            helperText.append("• $text\n")
            helperText.setSpan(
                ForegroundColorSpan(color),
                start,
                helperText.length,
                0
            )
        }

        addRequirement("Mínimo 8 caracteres", hasMinLength)
        addRequirement("Al menos una mayúscula", hasUpperCase)
        addRequirement("Al menos una minúscula", hasLowerCase)
        addRequirement("Al menos un número", hasNumber)
        addRequirement("Al menos un carácter especial", hasSpecialChar)

        binding.textInputLayoutPassword.helperText = helperText
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }

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

        // Validación del nombre
        if (name.isEmpty()) {
            binding.textInputLayoutName.error = "Ingrese su nombre"
            return false
        }
        binding.textInputLayoutName.error = null

        // Validación del apellido
        if (lastName.isEmpty()) {
            binding.textInputLayoutLastName.error = "Ingrese su apellido"
            return false
        }
        binding.textInputLayoutLastName.error = null

        // Validación del email
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = "Ingrese su correo electrónico"
            return false
        }
        if (!isValidEmail(email)) {
            binding.textInputLayoutEmail.error = "Ingrese un correo electrónico válido"
            return false
        }
        binding.textInputLayoutEmail.error = null

        // Validación completa de la contraseña
        if (!hasMinLength || !hasUpperCase || !hasLowerCase || !hasNumber || !hasSpecialChar) {
            binding.textInputLayoutPassword.error = "La contraseña no cumple con todos los requisitos"
            return false
        }
        binding.textInputLayoutPassword.error = null

        // Validación de confirmación de contraseña
        if (password != confirmPassword) {
            binding.textInputLayoutConfirmPassword.error = "Las contraseñas no coinciden"
            return false
        }
        binding.textInputLayoutConfirmPassword.error = null

        // Validación de términos y condiciones
        if (!binding.checkBoxTerms.isChecked) {
            Toast.makeText(this, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
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
