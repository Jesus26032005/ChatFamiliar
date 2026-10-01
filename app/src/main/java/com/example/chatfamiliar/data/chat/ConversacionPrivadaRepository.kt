package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.model.ConversacionPrivada
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.security.MessageDigest

class ConversacionPrivadaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val conversaciones = firestore.collection(
        COLECCION_CONVERSACIONES_PRIVADAS)

    private val familias = firestore.collection(COLECCION_FAMILIAS)

    fun obtenerOCrearConversacion(familiaId: String,
        otroUsuarioId: String,
        alCompletar: (Result<String>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            alCompletar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)
                ))
            return
        }
        if (!esIdValido(familiaId) ||
            !esIdValido(otroUsuarioId) ||
            otroUsuarioId == uid) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)
                ))
            return
        }

        val participantes = listOf(uid, otroUsuarioId).sorted()
        val conversacionId = generarConversacionId(participantes)
        val conversacionRef = conversaciones.document(conversacionId)
        val familiaRef = familias.document(familiaId)
        val miembroActualRef = familiaRef
            .collection(SUBCOLECCION_MIEMBROS)
            .document(uid)
        val otroMiembroRef = familiaRef
            .collection(SUBCOLECCION_MIEMBROS)
            .document(otroUsuarioId)
        firestore.runTransaction { transaccion ->
            val familia = transaccion.get(familiaRef)
            val miembroActual = transaccion.get(miembroActualRef)
            val otroMiembro = transaccion.get(otroMiembroRef)
            val conversacion = transaccion.get(conversacionRef)
            if (!familia.exists()) {
                throw ExcepcionChat(
                    CodigoErrorChat.FAMILIA_NO_DISPONIBLE) }
            if (!miembroActual.exists() || !otroMiembro.exists()) {
                throw ExcepcionChat(CodigoErrorChat.SIN_ACCESO)
            }
            if (conversacion.exists()) {
                val participantesGuardados = conversacion.get(
                    CAMPO_PARTICIPANTES) as? List<*>
                if (participantesGuardados != participantes) {
                    throw ExcepcionChat(
                        CodigoErrorChat.DATOS_INVALIDOS) }
                val familiaAnterior = conversacion.getString(
                    CAMPO_FAMILIA_REFERENCIA_ID)
                if (familiaAnterior != familiaId) {
                    transaccion.update(
                        conversacionRef,
                        CAMPO_FAMILIA_REFERENCIA_ID,
                        familiaId)
                }
            } else {
                val datos = mapOf(
                    CAMPO_PARTICIPANTES to participantes,
                    CAMPO_FAMILIA_REFERENCIA_ID to familiaId,
                    CAMPO_FECHA_CREACION to FieldValue.serverTimestamp(),
                    CAMPO_ULTIMO_MENSAJE_ID to "",
                    CAMPO_ULTIMO_MENSAJE to "",
                    CAMPO_ULTIMO_REMITENTE_ID to "",
                    CAMPO_NOMBRE_ULTIMO_REMITENTE to "",
                    CAMPO_FECHA_ULTIMO_MENSAJE to null
                )
                transaccion.set(conversacionRef, datos)
            }
            conversacionId

        }.addOnSuccessListener { id ->
            alCompletar(Result.success(id))
        }.addOnFailureListener { error ->
            alCompletar(
                Result.failure(ExcepcionChat.desde(error)))
        }
    }

    fun escucharConversacion(
        conversacionId: String,
        alActualizar: (Result<ConversacionPrivada>) -> Unit
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            alActualizar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return null }
        if (!esIdValido(conversacionId)) { alActualizar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)))
            return null }

        return conversaciones
            .document(conversacionId)
            .addSnapshotListener { documento, error ->
                if (error != null) {
                    alActualizar(
                        Result.failure(ExcepcionChat.desde(error)))
                    return@addSnapshotListener
                }
                if (documento == null || !documento.exists()) {
                    alActualizar(Result.failure(
                            ExcepcionChat(
                                CodigoErrorChat.DATOS_INVALIDOS)))
                    return@addSnapshotListener
                }
                val resultado = runCatching {
                    val conversacion = documento.toObject(
                        ConversacionPrivada::class.java
                    ) ?: throw ExcepcionChat(
                        CodigoErrorChat.DATOS_INVALIDOS)
                    val participantes = conversacion.participantes
                    if (participantes.size != 2 ||
                        participantes.distinct().size != 2
                    ) { throw ExcepcionChat(
                            CodigoErrorChat.DATOS_INVALIDOS) }
                    if (uid !in participantes) {
                        throw ExcepcionChat(
                            CodigoErrorChat.SIN_ACCESO) }
                    conversacion.copy(id = documento.id) }
                resultado.onSuccess {
                    alActualizar(Result.success(it))
                }.onFailure { errorLectura ->
                    val errorChat = if (errorLectura is ExcepcionChat) {
                        errorLectura
                    } else {
                        ExcepcionChat(
                            codigo = CodigoErrorChat.DATOS_INVALIDOS,
                            causa = errorLectura) }
                    alActualizar(Result.failure(errorChat))
                }
            }
    }

    private fun generarConversacionId(
        participantes: List<String>
    ): String {
        val contenido = participantes.joinToString(separator = "") {
            "${it.length}:$it"
        }

        val resumen = MessageDigest
            .getInstance("SHA-256")
            .digest(contenido.toByteArray(Charsets.UTF_8))

        return resumen.joinToString(separator = "") { byte ->
            (byte.toInt() and 0xff)
                .toString(16)
                .padStart(2, '0')
        }
    }

    private fun esIdValido(id: String): Boolean {
        return id.isNotBlank() &&
                !id.contains("/") &&
                id != "." &&
                id != ".."
    }

    companion object {
        private const val COLECCION_CONVERSACIONES_PRIVADAS =
            "conversacionesPrivadas"
        private const val CAMPO_ULTIMO_MENSAJE_ID = "ultimoMensajeId"
        private const val COLECCION_FAMILIAS = "familias"
        private const val SUBCOLECCION_MIEMBROS = "miembros"

        private const val CAMPO_PARTICIPANTES = "participantes"
        private const val CAMPO_FAMILIA_REFERENCIA_ID =
            "familiaReferenciaId"

        private const val CAMPO_FECHA_CREACION = "fechaCreacion"
        private const val CAMPO_ULTIMO_MENSAJE = "ultimoMensaje"
        private const val CAMPO_ULTIMO_REMITENTE_ID =
            "ultimoRemitenteId"
        private const val CAMPO_NOMBRE_ULTIMO_REMITENTE =
            "nombreUltimoRemitente"
        private const val CAMPO_FECHA_ULTIMO_MENSAJE =
            "fechaUltimoMensaje"
    }
}