package com.senati.mitiemposemanal.bbarrera.model

data class Categoria(val id: Int, val nombre: String, val tareas: Int = 0)

data class Tarea(val id: Int, val nombre: String, val idCategoria: Int, val categoria: String)

data class Fila(val id: Int, val titulo: String, val detalle: String? = null)