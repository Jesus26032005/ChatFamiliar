package com.example.chatfamiliar.model

import com.google.firebase.Timestamp

data class LecturaConversacion(
    val uid: String = "",
    val ultimoMensajeLeidoId: String = "",
    val fechaUltimoMensajeLeido: Timestamp? = null
)