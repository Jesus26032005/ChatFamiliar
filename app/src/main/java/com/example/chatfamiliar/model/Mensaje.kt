package com.example.chatfamiliar.model
import com.google.firebase.Timestamp

data class Mensaje(
    val id: String = "",
    val remitenteId: String = "",
    val nombreRemitente: String = "",
    val contenido: String = "",
    val fechaEnvio: Timestamp? = null,
    val tipo: String = TIPO_TEXTO,
    val duracionSegundos: Long = 0L
) {
    val esRegistroLlamada: Boolean
        get() = tipo == TIPO_LLAMADA || tipo == TIPO_LLAMADA_PERDIDA
    companion object {
        const val TIPO_TEXTO = "texto"
        const val TIPO_LLAMADA = "llamada"
        const val TIPO_LLAMADA_PERDIDA = "llamada_perdida"
    }
}