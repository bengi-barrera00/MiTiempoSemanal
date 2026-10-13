package com.senati.mitiemposemanal.bbarrera.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.senati.mitiemposemanal.bbarrera.model.ResumenCategoria
import com.senati.mitiemposemanal.bbarrera.util.Tiempo
import java.time.LocalDate

class ReporteDao(ctx: Context) {
    private val helper = DBHelper(ctx)
    private val eventoDao = EventoDao(ctx)

    fun cerrarSemanas() {
        val yaGuardadas = semanas().toSet()
        // Una semana con un evento todavía en curso no se cierra: su tiempo aún no está completo
        val bloqueada = eventoDao.enCurso()?.let { Tiempo.lunesDe(Tiempo.leer(it.inicio).toLocalDate()) }

        eventoDao.semanasConEventos(Tiempo.inicioSemana())
            .filter { it !in yaGuardadas && it != bloqueada }
            .forEach { guardar(it) }
    }

    fun semanas(): List<LocalDate> {
        val lista = mutableListOf<LocalDate>()
        helper.readableDatabase.rawQuery(
            "SELECT DISTINCT semana FROM reporte ORDER BY semana DESC", null
        ).use { c ->
            while (c.moveToNext()) lista.add(LocalDate.parse(c.getString(0)))
        }
        return lista
    }

    fun detalle(lunes: LocalDate): List<ResumenCategoria> {
        val lista = mutableListOf<ResumenCategoria>()
        helper.readableDatabase.rawQuery(
            """SELECT c.id, c.nombre, r.segundos, r.porcentaje
               FROM reporte r JOIN categoria c ON c.id = r.id_categoria
               WHERE r.semana = ?
               ORDER BY c.nombre COLLATE NOCASE""", arrayOf(lunes.toString())
        ).use { c ->
            while (c.moveToNext()) lista.add(
                ResumenCategoria(c.getInt(0), c.getString(1), c.getLong(2), c.getDouble(3))
            )
        }
        return lista
    }

    private fun guardar(lunes: LocalDate) {
        val resumen = eventoDao.resumenSemana(lunes.atStartOfDay())
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            resumen.forEach {
                val v = ContentValues().apply {
                    put("semana", lunes.toString())
                    put("id_categoria", it.id)
                    put("segundos", it.segundos)
                    put("porcentaje", it.segundos * 100.0 / Tiempo.SEMANA_SEG)
                }
                db.insertWithOnConflict("reporte", null, v, SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}