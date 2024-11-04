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
import com.astract.saludapp.models.NewsResponse
import com.astract.saludapp.models.NoticiaEntity
import com.astract.saludapp.network.RetrofitInstance
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date

class NoticiasViewModel() : ViewModel() {

    private val _noticias = MutableLiveData<List<Noticia>>()
    private val _noticia = MutableLiveData<Noticia?>()
    val noticia: LiveData<Noticia?> get() = _noticia
    val noticias: LiveData<List<Noticia>> get() = _noticias

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchAndUpdateNews(context: Context) {
        viewModelScope.launch {
            try {
                // Verificar si necesitamos actualizar las noticias
                if (!shouldUpdateNews()) {
                    Log.d("NoticiasViewModel", "Las noticias están actualizadas, cargando desde Firestore")
                    loadNoticias()
                    return@launch
                }

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
                            saveNoticiaToFirestore(noticiaEntity)
                        }

                        // Actualizar la última fecha de actualización
                        updateLastFetchTime()

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

    private suspend fun shouldUpdateNews(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val lastUpdateDoc = firestore.collection("system")
                    .document("lastUpdate")
                    .get()
                    .await()

                if (!lastUpdateDoc.exists()) {
                    return@withContext true
                }

                val lastUpdate = lastUpdateDoc.getTimestamp("timestamp")?.toDate()
                    ?: return@withContext true

                // Verificar si han pasado más de 6 horas desde la última actualización
                val sixHoursAgo = Date(System.currentTimeMillis() - (6 * 60 * 60 * 1000))
                return@withContext lastUpdate.before(sixHoursAgo)
            } catch (e: Exception) {
                Log.e("NoticiasViewModel", "Error checking update time: ${e.message}")
                return@withContext true
            }
        }
    }

    private fun updateLastFetchTime() {
        val updateTime = hashMapOf(
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("system")
            .document("lastUpdate")
            .set(updateTime)
            .addOnFailureListener { e ->
                Log.e("NoticiasViewModel", "Error updating last fetch time: ${e.message}")
            }
    }

    private fun saveNoticiaToFirestore(noticia: NoticiaEntity) {
        val documentId = generateDocumentId(noticia.url)

        firestore.collection("noticias")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    getNextId { nextId ->
                        val noticiaMap = hashMapOf(
                            "id" to nextId,
                            "source" to noticia.source,
                            "author" to noticia.author,
                            "title" to noticia.title,
                            "description" to noticia.description,
                            "url" to noticia.url,
                            "urlToImage" to noticia.urlToImage,
                            "publishedAt" to noticia.publishedAt,
                            "content" to noticia.content,
                            "language" to noticia.language,
                            "category" to noticia.category,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("noticias")
                            .document(documentId)
                            .set(noticiaMap)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Noticia añadida con ID: $documentId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error añadiendo noticia: ", e)
                            }
                    }
                } else {
                    Log.d("Firestore", "La noticia ya existe con ID: $documentId")
                }
            }
    }

    private fun generateDocumentId(url: String): String {
        return url.replace(Regex("[^a-zA-Z0-9]"), "_")
    }

    private fun getNextId(onSuccess: (Int) -> Unit) {
        val counterDoc = firestore.collection("counters").document("noticiaCounter")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterDoc)
            val nextId = if (snapshot.exists()) {
                val currentId = snapshot.getLong("count") ?: 0
                transaction.update(counterDoc, "count", currentId + 1)
                currentId + 1
            } else {
                transaction.set(counterDoc, hashMapOf("count" to 1))
                1
            }
            nextId
        }.addOnSuccessListener { nextId ->
            onSuccess(nextId.toInt())
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error obteniendo el siguiente ID: ", e)
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
            val noticiasList = mutableListOf<DocumentSnapshot>()

            try {
                val querySnapshot = firestore.collection("noticias").get().await()
                noticiasList.addAll(querySnapshot.documents)
            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las noticias de Firestore", e)
            }

            return@withContext noticiasList
        }
    }
}