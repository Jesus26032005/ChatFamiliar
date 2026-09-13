package com.example.chatfamiliar.model

data class MiembroFamilia(
    val uid: String = "",
    val rol: String = ROL_MIEMBRO,
    val codigoUnion: String = ""
) {
    companion object {
        const val ROL_MIEMBRO = "miembro"
        const val ROL_ADMINISTRADOR = "administrador"
    }
}