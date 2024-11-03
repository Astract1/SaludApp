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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Response
class NoticiasViewModel(private val dbHelper: MyDatabaseHelper) : ViewModel() {

    private val _noticias = MutableLiveData<List<Noticia>>()
    private val _noticia = MutableLiveData<Noticia?>()
    val noticia: LiveData<Noticia?> get() = _noticia
    val noticias: LiveData<List<Noticia>> get() = _noticias

    private val firestore = FirebaseFirestore.getInstance() // Inicializa Firestore

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

                            // Guarda la noticia en Firestore
                            saveNoticiaToFirestore(noticiaEntity)
                        }

                        Log.d("NoticiasViewModel", "Base de datos Firestore actualizada con las siguientes noticias:")
                        articles.forEach { noticia ->
                            Log.d("NoticiasViewModel", "Título: ${noticia.title}, Descripción: ${noticia.description}")
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

    private fun saveNoticiaToFirestore(noticia: NoticiaEntity) {
        // Primero, verificamos si la noticia ya existe usando la URL
        val query = firestore.collection("noticias")
            .whereEqualTo("url", noticia.url) // Cambia la comparación a la URL

        query.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                // Si no hay resultados, obtenemos el siguiente ID
                getNextId { nextId ->
                    val noticiaMap = hashMapOf(
                        "id" to nextId,  // Añadimos el ID autoincremental
                        "source" to noticia.source,
                        "author" to noticia.author,
                        "title" to noticia.title,
                        "description" to noticia.description,
                        "url" to noticia.url,
                        "urlToImage" to noticia.urlToImage,
                        "publishedAt" to noticia.publishedAt,
                        "content" to noticia.content,
                        "language" to noticia.language,
                        "category" to noticia.category
                    )

                    firestore.collection("noticias")
                        .add(noticiaMap)
                        .addOnSuccessListener { documentReference ->
                            Log.d("Firestore", "Noticia añadida con ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error añadiendo noticia: ", e)
                        }
                }
            } else {
                Log.d("Firestore", "La noticia ya existe y no se añadirá.")
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error realizando la consulta: ", e)
        }
    }
    private fun getNextId(onSuccess: (Int) -> Unit) {
        val counterDoc = firestore.collection("counters").document("noticiaCounter")

        // Intentar obtener el siguiente ID usando una transacción
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterDoc)
            val nextId = if (snapshot.exists()) {
                val currentId = snapshot.getLong("count") ?: 0
                transaction.update(counterDoc, "count", currentId + 1)
                currentId + 1
            } else {
                // Si no existe el documento, lo creamos
                transaction.set(counterDoc, hashMapOf("count" to 1))
                1
            }
            nextId
        }.addOnSuccessListener { nextId ->
            onSuccess(nextId.toInt())
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error obteniendo el siguiente ID: ", e)
            // Aquí podrías optar por reintentar si es necesario.
        }
    }


    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun loadNoticias() {
        viewModelScope.launch {
            try {
                val listaDeNoticias = withContext(Dispatchers.IO) {
                    val result = fetchNoticiasFromFirestore()
                    result.map { document ->
                        Noticia(
                            id = document.getLong("id")?.toInt() ?: 0,
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            content = document.getString("content") ?: "",
                            url = document.getString("url") ?: "",
                            urlToImage = document.getString("urlToImage") ?: "",
                            publishedAt = document.getString("publishedAt") ?: "",
                            language = document.getString("language") ?: "es"
                        )
                    }
                }
                _noticias.postValue(listaDeNoticias)
            } catch (e: Exception) {
                Log.e("Error", "Error al cargar las noticias", e)
            }
        }
    }

    private suspend fun fetchNoticiasFromFirestore(): List<DocumentSnapshot> {
        return withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val noticiasList = mutableListOf<DocumentSnapshot>()

            try {
                val querySnapshot = db.collection("noticias").get().await() // Asegúrate de usar coroutines
                noticiasList.addAll(querySnapshot.documents)
            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las noticias de Firestore", e)
            }

            return@withContext noticiasList
        }
    }



}