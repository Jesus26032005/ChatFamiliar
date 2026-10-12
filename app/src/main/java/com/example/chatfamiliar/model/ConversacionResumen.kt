package com.example.chatfamiliar.model
import com.google.firebase.Timestamp

/** Datos que muestra cada fila de la lista de chats, ya sea familiar o privado. */
data class ConversacionResumen(
    val id: String = "",
    val tipo: String = TIPO_FAMILIA,
    val titulo: String = "",
    val ultimoMensaje: String = "",
    val ultimoRemitenteId: String = "",
    val nombreUltimoRemitente: String = "",
    val fechaUltimoMensaje: Timestamp? = null,
    val mensajesNoLeidos: Int = 0,
    val familiaId: String = "",
    val otroUsuarioId: String = ""
) {
    companion object {
        const val TIPO_FAMILIA = "familia"
        const val TIPO_PRIVADO = "privado"
    } }