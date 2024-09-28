package com.astract.saludapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.astract.saludapp.Articulo
import com.astract.saludapp.Noticia
import com.astract.saludapp.Source
import com.astract.saludapp.models.NoticiaEntity

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        //Tabla de Noticias
        private const val DATABASE_NAME = "noticias.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "noticias"
        const val COLUMN_ID = "id"
        const val COLUMN_SOURCE = "source"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_URL = "url"
        const val COLUMN_URL_TO_IMAGE = "urlToImage"
        const val COLUMN_PUBLISHED_AT = "publishedAt"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_LANGUAGE = "language"
        const val COLUMN_CATEGORY = "category"



        // Tabla de Articulos

        const val TABLE_NAME_ARTICULOS = "articulos"
        const val COLUMN_ID_ARTICULO = "id"
        const val COLUMN_TITLE_ARTICULO = "title"
        const val COLUMN_BODY_ARTICULO = "body"
        const val COLUMN_AUTHOR_ARTICULO = "author"
        const val COLUMN_PUBLISHED_AT_ARTICULO = "publishedAt"
        const val COLUMN_URL_ARTICULO = "url"
        const val COLUMN_IMAGE_URL_ARTICULO = "imageUrl"

    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("MyDatabaseHelper", "Creating tables...")

        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_SOURCE TEXT," +
                "$COLUMN_AUTHOR TEXT," +
                "$COLUMN_TITLE TEXT," +
                "$COLUMN_DESCRIPTION TEXT," +
                "$COLUMN_URL TEXT," +
                "$COLUMN_URL_TO_IMAGE TEXT," +
                "$COLUMN_PUBLISHED_AT TEXT," +
                "$COLUMN_CONTENT TEXT," +
                "$COLUMN_LANGUAGE TEXT," +
                "$COLUMN_CATEGORY TEXT)")

        val createTableArticulos = ("CREATE TABLE $TABLE_NAME_ARTICULOS (" +
                "$COLUMN_ID_ARTICULO INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_TITLE_ARTICULO TEXT," +
                "$COLUMN_BODY_ARTICULO TEXT," +
                "$COLUMN_AUTHOR_ARTICULO TEXT," +
                "$COLUMN_PUBLISHED_AT_ARTICULO TEXT," +
                "$COLUMN_URL_ARTICULO TEXT," +
                "$COLUMN_IMAGE_URL_ARTICULO TEXT)")

        db.execSQL(createTable)
        db.execSQL(createTableArticulos)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_ARTICULOS")
        onCreate(db)
    }



    // Método para obtener todas las noticias
    fun getAllNoticias(): List<Noticia> {
        val listaDeNoticias = mutableListOf<Noticia>()
        val db: SQLiteDatabase = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                // Aquí asumimos que la columna de origen está en formato JSON o similar
                val sourceJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE))
                val source = Source(name = sourceJson) // Ajusta esto según cómo quieras estructurarlo

                val noticia = Noticia(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    source = source,
                    author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                    urlToImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL_TO_IMAGE)),
                    publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT)),
                    content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                )
                listaDeNoticias.add(noticia)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listaDeNoticias
    }


    fun insertOrUpdateNoticia(noticia: NoticiaEntity) {
        if (!noticiaExists(noticia.url) && !isRemoved(noticia)) { // Verifica si la noticia ya existe y no está "removed"
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_SOURCE, noticia.source)
                put(COLUMN_AUTHOR, noticia.author)
                put(COLUMN_TITLE, noticia.title)
                put(COLUMN_DESCRIPTION, noticia.description)
                put(COLUMN_URL, noticia.url)
                put(COLUMN_URL_TO_IMAGE, noticia.urlToImage)
                put(COLUMN_PUBLISHED_AT, noticia.publishedAt)
                put(COLUMN_CONTENT, noticia.content)
            }
            db.insert(TABLE_NAME, null, values)
            db.close()
        } else {
            Log.d("MyDatabaseHelper", "Noticia ya existe o es 'removed': ${noticia.title}")
        }
    }
    fun noticiaExists(url: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_URL),
            "$COLUMN_URL = ?",
            arrayOf(url),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getNoticiaById(id: Int): Noticia? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val sourceJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE))
            val source = Source(name = sourceJson)

            val noticia = Noticia(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                source = source,
                author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                urlToImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL_TO_IMAGE)),
                publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT)),
                content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            )
            cursor.close()
            db.close()
            noticia
        } else {
            cursor.close()
            db.close()
            null
        }
    }


    private fun isRemoved(noticia: NoticiaEntity): Boolean {
        return noticia.title.contains("removed", ignoreCase = true) ||
                noticia.description.contains("removed", ignoreCase = true)
    }




    fun getAllArticulos(): List<Articulo> {
        val listaDeArticulos = mutableListOf<Articulo>()
        val db: SQLiteDatabase = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery("SELECT * FROM $TABLE_NAME_ARTICULOS", null)

            cursor?.let {
                if (it.moveToFirst()) {
                    do {
                        // Asegúrate de que los índices no sean negativos
                        val articleIdIndex = it.getColumnIndex(COLUMN_ID_ARTICULO)
                        val titleIndex = it.getColumnIndex(COLUMN_TITLE_ARTICULO)

                        // Verifica que los índices sean válidos
                        if (articleIdIndex >= 0 && titleIndex >= 0) {
                            val articulo = Articulo(
                                articleId = it.getInt(articleIdIndex),
                                title = it.getString(titleIndex),
                                abstract = it.getString(it.getColumnIndexOrThrow(COLUMN_BODY_ARTICULO)) ?: "",
                                author = it.getString(it.getColumnIndexOrThrow(COLUMN_AUTHOR_ARTICULO)) ?: "",
                                publishedAt = it.getString(it.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT_ARTICULO)) ?: "",
                                url = it.getString(it.getColumnIndexOrThrow(COLUMN_URL_ARTICULO)) ?: "",
                                imagenurl = it.getString(it.getColumnIndexOrThrow(COLUMN_IMAGE_URL_ARTICULO)) ?: ""
                            )
                            listaDeArticulos.add(articulo)
                        } else {
                            Log.e("MyDatabaseHelper", "Invalid column index.")
                        }
                    } while (it.moveToNext())
                } else {
                    Log.e("MyDatabaseHelper", "No articles found in the database.")
                }
            } ?: run {
                Log.e("MyDatabaseHelper", "Cursor is null.")
            }
        } catch (e: Exception) {
            Log.e("MyDatabaseHelper", "Error loading articles: ${e.message}", e)
        } finally {
            cursor?.close()
            db.close()
        }

        return listaDeArticulos
    }




    fun getArticuloById(id: Int): Articulo? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME_ARTICULOS,
            null,
            "$COLUMN_ID_ARTICULO = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return try {
            if (cursor != null && cursor.moveToFirst()) {
                // Crear el objeto Articulo
                Articulo(
                    articleId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_ARTICULO)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE_ARTICULO)),
                    abstract = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY_ARTICULO)),
                    author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR_ARTICULO)),
                    publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT_ARTICULO)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL_ARTICULO)),
                    imagenurl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL_ARTICULO))
                ).also {
                    Log.d("MyDatabaseHelper", "Artículo encontrado: ${it.title}")
                }
            } else {
                Log.e("MyDatabaseHelper", "No se encontró el artículo con ID: $id")
                null
            }
        } catch (e: Exception) {
            Log.e("MyDatabaseHelper", "Error al recuperar el artículo: ${e.message}")
            null
        } finally {
            cursor?.close()  // Asegúrate de cerrar el cursor
        }
    }



    fun insertOrUpdateArticulo(articulo: Articulo) {
        if (!articuloExists(articulo.url)) {  // Verifica si el artículo existe usando la URL
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE_ARTICULO, articulo.title)
                put(COLUMN_BODY_ARTICULO, articulo.abstract)
                put(COLUMN_PUBLISHED_AT_ARTICULO, articulo.publishedAt)
                put(COLUMN_URL_ARTICULO, articulo.url)
                put(COLUMN_IMAGE_URL_ARTICULO, articulo.imagenurl)
                put(COLUMN_AUTHOR_ARTICULO, articulo.author)  // Asegúrate de tener esta columna
            }
            val id = db.insert(TABLE_NAME_ARTICULOS, null, values)
            if (id == -1L) {
                Log.e("MyDatabaseHelper", "Error al insertar el artículo: ${articulo.title}")
            } else {
                Log.d("MyDatabaseHelper", "Artículo insertado con ID: $id")
            }
            db.close()
        } else {
            updateArticulo(articulo)  // Llama a la función de actualización si existe
        }
    }

    fun articuloExists(url: String): Boolean {  // Cambié el argumento para usar la URL
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME_ARTICULOS,
            arrayOf(COLUMN_URL_ARTICULO),  // Asegúrate de tener la columna de URL
            "$COLUMN_URL_ARTICULO = ?",  // Consulta utilizando la URL
            arrayOf(url),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun updateArticulo(articulo: Articulo) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE_ARTICULO, articulo.title)
            put(COLUMN_BODY_ARTICULO, articulo.abstract)
            put(COLUMN_PUBLISHED_AT_ARTICULO, articulo.publishedAt)
            put(COLUMN_URL_ARTICULO, articulo.url)
            put(COLUMN_IMAGE_URL_ARTICULO, articulo.imagenurl)
            put(COLUMN_AUTHOR_ARTICULO, articulo.author)  // Asegúrate de tener esta columna
        }

        val rowsAffected = db.update(
            TABLE_NAME_ARTICULOS,
            values,
            "$COLUMN_URL_ARTICULO = ?",  // Actualiza usando la URL
            arrayOf(articulo.url)
        )

        if (rowsAffected > 0) {
            Log.d("MyDatabaseHelper", "Artículo actualizado: ${articulo.title}")
        } else {
            Log.e("MyDatabaseHelper", "Error al actualizar el artículo: ${articulo.title}")
        }
        db.close()
    }

}
