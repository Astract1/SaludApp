package com.astract.saludapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.astract.saludapp.Noticia
import com.astract.saludapp.Source
import com.astract.saludapp.models.NoticiaEntity

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
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
    }

    override fun onCreate(db: SQLiteDatabase) {
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
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Método para insertar datos
    fun insertNoticia(noticia: NoticiaEntity) {
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
            put(COLUMN_LANGUAGE, noticia.language)
            put(COLUMN_CATEGORY, noticia.category)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
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
                    content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
                    language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LANGUAGE)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
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
                put(COLUMN_LANGUAGE, noticia.language)
                put(COLUMN_CATEGORY, noticia.category)
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
                content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
                language = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LANGUAGE)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
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



}
