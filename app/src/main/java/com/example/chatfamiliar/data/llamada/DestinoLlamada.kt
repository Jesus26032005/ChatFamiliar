package com.example.chatfamiliar.data.llamada

/** A quién se dirige una videollamada. */
sealed interface DestinoLlamada {
    /** Llamada 1 a 1 desde un chat privado. */
    data class Privado(
        val conversacionId: String,
        val familiaCompartidaId: String,
        val otroUid: String) : DestinoLlamada
    /** Llamada a todos los integrantes de una familia. */
    data class Familia(
        val familiaId: String,
        val nombreFamilia: String) : DestinoLlamada }
/** Claves de los datos extra (custom) que viajan con cada llamada de Stream. */
object DatosLlamada {
    const val CLAVE_TIPO_CHAT = "tipoChat"
    const val CLAVE_FAMILIA_ID = "familiaId"
    const val CLAVE_NOMBRE_FAMILIA = "nombreFamilia"
    const val TIPO_PRIVADO = "privado"
    const val TIPO_FAMILIA = "familia"
}