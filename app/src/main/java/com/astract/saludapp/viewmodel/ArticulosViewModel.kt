package com.astract.saludapp.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astract.saludapp.Articulo
import com.astract.saludapp.database.MyDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ArticuloViewModel(private val dbHelper: MyDatabaseHelper) : ViewModel() {

    private val _articulos = MutableLiveData<List<Articulo>>()
    private val _articulo = MutableLiveData<Articulo?>()
    val articulos: LiveData<List<Articulo>> get() = _articulos

    private val apiKey = "utwTdMiTaapWmG2FPffXrdtiA2lVyqBX"
    private val apiUrl = "https://api.nytimes.com/svc/search/v2/articlesearch.json?fq=section_name:(\"Health\")%20AND%20body:(\"Fitness\")&api-key=$apiKey"

    fun fetchAndUpdateArticulos(context: Context) {
        viewModelScope.launch {
            try {
                val jsonResponse = fetchArticlesFromApi()
                jsonResponse?.let {
                    parseArticles(it)
                    loadArticulos() // Cargar los artículos desde la base de datos
                } ?: Log.e("ArticuloViewModel", "No articles found in response")
            } catch (e: Exception) {
                Log.e("ArticuloViewModel", "Error fetching articles: ${e.message}")
                showToast(context, "Error al obtener artículos: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun fetchArticlesFromApi(): String? = withContext(Dispatchers.IO) {
        val urlConnection = URL(apiUrl).openConnection() as HttpURLConnection
        return@withContext try {
            urlConnection.requestMethod = "GET"
            urlConnection.connect()
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                urlConnection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e("ArticuloViewModel", "Error response code: ${urlConnection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e("ArticuloViewModel", "Error fetching articles: ${e.message}")
            null
        } finally {
            urlConnection.disconnect()
        }
    }

    private fun parseArticles(jsonResponse: String) {
        val articles = mutableListOf<Articulo>()
        val jsonObject = JSONObject(jsonResponse)
        val response = jsonObject.getJSONObject("response")
        val docsArray = response.getJSONArray("docs")

        for (i in 0 until docsArray.length()) {
            val articleJson = docsArray.getJSONObject(i)
            val title = articleJson.getJSONObject("headline").getString("main")
            val abstract = articleJson.optString("abstract") ?: ""
            val url = articleJson.getString("web_url")
            val publishedDate = articleJson.getString("pub_date")

            // Extraer imagen (si existe)
            var imageUrl: String? = null
            val multimediaArray = articleJson.optJSONArray("multimedia")
            if (multimediaArray != null && multimediaArray.length() > 0) {
                for (j in 0 until multimediaArray.length()) {
                    val multimediaJson = multimediaArray.getJSONObject(j)
                    if (multimediaJson.getString("subtype") == "xlarge") {
                        imageUrl = "https://www.nytimes.com/" + multimediaJson.getString("url")
                        break
                    }
                }
            }

            // Crear instancia de Articulo
            val articulo = Articulo(
                title = title,
                abstract = abstract,
                url = url,
                publishedAt = publishedDate,
                imagenurl = imageUrl ?: "",
                author = articleJson.optString("author") ?: ""
            )

            articles.add(articulo)

            // Guardar en la base de datos
            dbHelper.insertOrUpdateArticulo(articulo)
        }

        _articulos.postValue(articles)
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun loadArticulos() {
        viewModelScope.launch {
            try {
                val listaDeArticulos = withContext(Dispatchers.IO) {
                    dbHelper.getAllArticulos()
                }
                _articulos.postValue(listaDeArticulos)
            } catch (e: Exception) {
                Log.e("Error", "Error al cargar los artículos", e)
            }
        }
    }

}


