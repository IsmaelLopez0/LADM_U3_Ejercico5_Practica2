package com.example.ladm_u3_ejercico5_practica2

class ClaveRemoto(idDoc: String, idAgenda: Int) {
    private var idDocumento = idDoc
    private var idAgenda = idAgenda

    fun getIdDocument(): String{
        return idDocumento
    }

    fun getIdAgenda(): Int{
        return idAgenda
    }

}