package com.example.chatfamiliar.model

data class MiembroFamiliaDetalle(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = MiembroFamilia.ROL_MIEMBRO
) {
    val esAdministrador: Boolean
        get() = rol == MiembroFamilia.ROL_ADMINISTRADOR
}