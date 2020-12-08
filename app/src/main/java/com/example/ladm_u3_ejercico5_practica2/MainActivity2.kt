package com.example.ladm_u3_ejercico5_practica2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity2 : AppCompatActivity() {
    var baseDatos = BaseDatos(this, "agenda", null, 1)
    var id = ""
    var cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var extras = intent.extras
        id = extras!!.getString("id")!!
        editText6.isCursorVisible = false
        editText7.isCursorVisible = false

        editText5.setText(extras.getString("lugar"))
        editText6.setText(extras.getString("fecha"))
        editText7.setText(extras.getString("hora"))
        editText8.setText(extras.getString("descripcion"))

        btnRegresar.setOnClickListener {
            finish()
        }

        btnActualizar.setOnClickListener {
            actualizar()
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "yyyy-MM-dd" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            editText6.setText(sdf.format(cal.time))
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            editText7.setText("$hour:$minute")
        }

        editText6.setOnClickListener {
            DatePickerDialog(
                    this, dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        editText7.setOnClickListener {
            var hour = cal.get(Calendar.HOUR)
            var minute = cal.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, timeSetListener, hour, minute, true)
            timePickerDialog.show()
        }

    }

    private fun actualizar() {
        try {
            var trans = baseDatos.writableDatabase
            var values = ContentValues()
            values.put("lugar", editText5.text.toString())
            values.put("fecha", editText6.text.toString())
            values.put("hora", editText7.text.toString())
            values.put("descripcion", editText8.text.toString())
            var res = trans.update("agenda", values, "id=?", arrayOf(id))
            if (res == -1) mensaje("No se pudo actualizar")
            else{
                mensaje("Se actualizó correctamente")
                var values2 = ContentValues()
                values2.put("idagenda", id)
                values2.put("accion", "2")
                var res2 = trans.insert("sincronizar", null, values2)
                if (res2 == -1L) mensaje("Algo no salió bien con la inserción para sincronización")
                else finish()
            }
            trans.close()
        } catch (e: SQLiteException){ mensaje(e.message) }
    }

    private fun mensaje(s: String?) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}