package com.example.chatfamiliar.model

/** Familia registrada; creadoPor es el uid del administrador principal. */
data class Familia(
    val id: String = "",
    val nombre: String = "",
    val codigoInvitacion: String = "",
    val creadoPor: String = ""
)