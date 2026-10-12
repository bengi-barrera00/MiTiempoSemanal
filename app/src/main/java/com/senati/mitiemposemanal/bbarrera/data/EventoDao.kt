package com.senati.mitiemposemanal.bbarrera.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.senati.mitiemposemanal.bbarrera.model.Evento
import com.senati.mitiemposemanal.bbarrera.util.Tiempo
import java.time.Duration

class EventoDao(ctx: Context) {
    private val helper = DBHelper(ctx)

    private val base = """SELECT e.id, e.id_tarea, t.nombre, c.nombre, e.inicio, e.fin, e.segundos
               FROM evento e
               JOIN tarea t ON t.id = e.id_tarea
               JOIN categoria c ON c.id = t.id_categoria"""

    private fun leer(c: Cursor) = Evento(
        c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4),
        if (c.isNull(5)) null else c.getString(5),
        if (c.isNull(6)) null else c.getLong(6)
    )

    fun listar(): List<Evento> {
        val lista = mutableListOf<Evento>()
        helper.readableDatabase.rawQuery("$base ORDER BY e.inicio DESC", null).use { c ->
            while (c.moveToNext()) lista.add(leer(c))
        }
        return lista
    }

    fun enCurso(): Evento? {
        helper.readableDatabase.rawQuery("$base WHERE e.fin IS NULL LIMIT 1", null).use { c ->
            return if (c.moveToFirst()) leer(c) else null
        }
    }

    fun iniciar(idTarea: Int): Boolean {
        if (enCurso() != null) return false
        val v = ContentValues().apply {
            put("id_tarea", idTarea)
            put("inicio", Tiempo.guardar(Tiempo.ahora()))
        }
        helper.writableDatabase.insertOrThrow("evento", null, v)
        return true
    }

    fun finalizar(e: Evento) {
        val fin = Tiempo.ahora()
        val seg = Duration.between(Tiempo.leer(e.inicio), fin).seconds
        val v = ContentValues().apply {
            put("fin", Tiempo.guardar(fin))
            put("segundos", seg.coerceAtLeast(0))
        }
        helper.writableDatabase.update("evento", v, "id = ?", arrayOf(e.id.toString()))
    }
}