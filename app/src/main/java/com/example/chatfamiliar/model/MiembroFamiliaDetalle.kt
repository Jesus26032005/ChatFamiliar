package com.example.chatfamiliar.model

/** Integrante de la familia con los datos de su perfil, para mostrarlo en pantalla. */
data class MiembroFamiliaDetalle(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = MiembroFamilia.ROL_MIEMBRO) {
    val esAdministrador: Boolean
        get() = rol == MiembroFamilia.ROL_ADMINISTRADOR }