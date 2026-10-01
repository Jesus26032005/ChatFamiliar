package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.model.ConversacionPrivada
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ListadoPrivadosRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val conversaciones = firestore.collection(
        COLECCION_CONVERSACIONES_PRIVADAS
    )
    fun escucharConversaciones(
        alActualizar: (Result<List<ConversacionPrivada>>) -> Unit
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) { alActualizar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return null }

        return conversaciones
            .whereArrayContains(CAMPO_PARTICIPANTES, uid)
            .addSnapshotListener { resultado, error ->
                if (auth.currentUser?.uid != uid) {
                    alActualizar(Result.failure(
                            ExcepcionChat(
                                CodigoErrorChat.SESION_REQUERIDA)))
                    return@addSnapshotListener
                }

                if (error != null) { alActualizar(
                        Result.failure(ExcepcionChat.desde(error)))
                    return@addSnapshotListener
                }
                if (resultado == null) {
                    alActualizar(
                        Result.failure(
                            ExcepcionChat(
                                CodigoErrorChat.DATOS_INVALIDOS)))
                    return@addSnapshotListener
                }
                val lista = runCatching {
                    resultado.documents.map { documento ->
                        val conversacion = documento.toObject(
                            ConversacionPrivada::class.java
                        ) ?: throw ExcepcionChat(
                            CodigoErrorChat.DATOS_INVALIDOS)
                        val participantes = conversacion.participantes
                        if (participantes.size != 2 ||
                            participantes.distinct().size != 2 ||
                            participantes.any { it.isBlank() }) {
                            throw ExcepcionChat(
                                CodigoErrorChat.DATOS_INVALIDOS) }
                        if (uid !in participantes) {
                            throw ExcepcionChat(
                                CodigoErrorChat.SIN_ACCESO)
                        }
                        conversacion.copy(id = documento.id)
                    }.sortedWith(
                        compareByDescending<ConversacionPrivada> {
                            it.fechaUltimoMensaje ?: it.fechaCreacion
                        }.thenBy { it.id })
                }
                lista.onSuccess {
                    alActualizar(Result.success(it))
                }.onFailure { errorLectura ->
                    val errorChat = if (errorLectura is ExcepcionChat) {
                        errorLectura } else {
                        ExcepcionChat(
                            codigo = CodigoErrorChat.DATOS_INVALIDOS,
                            causa = errorLectura) }
                    alActualizar(Result.failure(errorChat))
                }
            }
    }
    companion object {
        private const val COLECCION_CONVERSACIONES_PRIVADAS =
            "conversacionesPrivadas"
        private const val CAMPO_PARTICIPANTES = "participantes"
    }
}