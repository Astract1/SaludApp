package com.astract.saludapp


data class Source(
    val id: String? = null,
    val name: String = ""
)


data class Noticia(
    val id: Int = 0,
    val source: Source, // Cambiado a Source
    val author: String = "",
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val urlToImage: String = "",
    val publishedAt: String = "",
    val content: String = "",
    val language: String = "es",
    val category: String = "health"
)


data class Articulo(
  val articleId: Int = 0,
    val title: String = "",
    val abstract: String = "",
    val url: String = "",
    val publishedAt: String = "",
    val imagenurl: String = "",
    val author: String = ""
)
