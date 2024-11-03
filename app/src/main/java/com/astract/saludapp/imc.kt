package com.astract.saludapp

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var botonGuardarMeta: MaterialButton
    private lateinit var valorMetaTextView: TextView
    private lateinit var valorMeta: TextInputEditText

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var metaIMC: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_imc, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = sharedViewModel.getUserId()

        // Inicializamos los elementos de la vista
        initializeViews(view)

        // Cargar último IMC y meta del usuario
        cargarUltimoIMC()
        cargarMetaIMC()

        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        val fechaTextView: TextView = view.findViewById(R.id.fecha2)
        fechaTextView.text = obtenerFechaActual()

        pesoEditText = view.findViewById(R.id.peso)
        alturaEditText = view.findViewById(R.id.altura)
        valorPesoTextView = view.findViewById(R.id.valor_peso)
        calcularButton = view.findViewById(R.id.calcular_peso)
        iconArrow = view.findViewById(R.id.icon_arrow)
        valorMetaTextView = view.findViewById(R.id.valor_meta)
        botonGuardarMeta = view.findViewById(R.id.btnGuardarMetaIMC)
        valorMeta = view.findViewById(R.id.inputMetaIMC)
    }

    private fun setupClickListeners() {
        calcularButton.setOnClickListener {
            calcularIMC()
        }

        botonGuardarMeta.setOnClickListener {
            guardarMetaIMC()
        }

        val navigateToHistorial = {
            val scrollAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scroll_up)
            rootView.startAnimation(scrollAnimation)

            scrollAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    val action = imcDirections.actionImcToHistorialImc()
                    findNavController().navigate(action)
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        // Asignar el mismo listener tanto al TextView como a la flecha
        iconArrow.setOnClickListener { navigateToHistorial() }
        view?.findViewById<TextView>(R.id.ver_historial_imc)?.setOnClickListener { navigateToHistorial() }
    }

    private fun cargarUltimoIMC() {
        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("historial_imc")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val ultimoIMC = documents.documents[0].getDouble("imc") ?: 0.0
                        actualizarVistaIMC(ultimoIMC)
                    } else {
                        valorPesoTextView.text = "IMC: 0.0"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al cargar el último IMC", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarMetaIMC() {
        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val meta = document.getDouble("meta_imc")
                        if (meta != null) {
                            metaIMC = meta
                            valorMetaTextView.text = String.format("Meta IMC: %.2f", meta)
                        }
                    }
                }
        }
    }

    private fun actualizarVistaIMC(imc: Double) {
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

    private fun calcularIMC() {
        val peso = pesoEditText.text.toString().toDoubleOrNull()
        val altura = alturaEditText.text.toString().toDoubleOrNull()

        if (altura == null) {
            alturaEditText.error = "La altura no puede estar vacía"
            return
        }

        if (peso == null) {
            pesoEditText.error = "El peso no puede estar vacío"
            return
        }

        val alturaMetros = altura / 100
        val imc = peso / (alturaMetros * alturaMetros)
        valorPesoTextView.text = String.format("IMC: %.2f", imc)

        actualizarVistaIMC(imc)
        mostrarDialogoConfirmacion(imc, peso, altura)
    }

    private fun mostrarDialogoConfirmacion(imc: Double, peso: Double, altura: Double) {
        val pesoIdeal = calcularPesoIdeal(altura)
        val diferencia = peso - pesoIdeal

        val mensaje = when {
            diferencia > 0 -> "Necesitas bajar ${"%.2f".format(diferencia)} kg para alcanzar tu peso ideal de ${
                "%.2f".format(pesoIdeal)
            } kg."
            diferencia < 0 -> "Estás por debajo de tu peso ideal de ${"%.2f".format(pesoIdeal)} kg."
            else -> "¡Estás en tu peso ideal!"
        }

        val categoriaIMC = when {
            imc < 18.5 -> "Bajo peso"
            imc in 18.5..24.9 -> "Normal"
            imc in 25.0..29.9 -> "Sobrepeso"
            else -> "Obesidad"
        }

        val imcTexto =
            "¿Deseas guardar este IMC de ${"%.2f".format(imc)} (IMC Ideal: ${"%.2f".format(22.0)}) con Peso: $peso kg y Altura: $altura cm en tu historial?\n\nCategoría: $categoriaIMC\n$mensaje"

        val spannableString = SpannableString(imcTexto)
        val start = imcTexto.indexOf("IMC Ideal:") + "IMC Ideal: ".length
        val end = start + "22.00".length
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Guardar en Historial")
        builder.setMessage(spannableString)
        builder.setPositiveButton("Sí") { dialog, _ ->
            guardarEnHistorial(imc, peso, altura)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun calcularPesoIdeal(altura: Double): Double {
        val alturaMetros = altura / 100
        val imcIdeal = 22.0
        return imcIdeal * (alturaMetros * alturaMetros)
    }

    private fun guardarEnHistorial(imc: Double, peso: Double, altura: Double) {
        userId?.let { uid ->
            val fechaActual = obtenerFechaActual()
            val registroIMC = hashMapOf(
                "imc" to imc,
                "fecha" to fechaActual,
                "peso" to peso,
                "altura" to altura,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            db.collection("users")
                .document(uid)
                .collection("historial_imc")
                .add(registroIMC)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "IMC guardado en historial: IMC: ${"%.2f".format(imc)}, Peso: $peso kg, Altura: $altura cm",
                        Toast.LENGTH_LONG
                    ).show()
                    actualizarVistaIMC(imc)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar el IMC: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun guardarMetaIMC() {
        val metaIMCInput = valorMeta.text.toString().toDoubleOrNull()

        if (metaIMCInput == null) {
            valorMeta.error = "La meta no puede estar vacía"
            return
        }

        userId?.let { uid ->
            db.collection("users")
                .document(uid)
                .update("meta_imc", metaIMCInput)
                .addOnSuccessListener {
                    metaIMC = metaIMCInput
                    valorMetaTextView.text = String.format("Meta IMC: %.2f", metaIMC)
                    Toast.makeText(requireContext(), "Meta de IMC guardada: $metaIMC", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al guardar la meta: ${e.message}", Toast.LENGTH_LONG).show()
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