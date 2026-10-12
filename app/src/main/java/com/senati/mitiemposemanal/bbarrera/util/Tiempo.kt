package com.senati.mitiemposemanal.bbarrera.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Tiempo {
    private val ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val VISTA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    fun ahora(): LocalDateTime = LocalDateTime.now().withNano(0)

    fun guardar(f: LocalDateTime): String = f.format(ISO)
    fun leer(s: String): LocalDateTime = LocalDateTime.parse(s, ISO)
    fun mostrar(f: LocalDateTime): String = f.format(VISTA)

    fun duracion(seg: Long): String {
        val h = seg / 3600
        val m = (seg % 3600) / 60
        val s = seg % 60
        return when {
            h > 0 -> "$h h $m min"
            m > 0 -> "$m min $s s"
            else -> "$s s"
        }
    }

    fun reloj(seg: Long): String {
        val h = seg / 3600
        val m = (seg % 3600) / 60
        val s = seg % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }
}