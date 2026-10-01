package com.example.chatfamiliar.model

data class ContactoPrivado(
    val uid: String,
    val nombre: String,
    val familiasCompartidas: List<Familia>
)