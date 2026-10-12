package com.example.chatfamiliar.model

import com.google.firebase.Timestamp

/** Último mensaje que el usuario marcó como leído en una conversación. */
data class LecturaConversacion(
    val uid: String = "",
    val ultimoMensajeLeidoId: String = "",
    val fechaUltimoMensajeLeido: Timestamp? = null)