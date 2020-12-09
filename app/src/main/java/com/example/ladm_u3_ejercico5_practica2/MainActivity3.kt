package com.example.ladm_u3_ejercico5_practica2

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main3.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity3 : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var baseDatos = BaseDatos(this, "agenda", null, 1)
    var listaID = ArrayList<String>()
    var remoto = false //false: BD local, true: BD remota
    var datos = ArrayList<String>()
    var cal = Calendar.getInstance()
    var opcion = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        datos.add("-")
        resultadoConsulta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View, arg2: Int, arg3: Long) {
                opcion = arg3.toInt()
                if (opcion==3) txt2.visibility = View.VISIBLE
                else txt2.visibility = View.INVISIBLE
                when(opcion){
                    0 -> {txt1.hint = "Descripcion"}
                    1 -> {txt1.hint = "Lugar"}
                    2 -> {txt1.hint = "Fecha"}
                    3 -> {txt1.hint = "Rango de fechas"}
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {
                // TODO Auto-generated method stub
            }
        }

        btnRegresar2.setOnClickListener { finish() }

        btnConsultar.setOnClickListener {
            if (radioButton.isChecked) consultaLocal()
            else if (radioButton2.isChecked) consultaRemota()
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "yyyy-MM-dd" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            txt1.setText(sdf.format(cal.time))
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            txt1.setText(txt1.text.toString() + " $hour:$minute")
        }

        val dateSetListener2 = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "yyyy-MM-dd" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            txt2.setText(sdf.format(cal.time))
        }

        val timeSetListener2 = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            txt2.setText(txt2.text.toString() + " $hour:$minute")
        }

        txt1.setOnClickListener {
            if (opcion>=2){
                var hour = cal.get(Calendar.HOUR)
                var minute = cal.get(Calendar.MINUTE)
                val timePickerDialog = TimePickerDialog(this, timeSetListener, hour, minute, true)
                timePickerDialog.show()

                DatePickerDialog(
                    this,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        txt2.setOnClickListener {
            var hour = cal.get(Calendar.HOUR)
            var minute = cal.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, timeSetListener2, hour, minute, true)
            timePickerDialog.show()

            DatePickerDialog(
                this, dateSetListener2, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnEliminar.setOnClickListener { alertEliminar() }
    }

    private fun consultaRemota() {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        //val format = SimpleDateFormat("yyyy-MMMM-dd HH:mm")
        when (opcion) {
            0 -> {
                baseRemota.collection("agenda")
                    .whereEqualTo("descripcion", txt1.text.toString())
                    .addSnapshotListener { value, error ->
                        if (error != null){
                            datos.add("No hay coincidencia en BD remota")
                            return@addSnapshotListener
                        }
                        if (value != null) {
                            listaID.clear()
                            remoto = true
                            datos.clear()
                            for (document in value) {
                                var cad = "ID: ${document.id}\n" +
                                        "Lugar: ${document.getString("lugar")}\n" +
                                        "Fecha: ${format.format(document.getTimestamp("fecha")!!.toDate())}\n" +
                                        "Descripción: ${document.getString("descripcion")}"
                                listaID.add(document.id)
                                datos.add(cad)
                            }
                            resultadoConsulta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
                            btnEliminar.isEnabled = true
                        }
                    }
            }
            1 -> {
                baseRemota.collection("agenda")
                    .whereEqualTo("lugar", txt1.text.toString())
                    .addSnapshotListener { value, error ->
                        if (error != null){
                            datos.add("No hay coincidencia en BD remota")
                            return@addSnapshotListener
                        }
                        if (value != null) {
                            listaID.clear()
                            remoto = true
                            datos.clear()
                            for (document in value) {
                                var cad = "ID: ${document.id}\n" +
                                        "Lugar: ${document.getString("lugar")}\n" +
                                        "Fecha: ${format.format(document.getTimestamp("fecha")!!.toDate())}\n" +
                                        "Descripción: ${document.getString("descripcion")}"
                                listaID.add(document.id)
                                datos.add(cad)
                            }
                            resultadoConsulta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
                            btnEliminar.isEnabled = true
                        }
                    }
            }
            2 -> {
                val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val date = formatter.parse(txt1.text.toString()) as Date
                baseRemota.collection("agenda")
                    .whereEqualTo("fecha", Timestamp(date))
                    .addSnapshotListener { value, error ->
                        if (error != null){
                            datos.add("No hay coincidencia en BD remota")
                            return@addSnapshotListener
                        }
                        if (value != null) {
                            listaID.clear()
                            remoto = true
                            datos.clear()
                            for (document in value) {
                                var cad = "ID: ${document.id}\n" +
                                        "Lugar: ${document.getString("lugar")}\n" +
                                        "Fecha: ${format.format(document.getTimestamp("fecha")!!.toDate())}\n" +
                                        "Descripción: ${document.getString("descripcion")}"
                                listaID.add(document.id)
                                datos.add(cad)
                            }
                            resultadoConsulta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
                            btnEliminar.isEnabled = true
                        }
                    }
            }
            3 -> {
                val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val date = formatter.parse(txt1.text.toString()) as Date
                val date2 = formatter.parse(txt2.text.toString()) as Date
                baseRemota.collection("agenda")
                    .whereGreaterThan("fecha", Timestamp(date))
                    .whereLessThan("fecha", Timestamp(date2))
                    .addSnapshotListener { value, error ->
                        if (error != null){
                            datos.add("No hay coincidencia en BD remota")
                            return@addSnapshotListener
                        }
                        if (value != null) {
                            listaID.clear()
                            remoto = true
                            datos.clear()
                            for (document in value) {
                                var cad = "ID: ${document.id}\n" +
                                        "Lugar: ${document.getString("lugar")}\n" +
                                        "Fecha: ${format.format(document.getTimestamp("fecha")!!.toDate())}\n" +
                                        "Descripción: ${document.getString("descripcion")}"
                                listaID.add(document.id)
                                datos.add(cad)
                            }
                            resultadoConsulta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
                            btnEliminar.isEnabled = true
                        }
                    }
            }
        }
    }

    private fun consultaLocal() {
        try {
            var trans = baseDatos.readableDatabase
            when (opcion) {
                0 -> {
                    var res = trans.query(
                        "agenda",
                        arrayOf("*"),
                        "descripcion=?",
                        arrayOf(txt1.text.toString()),
                        null,
                        null,
                        null
                    ); saveInArray(res)
                }
                1 -> {
                    var res = trans.query(
                        "agenda",
                        arrayOf("*"),
                        "lugar=?",
                        arrayOf(txt1.text.toString()),
                        null,
                        null,
                        null
                    ); saveInArray(res)
                }
                2 -> {
                    var res = trans.query(
                        "agenda",
                        arrayOf("*"),
                        "fecha=?",
                        arrayOf(txt1.text.toString()),
                        null,
                        null,
                        null
                    ); saveInArray(res)
                }
                3 -> {
                    var res = trans.query(
                        "agenda",
                        arrayOf("*"),
                        "descripcion=?",
                        arrayOf(txt1.text.toString()),
                        null,
                        null,
                        null
                    ); saveInArray(res)
                }
            }
            trans.close()
        } catch (e: SQLiteException) { mensaje(e.message) }
    }

    private fun saveInArray(res: Cursor?) {
        datos.clear()
        listaID.clear()
        if (res!!.moveToNext()){
            do {
               var cad = "ID: ${res.getInt(0)}\n" +
                       "Lugar: ${res.getString(1)}\n" +
                       "Fecha: ${res.getString(2)}\n" +
                       "Hora: ${res.getString(3)}\n" +
                       "Descripción: ${res.getString(4)}"
                listaID.add("${res.getInt(0)}")
                datos.add(cad)
            } while (res.moveToNext())
        }else{
            datos.add("No encontró resultados")
        }
        btnEliminar.isEnabled = true
        resultadoConsulta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
    }

    private fun mensaje(s: String?) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun alertEliminar(){
        AlertDialog.Builder(this)
                .setTitle("ATENCIÓN")
                .setMessage("¿Esta seguro que desea eliminar todos los resultados de la consulta?")
                .setPositiveButton("Continuar") { _, _ -> eliminar() }
                .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
                .show()
    }

    private fun eliminar() {
        if (remoto){
            (0 until listaID.size).forEach {
                baseRemota.collection("agenda")
                        .document(listaID[it])
                        .delete()
                        .addOnFailureListener { it2 ->
                            mensaje("Alguno(s) no se pudo borrar, ${it2.message}")
                        }
            }
        } else {
            try {
                var trans = baseDatos.writableDatabase
                (0 until listaID.size).forEach {
                    var trans = baseDatos.writableDatabase
                    var res = trans.delete("agenda", "id=?", arrayOf(listaID[it]))
                    if (res == -1){ mensaje("ERROR! No se pudo eliminar") }
                    else {
                        var res2 = trans.delete("sincronizar", "idagenda=?", arrayOf(listaID[it]))
                        mensaje("Se eliminó con exito")
                        var values = ContentValues()
                        values.put("idagenda", listaID[it].toInt())
                        values.put("accion", "1")
                        var res3 = trans.insert("sincronizar", null, values)
                        if (res3 == -1L) mensaje("ERROR, no se pudo insertar para sincronización")
                    }

                    /*var res = trans.delete("agenda", "id=?", arrayOf(listaID[it]))
                    if (res == -1) { mensaje("No se pudo eliminar ${listaID[it]}") }
                    else {
                        var res2 = trans.delete("sincronizar", "idagenda=?", arrayOf(listaID[it]))
                        mensaje("Se eliminó con exito")
                        var values = ContentValues()
                        values.put("idagenda", listaID[it].toInt())
                        values.put("accion", "1")
                        var res3 = trans.insert("sincronizar", null, values)
                    }*/
                }
                trans.close()
            } catch (e: SQLiteException) { mensaje(e.message) }
        }
        datos.clear()
        listaID.clear()
        resultadoConsulta.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
        btnEliminar.isEnabled = false
    }
}