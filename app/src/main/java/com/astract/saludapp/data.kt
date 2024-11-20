package com.astract.saludapp

import java.util.UUID

data class Source(
    val id: String? = null,
    val name: String = ""
)

data class Noticia(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val url: String = "",
    val urlToImage: String = "",
    val publishedAt: String = "",
    val language: String = "es"
) {
    // Constructor sin argumentos necesario para Firestore
    constructor() : this(0, "", "", "", "", "", "", "es")
}

data class Articulo(
    val articleId: Int = 0,
    val title: String = "",
    val abstract: String = "",
    val url: String = "",
    val publishedAt: String = "",
    val imagenurl: String = "",
    val author: String = ""
)

data class HistorialIMCData(
    val id: String,
    val peso: Double,
    val altura: Double,
    val resultadoIMC: Double,
    val timestamp: Any,  // Cambiado a Any para manejar Timestamp de Firebase
    val fecha: String
)


data class SelloNegro(
    val id: Int = 0,
    val nombre: String = "",
    val resumen: String = "",
    val caracteristicas: List<String> = listOf(),
    val detalles: String = "",
    val recomendaciones: String = "",
    val ejemplos: List<String> = listOf(),
    val imagen_url: String = ""
) {
    constructor() : this(0, "", "", listOf(), "", "", listOf(), "")
}


data class MetaIMCData(
    val fecha: String,
    val metaIMC: Double
)

data class Habito(
    val nombre: String,
    var completado: Boolean,
    var tiempo: String = "",
    var frecuencia: String = ""
)
data class Disponibilidad(
    val fecha_inicio: String = "",  // Valor por defecto
    val fecha_fin: String = ""      // Valor por defecto
) {
    // Constructor sin argumentos necesario para Firestore
    constructor() : this("", "")
}

data class IMCRecomendado(
    val minimo: Double = 0.0,  // Valor por defecto
    val maximo: Double? = null  // Valor por defecto
) {
    // Constructor sin argumentos necesario para Firestore
    constructor() : this(0.0, null)
}

data class Reto(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String = "",
    val descripcion: String = "",
    val disponibilidad: Disponibilidad = Disponibilidad(),
    val imc_recomendado: IMCRecomendado? = null,
    var estaUnido: Boolean = false
) {
    // Constructor sin argumentos necesario para Firestore
    constructor() : this(
        UUID.randomUUID().toString(),
        "",
        "",
        Disponibilidad(),
        null,
        false
    )
}
data class RetoUsuario(
    val retoId: String = "",
    val userId: String = "",
    val titulo: String = "",
    val fechaInscripcion: String = "",
    val estado: String = "activo",
    val frecuenciaNotificacion: Int = 0
)

data class SellosResponse(val sellos: List<SelloNegro>)