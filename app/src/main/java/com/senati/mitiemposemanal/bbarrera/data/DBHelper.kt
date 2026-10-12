package com.senati.mitiemposemanal.bbarrera.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(ctx: Context) : SQLiteOpenHelper(ctx, "mitiempo.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE categoria(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE COLLATE NOCASE)"""
        )
        db.execSQL(
            """CREATE TABLE tarea(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE COLLATE NOCASE,
                id_categoria INTEGER NOT NULL,
                FOREIGN KEY(id_categoria) REFERENCES categoria(id))"""
        )
        listOf("Estudio", "Trabajo", "Descanso", "Ocio").forEach {
            db.execSQL("INSERT INTO categoria(nombre) VALUES(?)", arrayOf(it))
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}