package com.senati.mitiemposemanal.bbarrera.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.senati.mitiemposemanal.bbarrera.model.Categoria

class CategoriaDao(ctx: Context) {
    private val helper = DBHelper(ctx)

    fun listar(): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        helper.readableDatabase.rawQuery(
            """SELECT c.id, c.nombre, COUNT(t.id)
               FROM categoria c LEFT JOIN tarea t ON t.id_categoria = c.id
               GROUP BY c.id
               ORDER BY c.nombre COLLATE NOCASE""", null
        ).use { c ->
            while (c.moveToNext()) lista.add(Categoria(c.getInt(0), c.getString(1), c.getInt(2)))
        }
        return lista
    }

    fun insertar(nombre: String): Boolean = try {
        val v = ContentValues().apply { put("nombre", nombre) }
        helper.writableDatabase.insertOrThrow("categoria", null, v)
        true
    } catch (e: SQLiteConstraintException) {
        false
    }

    fun actualizar(id: Int, nombre: String): Boolean = try {
        val v = ContentValues().apply { put("nombre", nombre) }
        helper.writableDatabase.update("categoria", v, "id = ?", arrayOf(id.toString()))
        true
    } catch (e: SQLiteConstraintException) {
        false
    }

    fun contarTareas(id: Int): Int {
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM tarea WHERE id_categoria = ?", arrayOf(id.toString())
        ).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    fun eliminar(id: Int) {
        helper.writableDatabase.delete("categoria", "id = ?", arrayOf(id.toString()))
    }
}