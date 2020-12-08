package com.example.ladm_u3_ejercico5_practica2

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var baseDatos = BaseDatos(this, "agenda", null, 1)
    var listaID = ArrayList<String>()
    var datos = ArrayList<String>()
    var idRemotos = ArrayList<ClaveRemoto>()
    var cal = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarDatos()
        editText2.isCursorVisible = false
        editText3.isCursorVisible = false

        btnRegistrar.setOnClickListener {
            insertar()
        }

        btnSincronizar.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                sincronizar()
            }
        }

        baseRemota.collection("agenda")
                .addSnapshotListener { value, error ->
                    if (error != null ){
                        return@addSnapshotListener
                    }
                    idRemotos.clear()
                    for ( registro in value!! ){
                        idRemotos.add(
                            ClaveRemoto(
                                registro.id,
                                registro.getLong("idagenda")!!.toInt()
                            )
                        )
                    }
                }

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "yyyy-MM-dd" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            editText2.setText(sdf.format(cal.time))
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            editText3.setText("$hour:$minute")
        }

        editText2.setOnClickListener {
            DatePickerDialog(
                this, dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        editText3.setOnClickListener {
            var hour = cal.get(Calendar.HOUR)
            var minute = cal.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, timeSetListener, hour, minute, true)
            timePickerDialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sincronizar() {
        sincDelete()
        sincInsert()
        sincUpdate()
    }

    @SuppressLint("SimpleDateFormat", "Recycle")
    private fun sincUpdate() {
        try {
            var trans = baseDatos.readableDatabase
            var res = trans.query( "sincronizar", arrayOf("idagenda"), "accion=?", arrayOf("2"), null, null, null)
            if (res.moveToNext()) {
                do {
                    for (registro in idRemotos){
                        if ( registro.getIdAgenda() == res.getInt(0) ){
                            var res2 = trans.query("agenda", arrayOf("*"), "id=?", arrayOf(registro.getIdAgenda().toString()), null, null, null )
                            res2.moveToFirst()
                            val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                            baseRemota.collection("agenda")
                                    .document(registro.getIdDocument())
                                    .update("lugar", res2.getString(1),
                                    "fecha", formatter.parse(res2.getString(2)+" "+res2.getString(3)) as Date,
                                    "descripcion", res2.getString(4))
                                    //.addOnSuccessListener { }
                                    .addOnFailureListener { mensaje("ERROR, no se actualizó\n ${it.message}") }
                        }
                    }
                } while ( res.moveToNext() )
            }
            trans.close()
        } catch (e: SQLiteException){ mensaje(e.message) }
    }

    private fun sincDelete() {
        try {
            var trans = baseDatos.readableDatabase
            var res = trans.query(
                "sincronizar",
                arrayOf("idagenda"),
                "accion=?",
                arrayOf("1"),
                null,
                null,
                null
            )
            if (res.moveToNext()) {
                do {
                    for (registro in idRemotos){
                        if ( registro.getIdAgenda() == res.getInt(0) ){
                            baseRemota.collection("agenda")
                                    .document(registro.getIdDocument())
                                    .delete()
                                    .addOnSuccessListener { }
                                    .addOnFailureListener { mensaje("ERROR, no se elimino\n ${it.message}") }
                        }
                    }
                } while ( res.moveToNext() )
            }
            trans.close()
        } catch (e: SQLiteException){ mensaje(e.message) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sincInsert() {
        try {
            var trans = baseDatos.readableDatabase
            var res = trans.query( "sincronizar", arrayOf("idagenda"), "accion=?", arrayOf("0"), null, null, null )
            if (res.moveToNext()) {
                do {
                    var res2 = trans.query(
                        "agenda", arrayOf("*"), "id=?", arrayOf(
                            res.getInt(0).toString()
                        ), null, null, null
                    )
                    res2.moveToFirst()
                    var datosInsertar = hashMapOf<String, Any>()
                    val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    val date = formatter.parse(res2.getString(2)+" "+res2.getString(3)) as Date
                    datosInsertar.put("idagenda", res2.getInt(0))
                    datosInsertar.put("lugar", res2.getString(1))
                    datosInsertar.put("fecha", Timestamp(date) )
                    //datosInsertar.put("hora", res2.getString(3))
                    datosInsertar.put("descripcion", res2.getString(4))
                    baseRemota.collection("agenda")
                            .add(datosInsertar)
                            .addOnSuccessListener { }
                            .addOnFailureListener { mensaje("Error, no se pudo insertar ${it.message}") }
                } while (res.moveToNext())
                var res3 = trans.delete("sincronizar", "accion=?", arrayOf("0"))
            }
            trans.close()
        } catch (e: SQLiteException){ mensaje(e.message) }
    }

    private fun insertar() {
        try {
            var trans = baseDatos.writableDatabase
            var values = ContentValues()
            values.put("lugar", editText.text.toString())
            values.put("fecha", editText2.text.toString())
            values.put("hora", editText3.text.toString())
            values.put("descripcion", editText4.text.toString())
            var res = trans.insert("agenda", null, values)
            if (res == -1L) { mensaje("ERROR, no se pudo insertar") }
            else {
                mensaje("Se insertó correctamente")
                limpiarCampos()
                cargarDatos()

                var trans2 = baseDatos.writableDatabase
                var res2 = trans2.query(
                    "agenda",
                    arrayOf("*"),
                    null,
                    null,
                    null,
                    null,
                    "id DESC",
                    "1"
                )
                res2.moveToFirst()
                var values2 = ContentValues()
                values2.put("idagenda", res2.getInt(0))
                values2.put("accion", "0")
                var res3 = trans2.insert("sincronizar", null, values2)
                if (res3 == -1L) mensaje("ERROR, no se pudo insertar para sincronización")
                trans2.close()
            }
            trans.close()
        } catch (e: SQLiteException){
            mensaje(e.message)
        }
    }

    private fun limpiarCampos() {
        editText.setText("")
        editText2.setText("")
        editText3.setText("")
        editText4.setText("")
    }

    private fun cargarDatos() {
        try {
            var trans = baseDatos.readableDatabase
            listaID.clear()
            datos.clear()
            var res = trans.query("agenda", arrayOf("*"), null, null, null, null, null)
            if ( res.moveToNext() ){
                do {
                    var cad = "ID: ${res.getInt(0)}\n" +
                            "Lugar: ${res.getString(1)}\n" +
                            "Fecha: ${res.getString(2)}\n" +
                            "Hora: ${res.getString(3)}\n" +
                            "Domicilio: ${res.getString(4)}"
                    listaID.add(res.getInt(0).toString())
                    datos.add(cad)
                } while (res.moveToNext())
            } else{ datos.add("No hay citas registradas") }
            localList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
            localList.setOnItemClickListener { parent, view, position, id ->
                AlertDialog.Builder(this)
                        .setTitle("ATENCIÓN")
                        .setMessage("¿Qué desea hacer?\n ${datos.get(position)}")
                        .setPositiveButton("Actualizar") { _, _ ->
                            var trans2 = baseDatos.readableDatabase
                            var res2 = trans2.query("agenda", arrayOf("*"), "id=?", arrayOf(listaID.get(position)), null, null, null)
                            if(res2.moveToNext()){
                                var intent = Intent(this, MainActivity2::class.java)
                                intent.putExtra("id", res2.getInt(0).toString())
                                intent.putExtra("lugar", res2.getString(1))
                                intent.putExtra("fecha", res2.getString(2))
                                intent.putExtra("hora", res2.getString(3))
                                intent.putExtra("descripcion", res2.getString(4))
                                startActivity(intent)
                            } else { mensaje("Algo salió mal") }
                        }
                        .setNegativeButton("Eliminar"){ _, _ -> eliminar(listaID.get(position)) }
                        .setNeutralButton("Nada"){ d, _ -> d.dismiss() }
                        .show()
            }
            trans.close()
        } catch (e: SQLiteException){
            mensaje(e.message)
        }
    }

    private fun eliminar(idEliminar: String) {
        try {
            var trans = baseDatos.writableDatabase
            var res = trans.delete("agenda", "id=?", arrayOf(idEliminar))
            if (res == 0){ mensaje("ERROR! No se pudo eliminar") }
            else {
                var res2 = trans.delete("sincronizar", "idagenda=?", arrayOf(idEliminar))
                mensaje("Se eliminó con exito")
                var values = ContentValues()
                values.put("idagenda", idEliminar.toInt())
                values.put("accion", "1")
                var res3 = trans.insert("sincronizar", null, values)
                if (res3 == -1L) mensaje("ERROR, no se pudo insertar para sincronización")
            }
            trans.close()
            cargarDatos()
        } catch (e: SQLiteException) {
            mensaje(e.message!!)
        }
    }

    private fun mensaje(s: String?) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}