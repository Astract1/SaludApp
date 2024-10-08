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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class NoticiasViewModel(private val dbHelper: MyDatabaseHelper) : ViewModel() {

    private val _noticias = MutableLiveData<List<Noticia>>()
    private val _noticia = MutableLiveData<Noticia?>()
    val noticia: LiveData<Noticia?> get() = _noticia

    val noticias: LiveData<List<Noticia>> get() = _noticias

    fun fetchAndUpdateNews(context: Context) {
        viewModelScope.launch {
            try {
                val apiUrl =
                    "https://newsapi.org/v2/top-headlines?category=health&apiKey=${BuildConfig.NEWS_API_KEY}"
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
                            dbHelper.insertOrUpdateNoticia(noticiaEntity)
                        }

                        Log.d(
                            "NoticiasViewModel",
                            "Base de datos actualizada con las siguientes noticias:"
                        )
                        articles.forEach { noticia ->
                            Log.d(
                                "NoticiasViewModel",
                                "Título: ${noticia.title}, Descripción: ${noticia.description}"
                            )
                        }

                        loadNoticias()
                    } ?: Log.e("NoticiasViewModel", "No articles found in response")
                } else {
                    Log.e("NoticiasViewModel", "Error response: ${response.errorBody()?.string()}")
                    showToast(context, "Error al obtener noticias: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("NoticiasViewModel", "Error fetching news: ${e.message}")
                showToast(context, "Error al obtener noticias: ${e.localizedMessage}")
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun loadNoticias() {
        viewModelScope.launch {
            try {
                val listaDeNoticias = withContext(Dispatchers.IO) {
                    dbHelper.getAllNoticias()
                }
                _noticias.postValue(listaDeNoticias)
            } catch (e: Exception) {
                // Manejar el error adecuadamente, como mostrar un mensaje al usuario
                Log.e("Error", "Error al cargar las noticias", e)
            }
        }
    }


    fun getNoticiaById(id: Int) {
        viewModelScope.launch {
            val result = dbHelper.getNoticiaById(id)
            _noticia.postValue(result)
        }
    }
}
