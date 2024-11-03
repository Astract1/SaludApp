    package com.astract.saludapp

    import android.app.AlarmManager
    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.app.PendingIntent
    import android.content.Context
    import android.content.Intent
    import android.os.Build
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.view.animation.AnimationUtils
    import android.widget.Button
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.annotation.RequiresApi
    import androidx.appcompat.app.AlertDialog
    import androidx.core.app.NotificationCompat
    import androidx.recyclerview.widget.RecyclerView
    import com.google.firebase.firestore.FirebaseFirestore
    import java.text.SimpleDateFormat
    import java.util.*

    class retosAdapter(
        private val context: Context,
        private val retos: List<Reto>,
        private val onUnirseClick: (Reto, Int) -> Unit,
        private val ultimoIMC: Double,
        private val userId: String
    ) : RecyclerView.Adapter<retosAdapter.RetoViewHolder>() {

        private val db = FirebaseFirestore.getInstance()

        companion object {
            private const val CHANNEL_ID = "canal_retos"
            private const val CHANNEL_NAME = "Retos de Salud"
            private const val NOTIFICATION_ID = 1

            private const val FRECUENCIA_DIARIA = 0
            private const val FRECUENCIA_Dos = 1
            private const val FRECUENCIA_Tres = 2
        }

        inner class RetoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val trofeoIcon: ImageView = view.findViewById(R.id.trofeoIcon)
            val tituloReto: TextView = view.findViewById(R.id.tituloReto)
            val descripcionReto: TextView = view.findViewById(R.id.descripcionReto)
            val unirseButton: Button = view.findViewById(R.id.unirseButton)
            val fechaInicio: TextView = view.findViewById(R.id.fechaInicio)
            val fechaFin: TextView = view.findViewById(R.id.fechaFin)
            val colorBarra: View = view.findViewById(R.id.ColorBarra)
            val mensajeIMC: TextView = view.findViewById(R.id.mensajeIMC)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetoViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_reto, parent, false)
            return RetoViewHolder(view)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onBindViewHolder(holder: RetoViewHolder, position: Int) {
            val reto = retos[position]
            setupViewHolder(holder, reto)
            verificarInscripcion(reto, holder)

            // Verificar IMC si es necesario
            if (reto.imc_recomendado != null && ultimoIMC == 0.0) {
                holder.mensajeIMC.visibility = View.VISIBLE
                holder.unirseButton.isEnabled = false
            } else {
                holder.mensajeIMC.visibility = View.GONE
                holder.unirseButton.isEnabled = true
            }

            // Animar la aparición del item
            animateView(holder.itemView, position)
        }

        private fun setupViewHolder(holder: RetoViewHolder, reto: Reto) {
            holder.apply {
                tituloReto.text = reto.titulo
                descripcionReto.text = reto.descripcion
                colorBarra.setBackgroundColor(obtenerColorPorIMC())
                unirseButton.text = "Unirse"

                unirseButton.setOnClickListener {
                    mostrarDialogoFrecuencia(reto, holder)
                }
            }
        }

        private fun calcularFechas(): Pair<String, String> {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()

            // Fecha de inicio (hoy)
            val fechaInicio = formatoFecha.format(calendar.time)

            // Fecha de fin (7 días después)
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val fechaFin = formatoFecha.format(calendar.time)

            return Pair(fechaInicio, fechaFin)
        }

        private fun actualizarFechasView(holder: RetoViewHolder, fechaInicio: String, fechaFin: String) {
            holder.fechaInicio.text = "Inicio: $fechaInicio"
            holder.fechaFin.text = "Fin: $fechaFin"
        }

        private fun obtenerColorPorIMC(): Int = when {
            ultimoIMC == 0.0 -> context.getColor(R.color.black)  // Sin IMC registrado
            ultimoIMC < 18.5 -> context.getColor(R.color.blue)  // Bajo peso
            ultimoIMC in 18.5..24.9 -> context.getColor(R.color.green)  // Normal
            ultimoIMC in 25.0..29.9 -> context.getColor(R.color.yellow) // Sobrepeso
            else -> context.getColor(R.color.red)  // Obesidad
        }

        private fun verificarInscripcion(reto: Reto, holder: RetoViewHolder) {
            db.collection("users")
                .document(userId)
                .collection("retos_inscritos")
                .whereEqualTo("retoId", reto.id)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val documento = documents.documents[0]
                        actualizarBotonInscrito(holder.unirseButton)
                        actualizarFechasView(
                            holder,
                            documento.getString("fechaInicio") ?: "",
                            documento.getString("fechaFin") ?: ""
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val frecuencia = documento.getLong("frecuenciaNotificacion")?.toInt() ?: 0
                            configurarNotificacion(reto, frecuencia, false)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al verificar inscripción: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun actualizarBotonInscrito(button: Button) {
            button.apply {
                isEnabled = false
                text = "Inscrito"
                setBackgroundColor(context.getColor(R.color.guarado_cyan))
            }
        }

        private fun guardarInscripcionEnFirebase(reto: Reto, frecuencia: Int, fechas: Pair<String, String>) {
            val inscripcion = hashMapOf(
                "retoId" to reto.id,
                "titulo" to reto.titulo,
                "fechaInscripcion" to fechas.first,
                "frecuenciaNotificacion" to frecuencia,
                "estado" to "activo",
                "fechaInicio" to fechas.first,
                "fechaFin" to fechas.second
            )

            db.collection("users")
                .document(userId)
                .collection("retos_inscritos")
                .add(inscripcion)
                .addOnSuccessListener {
                    Toast.makeText(context, "¡Te has inscrito exitosamente!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al inscribirse: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun mostrarDialogoFrecuencia(reto: Reto, holder: RetoViewHolder) {
            val fechas = calcularFechas()

            val frecuencias = arrayOf("Diaria", "Cada dos dias", "Cada tres dias")
            AlertDialog.Builder(context)
                .setTitle("Selecciona la frecuencia de notificaciones")
                .setItems(frecuencias) { _, which ->
                    animateButton(holder.unirseButton)

                    holder.unirseButton.postDelayed({
                        onUnirseClick(reto, which)
                        actualizarBotonInscrito(holder.unirseButton)
                        actualizarFechasView(holder, fechas.first, fechas.second)
                        guardarInscripcionEnFirebase(reto, which, fechas)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            configurarNotificacion(reto, which, true)
                        }
                    }, 300)
                }
                .show()
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun configurarNotificacion(reto: Reto, frecuencia: Int, mostrarNotificacionInicial: Boolean) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            crearCanalNotificacion(notificationManager)

            val pendingIntent = crearPendingIntent(reto)
            programarNotificaciones(frecuencia, pendingIntent)

            if (mostrarNotificacionInicial) {
                mostrarNotificacionInicial(notificationManager, reto)
            }
        }

        private fun crearCanalNotificacion(notificationManager: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val canal = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Canal para notificaciones de retos de salud"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(canal)
            }
        }

        private fun crearPendingIntent(reto: Reto): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("retoTitulo", reto.titulo)
                putExtra("retoId", reto.id)
            }

            return PendingIntent.getBroadcast(
                context,
                reto.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun programarNotificaciones(frecuencia: Int, pendingIntent: PendingIntent) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intervalo = when (frecuencia) {
                FRECUENCIA_DIARIA -> AlarmManager.INTERVAL_DAY
                FRECUENCIA_Dos -> AlarmManager.INTERVAL_DAY * 2
                FRECUENCIA_Tres -> AlarmManager.INTERVAL_DAY * 3
                else -> AlarmManager.INTERVAL_DAY
            }

            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + intervalo,
                        intervalo,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    Toast.makeText(
                        context,
                        "No se pudieron programar las notificaciones. Verifica los permisos.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Tu dispositivo no permite programar notificaciones exactas",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        private fun mostrarNotificacionInicial(notificationManager: NotificationManager, reto: Reto) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("¡Te has unido a un nuevo reto!")
                .setContentText("Comenzaste el reto: ${reto.titulo}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(reto.hashCode(), notification)
        }

        private fun animateView(view: View, position: Int) {
            view.alpha = 0f
            view.translationY = 100f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(position * 100L)
                .start()
        }

        private fun animateButton(button: Button) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.button_animation)
            button.startAnimation(animation)
        }

        override fun getItemCount() = retos.size
    }