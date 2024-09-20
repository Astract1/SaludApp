package com.astract.saludapp.models

data class NoticiaEntity(
    val id: Int = 0,
    val source: String,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String,
    val language: String,
    val category: String
)
