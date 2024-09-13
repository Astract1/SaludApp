package com.astract.saludapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class imc : Fragment() {
    private lateinit var pesoEditText: TextInputEditText
    private lateinit var alturaEditText: TextInputEditText
    private lateinit var valorPesoTextView: TextView
    private lateinit var calcularButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_imc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fechaTextView: TextView = view.findViewById(R.id.fecha2)
        val fechaActual = obtenerFechaActual()
        fechaTextView.text = fechaActual

        // Initialize views
        pesoEditText = view.findViewById(R.id.peso)
        alturaEditText = view.findViewById(R.id.altura)
        valorPesoTextView = view.findViewById(R.id.valor_peso)
        calcularButton = view.findViewById(R.id.calcular_peso)

        calcularButton.setOnClickListener {
            calcularIMC()
        }
    }

    private fun calcularIMC() {
        val peso = pesoEditText.text.toString().toDoubleOrNull()
        val altura = alturaEditText.text.toString().toDoubleOrNull()

        if (peso != null && altura != null) {
            val alturaMetros = altura / 100  // Convertir altura a metros
            val imc = peso / (alturaMetros * alturaMetros)

            // Actualizar el TextView con el valor del IMC
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
        }
    }

    private fun obtenerFechaActual(): String {
        val locale = Locale("es", "ES")
        val formatoFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", locale)
        val fecha = Date()
        val fechaFormateada = formatoFecha.format(fecha)

        return fechaFormateada.replaceFirstChar { it.titlecase(locale) }
    }
}
