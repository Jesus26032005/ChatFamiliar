package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.model.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import java.util.UUID

class ChatFamiliaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val familias = firestore.collection(COLECCION_FAMILIAS)
    private val usuarios = firestore.collection(COLECCION_USUARIOS)

    fun generarMensajeId(): String {
        return UUID.randomUUID().toString() }

    fun escucharMensajesRecientes(
        familiaId: String,
        alActualizar: (Result<List<Mensaje>>) -> Unit
    ): ListenerRegistration? {
        val errorValidacion = when {
            auth.currentUser == null ->
                CodigoErrorChat.SESION_REQUERIDA
            !esIdValido(familiaId) ->
                CodigoErrorChat.IDENTIFICADOR_INVALIDO
            else -> null
        }
        if (errorValidacion != null) {
            alActualizar(
                Result.failure(ExcepcionChat(errorValidacion)))
            return null }
        return familias
            .document(familiaId)
            .collection(SUBCOLECCION_MENSAJES)
            .orderBy(
                CAMPO_FECHA_ENVIO,
                Query.Direction.ASCENDING)
            .orderBy(
                FieldPath.documentId(),
                Query.Direction.ASCENDING)
            .limitToLast(LIMITE_MENSAJES_RECIENTES)
            .addSnapshotListener(MetadataChanges.INCLUDE) { resultado, error ->
                if (error != null) {
                    alActualizar(
                        Result.failure(ExcepcionChat.desde(error)))
                    return@addSnapshotListener }
                if (resultado == null) {
                    alActualizar(
                        Result.failure(
                            ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)))
                    return@addSnapshotListener }
                val mensajes = runCatching {
                    resultado.documents.map { documento ->
                        val mensaje = documento.toObject(
                            Mensaje::class.java
                        ) ?: throw ExcepcionChat(
                            CodigoErrorChat.DATOS_INVALIDOS)
                        mensaje.copy(id = documento.id) }
                }

                mensajes.onSuccess {
                    alActualizar(Result.success(it))
                }.onFailure {
                    alActualizar(
                        Result.failure(
                            ExcepcionChat(
                                codigo = CodigoErrorChat.DATOS_INVALIDOS,
                                causa = it))
                    )
                }
            }
    }

    fun enviarMensaje(familiaId: String, mensajeId: String,
        contenido: String, alCompletar: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        val texto = contenido.trim()

        if (uid.isNullOrBlank()) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return
        }

        val errorValidacion = when {
            !esIdValido(familiaId) || !esIdValido(mensajeId) ->
                CodigoErrorChat.IDENTIFICADOR_INVALIDO
            texto.isEmpty() ->
                CodigoErrorChat.MENSAJE_VACIO
            texto.length > MAXIMO_CARACTERES ->
                CodigoErrorChat.MENSAJE_DEMASIADO_LARGO

            else -> null
        }

        if (errorValidacion != null) {
            alCompletar(
                Result.failure(ExcepcionChat(errorValidacion)))
            return
        }

        val familiaRef = familias.document(familiaId)
        val miembroRef = familiaRef
            .collection(SUBCOLECCION_MIEMBROS)
            .document(uid)

        val usuarioRef = usuarios.document(uid)
        val mensajeRef = familiaRef
            .collection(SUBCOLECCION_MENSAJES)
            .document(mensajeId)

        firestore.runTransaction { transaccion ->
            val familia = transaccion.get(familiaRef)
            val miembro = transaccion.get(miembroRef)
            val usuario = transaccion.get(usuarioRef)
            val mensajeExistente = transaccion.get(mensajeRef)

            if (!familia.exists()) {
                throw ExcepcionChat(
                    CodigoErrorChat.FAMILIA_NO_DISPONIBLE) }
            if (!miembro.exists()) { throw ExcepcionChat(
                    CodigoErrorChat.SIN_ACCESO) }
            if (!usuario.exists()) { throw ExcepcionChat(
                    CodigoErrorChat.PERFIL_NO_DISPONIBLE) }
            val nombre = usuario
                .getString(CAMPO_NOMBRE)
                .orEmpty().trim()
            if (nombre.isEmpty()) { throw ExcepcionChat(
                    CodigoErrorChat.NOMBRE_REQUERIDO) }
            if (mensajeExistente.exists()) {
                val mismoRemitente = mensajeExistente.getString(
                    CAMPO_REMITENTE_ID
                ) == uid
                val mismoContenido = mensajeExistente.getString(
                    CAMPO_CONTENIDO
                ) == texto
                if (!mismoRemitente || !mismoContenido) {
                    throw ExcepcionChat(
                        CodigoErrorChat.CONFLICTO_MENSAJE)
                }
            } else {
                val datos = mapOf(
                    CAMPO_REMITENTE_ID to uid,
                    CAMPO_NOMBRE_REMITENTE to nombre,
                    CAMPO_CONTENIDO to texto,
                    CAMPO_FECHA_ENVIO to FieldValue.serverTimestamp()
                )
                transaccion.set(mensajeRef, datos)
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
        const val MAXIMO_CARACTERES = 2000

        private const val LIMITE_MENSAJES_RECIENTES = 50L
        private const val COLECCION_FAMILIAS = "familias"
        private const val COLECCION_USUARIOS = "usuarios"
        private const val SUBCOLECCION_MIEMBROS = "miembros"
        private const val SUBCOLECCION_MENSAJES = "mensajes"
        private const val CAMPO_NOMBRE = "nombre"
        private const val CAMPO_REMITENTE_ID = "remitenteId"
        private const val CAMPO_NOMBRE_REMITENTE = "nombreRemitente"
        private const val CAMPO_CONTENIDO = "contenido"
        private const val CAMPO_FECHA_ENVIO = "fechaEnvio"
    }
}