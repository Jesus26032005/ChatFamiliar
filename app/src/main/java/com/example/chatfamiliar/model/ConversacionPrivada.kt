package com.example.chatfamiliar.model

import com.google.firebase.Timestamp

/**
 * Documento de una conversación privada: participantes, familia de referencia y resumen del último mensaje.
 */
data class ConversacionPrivada(
    val id: String = "",
    val participantes: List<String> = emptyList(),
    val familiaReferenciaId: String = "",
    val fechaCreacion: Timestamp? = null,
    val ultimoMensaje: String = "",
    val ultimoRemitenteId: String = "",
    val nombreUltimoRemitente: String = "",
    val fechaUltimoMensaje: Timestamp? = null,
    val ultimoMensajeId: String = ""
)