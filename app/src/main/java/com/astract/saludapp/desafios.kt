package com.astract.saludapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class desafios : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: retosAdapter
    private val retosList = mutableListOf<Reto>()
    private lateinit var noRetosMessage: TextView
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios, container, false)
        inicializarVistas(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = sharedViewModel.getUserId()
        if (userId == null) {
            mostrarMensaje("No se pudo identificar al usuario")
            return
        }
        cargarDatos()
    }

    private fun inicializarVistas(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        noRetosMessage = view.findViewById(R.id.noRetosMessage)
    }

    private fun cargarDatos() {
        cargarUltimoIMC { ultimoIMC ->
            if (ultimoIMC <= 0.0) {
                mostrarMensaje("No hay IMC asignado. Completa tu perfil para acceder a retos.")
            } else {
                cargarRetos(ultimoIMC)
            }
        }
    }

    private fun cargarUltimoIMC(callback: (Double) -> Unit) {
        userId?.let { uid ->
            Log.d("Desafios", "Intentando cargar IMC para usuario: $uid")

            // Primero verificamos si hay un IMC en el documento principal del usuario
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val imcDirecto = documentSnapshot.getDouble("imc")
                    if (imcDirecto != null) {
                        Log.d("Desafios", "IMC encontrado directamente: $imcDirecto")
                        callback(imcDirecto)
                    } else {
                        // Si no hay IMC directo, buscamos en el historial
                        db.collection("users")
                            .document(uid)
                            .collection("historial_imc")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    val ultimoIMC = documents.documents[0].getDouble("imc") ?: 0.0
                                    Log.d("Desafios", "IMC encontrado en historial: $ultimoIMC")
                                    callback(ultimoIMC)
                                } else {
                                    Log.d("Desafios", "No se encontró IMC en el historial")
                                    callback(0.0)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Desafios", "Error al cargar historial IMC: ${e.message}")
                                callback(0.0)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Desafios", "Error al cargar documento de usuario: ${e.message}")
                    callback(0.0)
                }
        } ?: run {
            Log.e("Desafios", "userId es null")
            callback(0.0)
        }
    }


    private fun cargarRetos(ultimoIMC: Double) {
        db.collection("retos")
            .get()
            .addOnSuccessListener { documents ->
                retosList.clear()
                for (document in documents) {
                    val reto = document.toObject(Reto::class.java)
                    if (cumpleRequisitosIMC(reto.imc_recomendado, ultimoIMC)) {
                        retosList.add(reto)
                    }
                }
                actualizarUI(ultimoIMC)
            }
            .addOnFailureListener {
                mostrarMensaje("Error al cargar los retos")
            }
    }

    private fun cumpleRequisitosIMC(imcRecomendado: IMCRecomendado?, imc: Double): Boolean {
        if (imcRecomendado == null) return true
        return imc >= imcRecomendado.minimo &&
                (imcRecomendado.maximo == null || imc <= imcRecomendado.maximo)
    }

    private fun actualizarUI(ultimoIMC: Double) {
        if (retosList.isEmpty()) {
            mostrarMensaje("No hay retos disponibles para tu IMC actual")
            return
        }

        userId?.let { uid ->
            adapter = retosAdapter(
                context = requireContext(),
                retos = retosList,
                onUnirseClick = { _, _ ->
                },
                ultimoIMC = ultimoIMC,
                userId = uid
            )
            recyclerView.adapter = adapter
            noRetosMessage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }



    private fun mostrarMensaje(mensaje: String) {
        noRetosMessage.text = mensaje
        noRetosMessage.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
}
