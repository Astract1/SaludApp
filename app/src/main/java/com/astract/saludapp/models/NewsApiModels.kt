package com.astract.saludapp.models

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("status") val status: String, // Estado de la respuesta (ej. "ok" o "error")
    @SerializedName("totalResults") val totalResults: Int, // Total de resultados encontrados
    @SerializedName("articles") val articles: List<Article> // Lista de artículos
)

data class Article(
    @SerializedName("source") val source: Source, // Fuente de la noticia
    @SerializedName("author") val author: String?, // Autor de la noticia (puede ser nulo)
    @SerializedName("title") val title: String, // Título del artículo
    @SerializedName("description") val description: String?, // Descripción breve del artículo
    @SerializedName("url") val url: String, // URL del artículo
    @SerializedName("urlToImage") val urlToImage: String?, // URL de la imagen (puede ser nulo)
    @SerializedName("publishedAt") val publishedAt: String, // Fecha de publicación
    @SerializedName("content") val content: String? // Contenido del artículo (puede ser nulo)
)

data class Source(
    @SerializedName("id") val id: String?, // ID de la fuente (puede ser nulo)
    @SerializedName("name") val name: String // Nombre de la fuente
)
