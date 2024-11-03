package com.astract.saludapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.astract.saludapp.Articulo
import com.astract.saludapp.HistorialIMCData
import com.astract.saludapp.Noticia
import com.astract.saludapp.Source
import com.astract.saludapp.models.NoticiaEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        const val COLUMN_IS_SAVED = "is_saved"


        //Tabla de Historial de IMC

        const val TABLE_NAME_IMC = "historial_imc"
        const val COLUMN_ID_IMC = "id"
        const val COLUMN_IMC = "imc"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_PESO = "peso"
        const val COLUMN_ALTURA = "altura"

        // Tabla de Inscripciones a Retos
        const val TABLE_NAME_INSCRIPCIONES = "inscripciones_retos"
        const val COLUMN_ID_INSCRIPCION = "id"
        const val COLUMN_TITULO_RETO = "titulo_reto"
        const val COLUMN_FECHA_INSCRIPCION = "fecha_inscripcion"
        const val COLUMN_FRECUENCIA_NOTIFICACION = "frecuencia_notificacion"
        const val COLUMN_COMPLETADO = "completado"


        //





    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("MyDatabaseHelper", "Creating tables...")

        val createTable = """
    CREATE TABLE $TABLE_NAME (
        $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_SOURCE TEXT,
        $COLUMN_AUTHOR TEXT,
        $COLUMN_TITLE TEXT,
        $COLUMN_DESCRIPTION TEXT,
        $COLUMN_URL TEXT,
        $COLUMN_URL_TO_IMAGE TEXT,
        $COLUMN_PUBLISHED_AT TEXT,
        $COLUMN_CONTENT TEXT,
        $COLUMN_LANGUAGE TEXT,
        $COLUMN_CATEGORY TEXT,
        $COLUMN_IS_SAVED INTEGER DEFAULT 0
    );
""".trimIndent()

        val createTableArticulos = """
    CREATE TABLE $TABLE_NAME_ARTICULOS (
        $COLUMN_ID_ARTICULO INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_TITLE_ARTICULO TEXT,
        $COLUMN_BODY_ARTICULO TEXT,
        $COLUMN_AUTHOR_ARTICULO TEXT,
        $COLUMN_PUBLISHED_AT_ARTICULO TEXT,
        $COLUMN_URL_ARTICULO TEXT,
        $COLUMN_IMAGE_URL_ARTICULO TEXT,
        $COLUMN_IS_SAVED INTEGER DEFAULT 0
    );
""".trimIndent()


        val createTableIMC = ("CREATE TABLE $TABLE_NAME_IMC (" +
                "$COLUMN_ID_IMC INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_IMC REAL," +
                "$COLUMN_FECHA TEXT," +
                "$COLUMN_PESO REAL," +
                "$COLUMN_ALTURA REAL)")

        val createTableInscripciones = """
        CREATE TABLE $TABLE_NAME_INSCRIPCIONES (
            $COLUMN_ID_INSCRIPCION INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TITULO_RETO TEXT,
            $COLUMN_FECHA_INSCRIPCION TEXT,
            $COLUMN_FRECUENCIA_NOTIFICACION INTEGER,
            $COLUMN_COMPLETADO INTEGER DEFAULT 0
        )
    """.trimIndent()

        db.execSQL(createTable)
        db.execSQL(createTableArticulos)
        db.execSQL(createTableIMC)
        db.execSQL(createTableInscripciones)
    }



    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_ARTICULOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_IMC")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_INSCRIPCIONES")
        onCreate(db)
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

    fun insertHistorialIMC(imc: Double, fecha: String, peso: Double, altura: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IMC, imc)
            put(COLUMN_FECHA, fecha)
            put(COLUMN_PESO, peso)
            put(COLUMN_ALTURA, altura)
        }
        val id = db.insert(TABLE_NAME_IMC, null, values)
        if (id == -1L) {
            Log.e("MyDatabaseHelper", "Error al insertar el IMC: $imc")
        } else {
            Log.d("MyDatabaseHelper", "IMC insertado con ID: $id")
        }
        db.close()
    }




    fun checkTableExists(): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_NAME_IMC'", null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun deleteHistorialIMCById(id: Int): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        return try {
            val rowsAffected = db.delete(TABLE_NAME_IMC, "$COLUMN_ID = ?", arrayOf(id.toString()))
            rowsAffected > 0 // Retorna true si se eliminó al menos un registro
        } catch (e: SQLiteException) {
            Log.e("MyDatabaseHelper", "Error al eliminar historial IMC: ${e.message}")
            false
        } finally {
            db.close() // Cerrar la base de datos
        }
    }


    fun obtenerUltimoIMC(): Double?{
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_IMC FROM $TABLE_NAME_IMC ORDER BY $COLUMN_ID_IMC DESC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        val imc: Double? = if (cursor.moveToFirst()) {
            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_IMC))
        } else {
            null
        }
        cursor.close()
        db.close()
        return imc
    }

    fun inscribirseAReto(tituloReto: String, frecuenciaNotificacion: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITULO_RETO, tituloReto)
            put(COLUMN_FECHA_INSCRIPCION, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
            put(COLUMN_FRECUENCIA_NOTIFICACION, frecuenciaNotificacion)
            put(COLUMN_COMPLETADO, 0) // Añadir el estado inicial como no completado
        }

        db.insert(TABLE_NAME_INSCRIPCIONES, null, values)
        db.close()
    }

    fun obtenerRetosInscritos(): List<String> {
        val retosInscritos = mutableListOf<String>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_NAME_INSCRIPCIONES,
            arrayOf(COLUMN_TITULO_RETO),
            null,
            null,
            null,
            null,
            "$COLUMN_FECHA_INSCRIPCION DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                retosInscritos.add(it.getString(it.getColumnIndexOrThrow(COLUMN_TITULO_RETO)))
            }
        }

        return retosInscritos
    }


    fun estaInscritoEnReto(tituloReto: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME_INSCRIPCIONES,
            null,
            "$COLUMN_TITULO_RETO = ?",
            arrayOf(tituloReto),
            null,
            null,
            null
        )

        val estaInscrito = cursor.count > 0
        cursor.close()
        db.close()
        return estaInscrito
    }
    fun isRetoCompletado(tituloReto: String): Boolean {
        val db = this.readableDatabase
        var completado = false

        val cursor = db.query(
            TABLE_NAME_INSCRIPCIONES,
            arrayOf(COLUMN_COMPLETADO),
            "$COLUMN_TITULO_RETO = ?",
            arrayOf(tituloReto),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                completado = it.getInt(it.getColumnIndexOrThrow(COLUMN_COMPLETADO)) == 1
            }
        }

        return completado
    }

    fun actualizarEstadoReto(tituloReto: String, completado: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COMPLETADO, if (completado) 1 else 0)
        }

        db.update(
            TABLE_NAME_INSCRIPCIONES,
            values,
            "$COLUMN_TITULO_RETO = ?",
            arrayOf(tituloReto)
        )
    }

    fun eliminarInscripcionReto(tituloReto: String): Boolean {
        val db = this.writableDatabase
        return try {
            val rowsDeleted = db.delete(
                TABLE_NAME_INSCRIPCIONES,
                "$COLUMN_TITULO_RETO = ?",
                arrayOf(tituloReto)
            )
            db.close()
            rowsDeleted > 0
        } catch (e: Exception) {
            Log.e("MyDatabaseHelper", "Error al eliminar la inscripción: ${e.message}")
            false
        }
    }

    fun saveArticulo(id: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_IS_SAVED, 1) // Marca el artículo como guardado

        db.update(TABLE_NAME_ARTICULOS, values, "$COLUMN_ID_ARTICULO = ?", arrayOf(id.toString()))
        db.close()
    }

    fun saveNoticia(id: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_IS_SAVED, 1) // Marca la noticia como guardada

        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun unSaveNoticia(id : Int){
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_IS_SAVED, 0) // Marca la noticia como guardada

        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun isNoticiaSaved (id: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_IS_SAVED),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        val isSaved = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SAVED)) == 1
        } else {
            false
        }
        cursor.close()
        db.close()
        return isSaved
    }



    fun getArticulosGuardados(): List<Articulo> {
        val articulosGuardados = mutableListOf<Articulo>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME_ARTICULOS,
            null,
            "$COLUMN_IS_SAVED = ?",
            arrayOf("1"),  // Busca artículos que están marcados como guardados
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val articulo = Articulo(
                    articleId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_ARTICULO)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE_ARTICULO)),
                    abstract = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY_ARTICULO)),
                    author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR_ARTICULO)),
                    publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT_ARTICULO)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL_ARTICULO)),
                    imagenurl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL_ARTICULO))
                )
                articulosGuardados.add(articulo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return articulosGuardados
    }


    fun unSaveArticulo(id: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_IS_SAVED, 0) // Marca el artículo como no guardado

        db.update(TABLE_NAME_ARTICULOS, values, "$COLUMN_ID_ARTICULO = ?", arrayOf(id.toString()))
        db.close()
    }

    fun isAriculoSaved(id: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME_ARTICULOS,
            arrayOf(COLUMN_IS_SAVED),
            "$COLUMN_ID_ARTICULO = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        val isSaved = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SAVED)) == 1
        } else {
            false
        }
        cursor.close()
        db.close()
        return isSaved
    }

}
