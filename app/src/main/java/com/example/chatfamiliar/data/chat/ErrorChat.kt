package com.example.chatfamiliar.data.chat

import com.google.firebase.firestore.FirebaseFirestoreException

enum class CodigoErrorChat {
    SESION_REQUERIDA,
    IDENTIFICADOR_INVALIDO,
    MENSAJE_VACIO,
    MENSAJE_DEMASIADO_LARGO,
    FAMILIA_NO_DISPONIBLE,
    SIN_ACCESO,
    PERFIL_NO_DISPONIBLE,
    NOMBRE_REQUERIDO,
    CONFLICTO_MENSAJE,
    MENSAJE_NO_DISPONIBLE,
    FECHA_NO_CONFIRMADA,
    DATOS_INVALIDOS,
    SERVICIO_NO_DISPONIBLE,
    TIEMPO_AGOTADO,
    DESCONOCIDO
}

class ExcepcionChat(
    val codigo: CodigoErrorChat,
    causa: Throwable? = null
) : Exception(codigo.name, causa) {

    companion object {

        fun desde(error: Throwable): ExcepcionChat {
            // funcion auxlar borrar para produccion
            val errorFirestoreDiagnostico = generateSequence(error) {
                it.cause
            }
                .take(12)
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()

            if (errorFirestoreDiagnostico != null) {
                android.util.Log.w(
                    "ChatFirestore",
                    "${errorFirestoreDiagnostico.code}: " +
                            errorFirestoreDiagnostico.message.orEmpty()
                )
            }
            ///xdfdf

            val causas = generateSequence(error) { it.cause }
                .take(12)
                .toList()

            // Conserva los errores propios, incluso si Firebase los envuelve.
            val errorChat = causas
                .filterIsInstance<ExcepcionChat>()
                .firstOrNull()

            if (errorChat != null) {
                return errorChat
            }

            val errorFirestore = causas
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()

            val codigo = when (errorFirestore?.code) {
                FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                    CodigoErrorChat.SESION_REQUERIDA

                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    CodigoErrorChat.SIN_ACCESO

                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    CodigoErrorChat.SERVICIO_NO_DISPONIBLE

                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    CodigoErrorChat.TIEMPO_AGOTADO

                else ->
                    CodigoErrorChat.DESCONOCIDO
            }

            return ExcepcionChat(
                codigo = codigo,
                causa = error
            )
        }
    }
}