package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.model.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
class HistorialFamiliaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val familias = firestore.collection(COLECCION_FAMILIAS)
    fun cargarMensajesAnteriores(familiaId: String,
        mensajeMasAntiguo: Mensaje,
        alCompletar: (Result<List<Mensaje>>) -> Unit) {
        if (auth.currentUser == null) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return }
        if (!esIdValido(familiaId) ||
            !esIdValido(mensajeMasAntiguo.id)) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)))
            return }
        val fechaLimite = mensajeMasAntiguo.fechaEnvio
        if (fechaLimite == null) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.FECHA_NO_CONFIRMADA)))
            return }
        familias
            .document(familiaId)
            .collection(SUBCOLECCION_MENSAJES)
            .orderBy(CAMPO_FECHA_ENVIO,
                Query.Direction.ASCENDING)
            .orderBy(FieldPath.documentId(),
                Query.Direction.ASCENDING)
            .endBefore(fechaLimite,
                mensajeMasAntiguo.id)
            .limitToLast(TAMANO_PAGINA.toLong())
            .get(Source.SERVER)
            .addOnSuccessListener { resultado ->
                val mensajes = runCatching {
                    resultado.documents.map { documento ->
                        val mensaje = documento.toObject(
                            Mensaje::class.java
                        ) ?: throw ExcepcionChat(
                            CodigoErrorChat.DATOS_INVALIDOS)
                        if (mensaje.fechaEnvio == null) {
                            throw ExcepcionChat(
                                CodigoErrorChat.DATOS_INVALIDOS)
                        }
                        mensaje.copy(id = documento.id) } }
                mensajes.onSuccess {
                    alCompletar(Result.success(it)) }.onFailure {
                    alCompletar(
                        Result.failure(
                            ExcepcionChat(
                                codigo = CodigoErrorChat.DATOS_INVALIDOS,
                                causa = it))) }
            }.addOnFailureListener { error ->
                alCompletar(
                    Result.failure(ExcepcionChat.desde(error))) }
    }
    private fun esIdValido(id: String): Boolean {
        return id.isNotBlank() && !id.contains("/") &&
                id != "." && id != ".." }
    companion object {
        const val TAMANO_PAGINA = 50
        private const val COLECCION_FAMILIAS = "familias"
        private const val SUBCOLECCION_MENSAJES = "mensajes"
        private const val CAMPO_FECHA_ENVIO = "fechaEnvio"
    }
}