package com.senati.mitiemposemanal.bbarrera.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.senati.mitiemposemanal.bbarrera.model.Tarea

class TareaDao(ctx: Context) {
    private val helper = DBHelper(ctx)

    fun listar(): List<Tarea> {
        val lista = mutableListOf<Tarea>()
        helper.readableDatabase.rawQuery(
            """SELECT t.id, t.nombre, t.id_categoria, c.nombre
               FROM tarea t JOIN categoria c ON c.id = t.id_categoria
               ORDER BY t.nombre COLLATE NOCASE""", null
        ).use { c ->
            while (c.moveToNext()) lista.add(
                Tarea(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3))
            )
        }
        return lista
    }

    fun insertar(nombre: String, idCategoria: Int): Boolean = try {
        val v = ContentValues().apply {
            put("nombre", nombre)
            put("id_categoria", idCategoria)
        }
        helper.writableDatabase.insertOrThrow("tarea", null, v)
        true
    } catch (e: SQLiteConstraintException) {
        false
    }

    fun actualizar(id: Int, nombre: String, idCategoria: Int): Boolean = try {
        val v = ContentValues().apply {
            put("nombre", nombre)
            put("id_categoria", idCategoria)
        }
        helper.writableDatabase.update("tarea", v, "id = ?", arrayOf(id.toString()))
        true
    } catch (e: SQLiteConstraintException) {
        false
    }

    fun eliminar(id: Int) {
        helper.writableDatabase.delete("tarea", "id = ?", arrayOf(id.toString()))
    }

    fun buscar(texto: String): List<Tarea> =
        consultar("WHERE t.nombre LIKE ?", arrayOf("%$texto%"), 8)

    fun buscarExacta(nombre: String): Tarea? =
        consultar("WHERE t.nombre = ?", arrayOf(nombre), 1).firstOrNull()

    private fun consultar(filtro: String, args: Array<String>, limite: Int): List<Tarea> {
        val lista = mutableListOf<Tarea>()
        helper.readableDatabase.rawQuery(
            """SELECT t.id, t.nombre, t.id_categoria, c.nombre
               FROM tarea t JOIN categoria c ON c.id = t.id_categoria
               $filtro
               ORDER BY t.nombre COLLATE NOCASE
               LIMIT $limite""", args
        ).use { c ->
            while (c.moveToNext()) lista.add(
                Tarea(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3))
            )
        }
        return lista
    }
}