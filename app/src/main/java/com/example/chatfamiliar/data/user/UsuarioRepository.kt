package com.example.chatfamiliar.data.user

import com.example.chatfamiliar.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class UsuarioRepository {
    private val firestore =
        FirebaseFirestore.getInstance()
    private val usuarios =
        firestore.collection(
            COLECCION_USUARIOS
        )

    fun crearUsuario(
        uid: String,
        correo: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        val usuario = Usuario(uid = uid, nombre = "",
            correo = correo)

        usuarios
            .document(uid)
            .set(usuario)
            .addOnSuccessListener {
                alCompletar(Result.success(Unit))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }

    fun obtenerUsuario(
        uid: String,
        alCompletar: (Result<Usuario?>) -> Unit
    ) {
        usuarios
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->
                if (!documento.exists()) {
                    alCompletar(Result.success(null))
                    return@addOnSuccessListener
                }
                val usuario =
                    documento.toObject(Usuario::class.java)
                alCompletar(Result.success(usuario))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(
                    Result.failure(excepcion)
                )
            }
    }

    fun actualizarNombre(
        uid: String,
        nombre: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        usuarios
            .document(uid)
            .update(CAMPO_NOMBRE, nombre.trim())
            .addOnSuccessListener {
                alCompletar(Result.success(Unit))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)
                )
            }
    }

    companion object {
        private const val COLECCION_USUARIOS =
            "usuarios"
        private const val CAMPO_NOMBRE =
            "nombre"
    }
}