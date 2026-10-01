package com.example.chatfamiliar.ui.chat

import androidx.annotation.StringRes
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.chat.CodigoErrorChat
import com.example.chatfamiliar.data.chat.ExcepcionChat

@StringRes
fun obtenerRecursoErrorChat(error: Throwable): Int {
    val codigo = ExcepcionChat.desde(error).codigo
    return when (codigo) {
        CodigoErrorChat.SESION_REQUERIDA ->
            R.string.chat_error_session_required

        CodigoErrorChat.IDENTIFICADOR_INVALIDO ->
            R.string.chat_error_invalid_identifier

        CodigoErrorChat.MENSAJE_VACIO ->
            R.string.chat_error_empty_message

        CodigoErrorChat.MENSAJE_DEMASIADO_LARGO ->
            R.string.chat_error_message_too_long

        CodigoErrorChat.FAMILIA_NO_DISPONIBLE ->
            R.string.chat_error_family_unavailable

        CodigoErrorChat.SIN_ACCESO ->
            R.string.chat_error_access_denied

        CodigoErrorChat.PERFIL_NO_DISPONIBLE ->
            R.string.chat_error_profile_unavailable

        CodigoErrorChat.NOMBRE_REQUERIDO ->
            R.string.chat_error_name_required

        CodigoErrorChat.CONFLICTO_MENSAJE ->
            R.string.chat_error_message_conflict

        CodigoErrorChat.MENSAJE_NO_DISPONIBLE ->
            R.string.chat_error_message_unavailable

        CodigoErrorChat.FECHA_NO_CONFIRMADA ->
            R.string.chat_error_date_unconfirmed

        CodigoErrorChat.DATOS_INVALIDOS ->
            R.string.chat_error_invalid_data

        CodigoErrorChat.SERVICIO_NO_DISPONIBLE ->
            R.string.chat_error_service_unavailable

        CodigoErrorChat.TIEMPO_AGOTADO ->
            R.string.chat_error_timeout

        CodigoErrorChat.DESCONOCIDO ->
            R.string.chat_error_unknown
    }
}