package com.example.chatfamiliar.model

/** Perfil del usuario guardado en Firestore. */
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = ""
)