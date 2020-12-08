package com.example.ladm_u3_ejercico5_practica2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE agenda (id INTEGER NOT NULL PRIMARY KEY, lugar TEXT, fecha TEXT, hora TEXT, descripcion TEXT);")
        db.execSQL("CREATE TABLE sincronizar (id INTEGER NOT NULL PRIMARY KEY, idagenda INTEGER, accion VARCHAR(1) );")//0 = insertar, 1 = eliminar, 2 = actualizar
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

}