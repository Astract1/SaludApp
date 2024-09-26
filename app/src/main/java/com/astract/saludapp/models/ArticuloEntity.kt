package com.astract.saludapp.models

data class ArticuloEntity(
    val id: Int = 0,
    val title: String,
    val abstract: String,
    val url: String,
    val publishedAt: String,
    val imagenurl: String
)