package com.astract.saludapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class Historialimc : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_historialimc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val volverTextView: View = view.findViewById(R.id.ver_historial_imc)
        val iconArrow: View = view.findViewById(R.id.icon_arrow)

        // Click listener para volver a la calculadora IMC
        volverTextView.setOnClickListener {
            iniciarAnimacionDeScroll(view)
        }

        iconArrow.setOnClickListener {
            iniciarAnimacionDeScroll(view)
        }
    }

    private fun iniciarAnimacionDeScroll(rootView: View) {
        // Cargar la animación de desplazamiento hacia abajo desde XML
        val scrollAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)

        // Aplicar la animación a la vista raíz del fragmento
        rootView.startAnimation(scrollAnimation)

        // Listener para saber cuándo termina la animación
        scrollAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Acciones opcionales al inicio de la animación
            }

            override fun onAnimationEnd(animation: Animation?) {
                val action = imcDirections.actionGlobalImc()
                findNavController().navigate(action) // Navegar usando el NavController
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // No se requiere ninguna acción aquí
            }
        })
    }
}
