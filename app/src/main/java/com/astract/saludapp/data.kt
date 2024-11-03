package com.astract.saludapp


data class Source(
    val id: String? = null,
    val name: String = ""
)


data class Noticia(
    val id: Int = 0,  // Es recomendable que el ID sea un String si se va a utilizar el ID de documento de Firestore
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
    val fecha: String,
    val peso: Double,
    val altura: Double,
    val resultadoIMC: Double
)



data class SelloNegro(
    val id: Int,
    val nombre: String,
    val resumen: String,
    val caracteristicas: List<String>,
    val detalles: String,
    val recomendaciones: String,
    val ejemplos: List<String>,
    val imagen_url: String
)

data class MetaIMCData(
    val fecha: String,
    val metaIMC: Double

)

data class Habito(
    val nombre: String,
    var completado: Boolean
)


data class SellosResponse(val sellos: List<SelloNegro>)



