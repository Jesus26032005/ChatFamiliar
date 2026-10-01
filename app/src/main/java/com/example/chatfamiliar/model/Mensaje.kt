package com.example.chatfamiliar.model
import com.google.firebase.Timestamp

data class Mensaje(
    val id: String = "",
    val remitenteId: String = "",
    val nombreRemitente: String = "",
    val contenido: String = "",
    val fechaEnvio: Timestamp? = null
)