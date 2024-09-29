package com.astract.saludapp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.astract.saludapp.database.MyDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class imc : Fragment() {
    private lateinit var pesoEditText: TextInputEditText
    private lateinit var alturaEditText: TextInputEditText
    private lateinit var valorPesoTextView: TextView
    private lateinit var calcularButton: MaterialButton
    private lateinit var iconArrow: ImageView
    private lateinit var rootView: View
    private lateinit var dbHelper: MyDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        rootView = inflater.inflate(R.layout.fragment_imc, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el dbHelper aquí
        dbHelper = MyDatabaseHelper(requireContext())

        val fechaTextView: TextView = view.findViewById(R.id.fecha2)
        val fechaActual = obtenerFechaActual()
        fechaTextView.text = fechaActual

        // Inicializar las vistas
        pesoEditText = view.findViewById(R.id.peso)
        alturaEditText = view.findViewById(R.id.altura)
        valorPesoTextView = view.findViewById(R.id.valor_peso)
        calcularButton = view.findViewById(R.id.calcular_peso)
        iconArrow = view.findViewById(R.id.icon_arrow)

        calcularButton.setOnClickListener {
            calcularIMC()
        }

        iconArrow.setOnClickListener {
            // Cargar la animación de scroll desde XML
            val scrollAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scroll_up)

            // Aplicar la animación a la vista raíz del fragmento
            rootView.startAnimation(scrollAnimation)

            // Listener para saber cuándo termina la animación
            scrollAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // Opcional: Acciones al inicio de la animación
                }

                override fun onAnimationEnd(animation: Animation?) {
                    val action = imcDirections.actionImcToHistorialImc()
                    findNavController().navigate(action) // Navegar usando el NavController
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    // No se requiere ninguna acción aquí
                }
            })
        }
    }

    private fun calcularIMC() {
        val peso = pesoEditText.text.toString().toDoubleOrNull()
        val altura = alturaEditText.text.toString().toDoubleOrNull()



        // Validar entrada de altura
        if (altura == null) {
            alturaEditText.error = "La altura no puede estar vacía"
            return
        }


        if (peso == null) {
            pesoEditText.error = "El peso no puede estar vacío"
            return
        }


        if (peso != null && altura != null) {
            val alturaMetros = altura / 100  // Convertir altura a metros
            val imc = peso / (alturaMetros * alturaMetros)

            valorPesoTextView.text = String.format("IMC: %.2f", imc)

            // Cambiar color y texto basado en el IMC
            when {
                imc < 18.5 -> {
                    valorPesoTextView.setTextColor(Color.BLUE)
                    valorPesoTextView.text = "Bajo peso\nIMC: %.2f".format(imc)
                }

                imc in 18.5..24.9 -> {
                    valorPesoTextView.setTextColor(Color.GREEN)
                    valorPesoTextView.text = "Normal\nIMC: %.2f".format(imc)
                }

                imc in 25.0..29.9 -> {
                    valorPesoTextView.setTextColor(Color.YELLOW)
                    valorPesoTextView.text = "Sobrepeso\nIMC: %.2f".format(imc)
                }

                else -> {
                    valorPesoTextView.setTextColor(Color.RED)
                    valorPesoTextView.text = "Obesidad\nIMC: %.2f".format(imc)
                }
            }

            // Diálogo de confirmación para guardar en el historial
            mostrarDialogoConfirmacion(imc, peso, altura)
        }
    }

    private fun mostrarDialogoConfirmacion(imc: Double, peso: Double, altura: Double) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Guardar en Historial")
        builder.setMessage("¿Deseas guardar este IMC de ${"%.2f".format(imc)} con Peso: $peso kg y Altura: $altura cm en tu historial?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            guardarEnHistorial(imc, peso, altura)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun guardarEnHistorial(imc: Double, peso: Double, altura: Double) {
        // Obtener la fecha actual
        val fechaActual = obtenerFechaActual()

        // Llamar a la función insertHistorialIMC
        dbHelper.insertHistorialIMC(imc, fechaActual, peso, altura)

        // Mostrar un Toast como confirmación
        Toast.makeText(
            requireContext(),
            "IMC guardado en historial: IMC: ${"%.2f".format(imc)}, Peso: $peso kg, Altura: $altura cm",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun obtenerFechaActual(): String {
        val locale = Locale("es", "ES")
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", locale)
        val fecha = Date()
        val fechaFormateada = formatoFecha.format(fecha)

        return fechaFormateada.replaceFirstChar { it.titlecase(locale) }
    }
}