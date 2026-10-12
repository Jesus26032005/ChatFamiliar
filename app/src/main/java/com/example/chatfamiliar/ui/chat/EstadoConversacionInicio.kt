package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import com.example.chatfamiliar.model.ConversacionResumen

/** Estado de una fila de la lista de chats: resumen, cargas y errores de cada dato. */
data class EstadoConversacionInicio(
    val resumen: ConversacionResumen = ConversacionResumen(),
    val cargandoNombre: Boolean = false,
    val cargandoUltimoMensaje: Boolean = true,
    val conteoConfirmado: Boolean = false,
    val actualizandoConteo: Boolean = false,

    @get:StringRes
    val errorNombreRecurso: Int? = null,

    @get:StringRes
    val errorUltimoMensajeRecurso: Int? = null,

    @get:StringRes
    val errorConteoRecurso: Int? = null) {
    val clave: String
        get() = "${resumen.tipo}:${resumen.id}"

    val mensajesNoLeidosVisibles: Int?
        get() = if (conteoConfirmado) { resumen.mensajesNoLeidos } else { null } }