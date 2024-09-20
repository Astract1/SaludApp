package com.astract.saludapp.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astract.saludapp.BuildConfig
import com.astract.saludapp.Noticia
import com.astract.saludapp.database.MyDatabaseHelper
import com.astract.saludapp.models.NewsResponse
import com.astract.saludapp.models.NoticiaEntity
import com.astract.saludapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response

class NoticiasViewModel(private val dbHelper: MyDatabaseHelper) : ViewModel() {

    private val _noticias = MutableLiveData<List<Noticia>>()
    val noticias: LiveData<List<Noticia>> get() = _noticias

    fun fetchAndUpdateNews(context: Context) {
        viewModelScope.launch {
            try {
                val apiUrl = "https://newsapi.org/v2/top-headlines?category=health&apiKey=${BuildConfig.NEWS_API_KEY}"
                val response: Response<NewsResponse> = RetrofitInstance.api.getHealthNews(apiUrl)

                if (response.isSuccessful) {
                    response.body()?.articles?.let { articles ->
                        articles.forEach { noticia ->
                            val noticiaEntity = NoticiaEntity(
                                source = noticia.source.name,
                                author = noticia.author ?: "",
                                title = noticia.title,
                                description = noticia.description ?: "",
                                url = noticia.url,
                                urlToImage = noticia.urlToImage ?: "",
                                publishedAt = noticia.publishedAt ?: "",
                                content = noticia.content ?: "",
                                language = "es",
                                category = "healthy"
                            )
                            dbHelper.insertOrUpdateNoticia(noticiaEntity) // Cambia a tu método de actualización
                        }

                        // Imprimir información de las noticias actualizadas en la consola
                        Log.d("NoticiasViewModel", "Base de datos actualizada con las siguientes noticias:")
                        articles.forEach { noticia ->
                            Log.d("NoticiasViewModel", "Título: ${noticia.title}, Descripción: ${noticia.description}")
                        }

                        // Cargar noticias actualizadas desde la base de datos
                        loadNoticias()
                    } ?: Log.e("NoticiasViewModel", "No articles found in response")
                } else {
                    Log.e("NoticiasViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NoticiasViewModel", "Error fetching news: ${e.message}")
            }
        }
    }



    fun loadNoticias() {
        val listaDeNoticias = dbHelper.getAllNoticias()
        _noticias.postValue(listaDeNoticias)
    }
}
