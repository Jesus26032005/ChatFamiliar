package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.model.LecturaConversacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore

class MensajesNoLeidosRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    fun contarFamilia(
        familiaId: String,
        lectura: LecturaConversacion,
        alCompletar: (Result<Int>) -> Unit
    ) {
        contar(coleccion = COLECCION_FAMILIAS,
            conversacionId = familiaId,
            lectura = lectura,
            alCompletar = alCompletar)
    }
    fun contarPrivado(conversacionId: String,
        lectura: LecturaConversacion,
        alCompletar: (Result<Int>) -> Unit) {
        contar(coleccion = COLECCION_CONVERSACIONES_PRIVADAS,
            conversacionId = conversacionId,
            lectura = lectura,
            alCompletar = alCompletar)
    }

    private fun contar(coleccion: String, conversacionId: String,
        lectura: LecturaConversacion,
        alCompletar: (Result<Int>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)
                ))
            return
        }
        if (!esIdValido(conversacionId)) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)
                ))
            return
        }
        if (lectura.uid != uid) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)))
            return }
        val fechaLeida = lectura.fechaUltimoMensajeLeido
        val mensajeLeidoId = lectura.ultimoMensajeLeidoId
        val sinLectura = fechaLeida == null && mensajeLeidoId.isEmpty()
        if (!sinLectura &&
            (fechaLeida == null || !esIdValido(mensajeLeidoId))) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)))
            return
        }
        val mensajes = firestore
            .collection(coleccion)
            .document(conversacionId)
            .collection(SUBCOLECCION_MENSAJES)
        val filtroOtrosRemitentes = Filter.notEqualTo(
            CAMPO_REMITENTE_ID,
            uid)
        val consulta = if (sinLectura) {
            mensajes.where(filtroOtrosRemitentes)
        } else {
            val filtroPosteriores = Filter.or(
                Filter.greaterThan(
                    CAMPO_FECHA_ENVIO,
                    fechaLeida),
                Filter.and(
                    Filter.equalTo(
                        CAMPO_FECHA_ENVIO,
                        fechaLeida),
                    Filter.greaterThan(
                        FieldPath.documentId(),
                        mensajeLeidoId
                    )))
            mensajes.where(Filter.and(
                    filtroOtrosRemitentes,
                    filtroPosteriores))
        }
        consulta
            .count()
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { resultado ->
                if (auth.currentUser?.uid != uid) {
                    alCompletar(Result.failure(
                            ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
                    return@addOnSuccessListener
                }
                val cantidad = resultado.count
                if (cantidad < 0L || cantidad > Int.MAX_VALUE.toLong()) {
                    alCompletar(Result.failure(
                            ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)))
                    return@addOnSuccessListener }
                alCompletar(Result.success(cantidad.toInt()))
            }.addOnFailureListener { error ->
                alCompletar(
                    Result.failure(ExcepcionChat.desde(error)))
            }
    }

    private fun esIdValido(id: String): Boolean {
        return id.isNotBlank() && !id.contains("/") &&
                id != "." && id != ".." }
    companion object {
        private const val COLECCION_FAMILIAS = "familias"
        private const val COLECCION_CONVERSACIONES_PRIVADAS =
            "conversacionesPrivadas"
        private const val SUBCOLECCION_MENSAJES = "mensajes"
        private const val CAMPO_REMITENTE_ID = "remitenteId"
        private const val CAMPO_FECHA_ENVIO = "fechaEnvio"
    }
}