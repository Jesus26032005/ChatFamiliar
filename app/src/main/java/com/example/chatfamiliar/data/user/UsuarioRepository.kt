package com.example.chatfamiliar.data.user

import com.example.chatfamiliar.data.chat.CodigoErrorChat
import com.example.chatfamiliar.data.chat.ExcepcionChat
import com.example.chatfamiliar.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UsuarioRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usuarios = firestore.collection(COLECCION_USUARIOS)
    fun crearUsuario(
        uid: String,
        correo: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        val usuario = Usuario(uid = uid,
            nombre = "", correo = correo)
        usuarios.document(uid)
            .set(usuario)
            .addOnSuccessListener {
                alCompletar(Result.success(Unit)) }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)) }
    }

    fun obtenerUsuario(uid: String,
        alCompletar: (Result<Usuario?>) -> Unit) {
        usuarios.document(uid)
            .get()
            .addOnSuccessListener { documento ->
                if (!documento.exists()) {
                    alCompletar(Result.success(null))
                    return@addOnSuccessListener }
                val usuario = documento.toObject(
                    Usuario::class.java)
                alCompletar(Result.success(usuario)) }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)) }
    }

    fun actualizarNombre(
        uid: String,
        nombre: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        usuarios.document(uid)
            .update(CAMPO_NOMBRE, nombre.trim())
            .addOnSuccessListener {
                alCompletar(Result.success(Unit)) }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)) }
    }
    fun escucharUsuario(uidUsuario: String,
        alActualizar: (Result<Usuario?>) -> Unit
    ): ListenerRegistration? {
        val uidSesion = auth.currentUser?.uid
        if (uidSesion.isNullOrBlank()) {
            alActualizar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return null
        }
        if (!esIdValido(uidUsuario)) {
            alActualizar(
                Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)
                ))
            return null
        }
        return usuarios
            .document(uidUsuario)
            .addSnapshotListener { documento, error ->
                if (auth.currentUser?.uid != uidSesion) {
                    alActualizar(
                        Result.failure(
                            ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)
                        ))
                    return@addSnapshotListener }
                if (error != null) {
                    alActualizar(Result.failure(ExcepcionChat.desde(error)))
                    return@addSnapshotListener
                }
                if (documento == null) {
                    alActualizar(
                        Result.failure(
                            ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)))
                    return@addSnapshotListener
                }
                if (!documento.exists()) {
                    alActualizar(Result.success(null))
                    return@addSnapshotListener }
                val resultado = runCatching {
                    val usuario = documento.toObject(
                        Usuario::class.java
                    ) ?: throw ExcepcionChat(
                        CodigoErrorChat.DATOS_INVALIDOS)
                    usuario.copy(uid = documento.id)
                }
                resultado.onSuccess {
                    alActualizar(Result.success(it))

                }.onFailure { errorLectura ->
                    alActualizar(
                        Result.failure(
                            ExcepcionChat(
                                codigo = CodigoErrorChat.DATOS_INVALIDOS,
                                causa = errorLectura
                            )
                        )
                    )
                }
            }
    }
    private fun esIdValido(id: String): Boolean {
        return id.isNotBlank() &&
                !id.contains("/") &&
                id != "." &&
                id != ".."
    }
    companion object {
        private const val COLECCION_USUARIOS = "usuarios"
        private const val CAMPO_NOMBRE = "nombre"
    }
}