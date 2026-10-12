package com.senati.mitiemposemanal.bbarrera.model

data class Categoria(val id: Int, val nombre: String, val tareas: Int = 0)

data class Tarea(val id: Int, val nombre: String, val idCategoria: Int, val categoria: String)

data class Fila(val id: Int, val titulo: String, val detalle: String? = null)

data class Evento(
    val id: Int,
    val idTarea: Int,
    val tarea: String,
    val categoria: String,
    val inicio: String,
    val fin: String? = null,
    val segundos: Long? = null
)