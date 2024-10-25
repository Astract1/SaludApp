import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.astract.saludapp.NotificationReceiver
import com.astract.saludapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Disponibilidad(
    val fecha_inicio: String,
    val fecha_fin: String
)

data class IMCRecomendado(
    val minimo: Double,
    val maximo: Double?
)

data class Reto(
    val titulo: String,
    val descripcion: String,
    val disponibilidad: Disponibilidad,
    val imc_recomendado: IMCRecomendado?,
    var estaUnido: Boolean = false,
    var frecuenciaNotificacion: Int = 0
)

class retosAdapter(
    private val context: Context,
    private val retos: List<Reto>,
    private val onUnirseClick: (Reto) -> Unit,
    private val ultimoIMC: Double
) : RecyclerView.Adapter<retosAdapter.RetoViewHolder>() {

    companion object {
        private const val CHANNEL_ID = "canal_retos"
        private const val CHANNEL_NAME = "Retos de Salud"
        private const val NOTIFICATION_ID = 1

        private const val FRECUENCIA_DIARIA = 0
        private const val FRECUENCIA_DOS_DIAS = 1
        private const val FRECUENCIA_PRUEBA = 2
    }

    class RetoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trofeoIcon: ImageView = view.findViewById(R.id.trofeoIcon)
        val tituloReto: TextView = view.findViewById(R.id.tituloReto)
        val descripcionReto: TextView = view.findViewById(R.id.descripcionReto)
        val unirseButton: Button = view.findViewById(R.id.unirseButton)
        val fechaInicio: TextView = view.findViewById(R.id.fechaInicio)
        val fechaFin: TextView = view.findViewById(R.id.fechaFin)
        val colorBarra: View = view.findViewById(R.id.ColorBarra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_reto, parent, false)
        return RetoViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(holder: RetoViewHolder, position: Int) {
        val reto = retos[position]
        setupViewHolder(holder, reto)
        actualizarEstadoBoton(holder, reto)
    }

    private fun setupViewHolder(holder: RetoViewHolder, reto: Reto) {
        holder.apply {
            tituloReto.text = reto.titulo
            descripcionReto.text = reto.descripcion
            fechaInicio.text = "Fecha Inicio: ${reto.disponibilidad.fecha_inicio}"
            fechaFin.text = "Fecha Fin: ${reto.disponibilidad.fecha_fin}"
            colorBarra.setBackgroundColor(obtenerColorPorIMC())
        }
    }

    private fun obtenerColorPorIMC(): Int = when {
        ultimoIMC < 18.5 -> context.getColor(R.color.blue)
        ultimoIMC in 18.5..24.9 -> context.getColor(R.color.green)
        ultimoIMC in 25.0..29.9 -> context.getColor(R.color.yellow)
        else -> context.getColor(R.color.red)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun actualizarEstadoBoton(holder: RetoViewHolder, reto: Reto) {
        holder.unirseButton.apply {
            isEnabled = !reto.estaUnido
            text = if (reto.estaUnido) "Inscrito" else "Unirse"
            setBackgroundColor(context.getColor(if (reto.estaUnido) R.color.default_cyan else R.color.cyan_book))

            if (!reto.estaUnido) {
                setOnClickListener {
                    manejarClickUnirse(holder, reto)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun manejarClickUnirse(holder: RetoViewHolder, reto: Reto) {
        mostrarDialogoFrecuenciaNotificacion(reto) { frecuenciaSeleccionada ->
            if (frecuenciaSeleccionada != null) {
                holder.unirseButton.setBackgroundColor(context.getColor(R.color.cyan_book))

                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                val fechaInicio = sdf.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val fechaFin = sdf.format(calendar.time)

                actualizarFechas(holder, fechaInicio, fechaFin)

                reto.estaUnido = true
                reto.frecuenciaNotificacion = frecuenciaSeleccionada
                configurarNotificacion(reto)

                holder.unirseButton.apply {
                    text = "Inscrito"
                    isEnabled = false
                }

                Toast.makeText(context, "Te has unido al reto: ${reto.titulo}", Toast.LENGTH_SHORT).show()
                onUnirseClick(reto)
            }
        }
    }

    private fun actualizarFechas(holder: RetoViewHolder, fechaInicio: String, fechaFin: String) {
        holder.fechaInicio.text = "Fecha Inicio: $fechaInicio"
        holder.fechaFin.text = "Fecha Fin: $fechaFin"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun mostrarDialogoFrecuenciaNotificacion(reto: Reto, callback: (Int?) -> Unit) {
        val opciones = arrayOf("Diariamente", "Cada 2 días", "Prueba (cada minuto)")

        AlertDialog.Builder(context)
            .setTitle("Selecciona la frecuencia de las notificaciones")
            .setItems(opciones) { dialog, which ->
                callback(which)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                callback(null)
                dialog.dismiss()
            }
            .setOnCancelListener {
                callback(null)
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun configurarNotificacion(reto: Reto) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        crearCanalNotificacion(notificationManager)

        val pendingIntent = crearPendingIntent(reto)
        programarNotificaciones(reto, pendingIntent)

        mostrarNotificacionInicial(notificationManager, reto)
    }

    private fun crearCanalNotificacion(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(canal)
        }
    }

    private fun crearPendingIntent(reto: Reto): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("retoTitulo", reto.titulo)
        }

        return PendingIntent.getBroadcast(
            context,
            reto.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun programarNotificaciones(reto: Reto, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when (reto.frecuenciaNotificacion) {
            FRECUENCIA_DIARIA -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 60000,
                    60000,
                    pendingIntent
                )
            }
            FRECUENCIA_DOS_DIAS -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 120000,
                    120000,
                    pendingIntent
                )
            }
            FRECUENCIA_PRUEBA -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mostrarNotificacion(notificationManager, reto)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 15000,
                        pendingIntent
                    )
                }
            }
        }
    }

    private fun mostrarNotificacion(notificationManager: NotificationManager, reto: Reto) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Recordatorio de Reto")
            .setContentText("No olvides trabajar en tu reto: ${reto.titulo}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun mostrarNotificacionInicial(notificationManager: NotificationManager, reto: Reto) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("¡Te has unido a un nuevo reto!")
            .setContentText("Comenzaste el reto: ${reto.titulo}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun getItemCount(): Int = retos.size
}
