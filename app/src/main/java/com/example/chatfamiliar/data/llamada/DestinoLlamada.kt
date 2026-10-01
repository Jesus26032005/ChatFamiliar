package com.example.chatfamiliar.data.llamada

sealed interface DestinoLlamada {
    data class Privado(
        val conversacionId: String,
        val familiaCompartidaId: String,
        val otroUid: String
    ) : DestinoLlamada
    data class Familia(
        val familiaId: String,
        val nombreFamilia: String
    ) : DestinoLlamada
}
object DatosLlamada {
    const val CLAVE_TIPO_CHAT = "tipoChat"
    const val CLAVE_FAMILIA_ID = "familiaId"
    const val CLAVE_NOMBRE_FAMILIA = "nombreFamilia"
    const val TIPO_PRIVADO = "privado"
    const val TIPO_FAMILIA = "familia"
}