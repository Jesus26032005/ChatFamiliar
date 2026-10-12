package com.example.chatfamiliar.model

/** Persona con la que se puede abrir un chat privado y las familias que comparten. */
data class ContactoPrivado(
    val uid: String,
    val nombre: String,
    val familiasCompartidas: List<Familia>)