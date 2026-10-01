package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.model.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query

class UltimoMensajeFamiliaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val familias = firestore.collection(COLECCION_FAMILIAS)
    fun escucharUltimoMensaje(
        familiaId: String,
        alActualizar: (Result<Mensaje?>) -> Unit
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            alActualizar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)
                ))
            return null
        }
        if (!esIdValido(familiaId)) {
            alActualizar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)
                ))
            return null
        }
        return familias
            .document(familiaId)
            .collection(SUBCOLECCION_MENSAJES)
            .orderBy(
                CAMPO_FECHA_ENVIO,
                Query.Direction.DESCENDING)
            .orderBy(
                FieldPath.documentId(),
                Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener(MetadataChanges.INCLUDE) { resultado, error ->
                if (auth.currentUser?.uid != uid) {
                    alActualizar(Result.failure(
                            ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
                    return@addSnapshotListener }
                if (error != null) { alActualizar(
                        Result.failure(ExcepcionChat.desde(error)))
                    return@addSnapshotListener }
                if (resultado == null) {
                    alActualizar(Result.failure(
                            ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)
                        ))
                    return@addSnapshotListener
                }
                // Espera confirmación del servidor antes de usar el
                // resultado para actualizar la vista previa y el conteo.
                if (resultado.metadata.isFromCache ||
                    resultado.metadata.hasPendingWrites()) {
                    return@addSnapshotListener }
                val documento = resultado.documents.firstOrNull()
                if (documento == null) {
                    alActualizar(Result.success(null))
                    return@addSnapshotListener }
                val resultadoMensaje = runCatching {
                    val mensaje = documento.toObject(
                        Mensaje::class.java
                    ) ?: throw ExcepcionChat(
                        CodigoErrorChat.DATOS_INVALIDOS)
                    if (mensaje.fechaEnvio == null) {
                        throw ExcepcionChat(
                            CodigoErrorChat.FECHA_NO_CONFIRMADA) }
                    mensaje.copy(id = documento.id)
                }
                resultadoMensaje.onSuccess {
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

    private fun esIdValido(id: String): Boolean {
        return id.isNotBlank() && !id.contains("/") &&
                id != "." && id != ".." }
    companion object {
        private const val COLECCION_FAMILIAS = "familias"
        private const val SUBCOLECCION_MENSAJES = "mensajes"

        private const val CAMPO_FECHA_ENVIO = "fechaEnvio"
    }
}