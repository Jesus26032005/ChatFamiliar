package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.model.LecturaConversacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class LecturaFamiliaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val familias = firestore.collection(COLECCION_FAMILIAS)

    fun escucharLectura(
        familiaId: String,
        alActualizar: (Result<LecturaConversacion>) -> Unit
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            alActualizar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return null
        }
        if (!esIdValido(familiaId)) {
            alActualizar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)))
            return null }
        return familias
            .document(familiaId)
            .collection(SUBCOLECCION_LECTURAS)
            .document(uid)
            .addSnapshotListener { documento, error ->
                if (error != null) {
                    alActualizar(
                        Result.failure(ExcepcionChat.desde(error)))
                    return@addSnapshotListener
                }
                if (documento == null) {
                    alActualizar(Result.failure(
                            ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)
                        ))
                    return@addSnapshotListener
                }
                val resultado = runCatching {
                    if (!documento.exists()) {
                        LecturaConversacion(uid = uid)
                    } else {
                        val lectura = documento.toObject(
                            LecturaConversacion::class.java
                        ) ?: throw ExcepcionChat(
                            CodigoErrorChat.DATOS_INVALIDOS)
                        lectura.copy(uid = uid) } }
                resultado.onSuccess {
                    alActualizar(Result.success(it))
                }.onFailure {
                    alActualizar(
                        Result.failure(
                            ExcepcionChat(
                                codigo = CodigoErrorChat.DATOS_INVALIDOS,
                                causa = it)))
                }
            }
    }

    fun actualizarLectura(familiaId: String, mensajeId: String,
        alCompletar: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            alCompletar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)
                ))
            return
        }

        if (!esIdValido(familiaId) || !esIdValido(mensajeId)) {
            alCompletar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)
                ))
            return
        }

        val familiaRef = familias.document(familiaId)
        val miembroRef = familiaRef
            .collection(SUBCOLECCION_MIEMBROS)
            .document(uid)
        val mensajeRef = familiaRef
            .collection(SUBCOLECCION_MENSAJES)
            .document(mensajeId)
        val lecturaRef = familiaRef
            .collection(SUBCOLECCION_LECTURAS)
            .document(uid)
        firestore.runTransaction { transaccion ->
            val familia = transaccion.get(familiaRef)
            val miembro = transaccion.get(miembroRef)
            val mensaje = transaccion.get(mensajeRef)
            val lecturaActual = transaccion.get(lecturaRef)
            if (!familia.exists()) {
                throw ExcepcionChat(
                    CodigoErrorChat.FAMILIA_NO_DISPONIBLE) }
            if (!miembro.exists()) {
                throw ExcepcionChat(
                    CodigoErrorChat.SIN_ACCESO) }
            if (!mensaje.exists()) {
                throw ExcepcionChat(
                    CodigoErrorChat.MENSAJE_NO_DISPONIBLE)}
            val fechaMensaje = mensaje.getTimestamp(
                CAMPO_FECHA_ENVIO
            ) ?: throw ExcepcionChat(
                CodigoErrorChat.FECHA_NO_CONFIRMADA)
            val fechaAnterior = lecturaActual.getTimestamp(
                CAMPO_FECHA_ULTIMO_MENSAJE_LEIDO)
            val idAnterior = lecturaActual.getString(
                CAMPO_ULTIMO_MENSAJE_LEIDO_ID).orEmpty()

            val debeAvanzar = when {
                fechaAnterior == null -> true
                fechaMensaje > fechaAnterior -> true
                fechaMensaje == fechaAnterior &&
                        mensajeId > idAnterior -> true
                else -> false
            }
            if (debeAvanzar) {
                val datos = mapOf(
                    CAMPO_UID to uid,
                    CAMPO_ULTIMO_MENSAJE_LEIDO_ID to mensajeId,
                    CAMPO_FECHA_ULTIMO_MENSAJE_LEIDO to fechaMensaje)
                transaccion.set(lecturaRef, datos)
            }
            Unit
        }.addOnSuccessListener {
            alCompletar(Result.success(Unit))

        }.addOnFailureListener { error ->
            alCompletar(
                Result.failure(ExcepcionChat.desde(error))
            )
        }
    }

    private fun esIdValido(id: String): Boolean {
        return id.isNotBlank() &&
                !id.contains("/") &&
                id != "." &&
                id != ".."
    }

    companion object {
        private const val COLECCION_FAMILIAS = "familias"

        private const val SUBCOLECCION_MIEMBROS = "miembros"
        private const val SUBCOLECCION_MENSAJES = "mensajes"
        private const val SUBCOLECCION_LECTURAS = "lecturas"

        private const val CAMPO_UID = "uid"
        private const val CAMPO_FECHA_ENVIO = "fechaEnvio"
        private const val CAMPO_ULTIMO_MENSAJE_LEIDO_ID =
            "ultimoMensajeLeidoId"
        private const val CAMPO_FECHA_ULTIMO_MENSAJE_LEIDO =
            "fechaUltimoMensajeLeido"
    }
}