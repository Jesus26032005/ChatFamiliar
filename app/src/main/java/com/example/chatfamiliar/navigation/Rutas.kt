package com.example.chatfamiliar.navigation

import android.net.Uri

/** Rutas de navegación y funciones para construirlas con sus argumentos codificados. */
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val VERIFICACION = "verificacion"
    const val HOME = "home"
    const val GESTION_FAMILIA = "gestion_familia"
    const val GESTION_FAMILIA_CON_ARGUMENTOS =
        "$GESTION_FAMILIA?familiaId={familiaId}"
    const val NUEVO_MENSAJE = "nuevo_mensaje"
    const val CHAT_FAMILIA =
        "chat_familia/{familiaId}?nombre={nombre}"
    const val CHAT_PRIVADO =
        "chat_privado/{conversacionId}"

    /** Ruta de gestión de familia; sin id abre la opción de crear o unirse. */
    fun crearRutaGestionFamilia(familiaId: String?): String { return if (familiaId.isNullOrBlank()) { GESTION_FAMILIA }
        else { "$GESTION_FAMILIA?familiaId=${Uri.encode(familiaId)}" } }
    /** Ruta del chat de una familia con su nombre para la cabecera. */
    fun crearRutaChatFamilia(familiaId: String, nombre: String): String {
        return "chat_familia/${Uri.encode(familiaId)}" +
                "?nombre=${Uri.encode(nombre)}" }

    /** Ruta de un chat privado. */
    fun crearRutaChatPrivado(conversacionId: String): String { return "chat_privado/${Uri.encode(conversacionId)}" } }