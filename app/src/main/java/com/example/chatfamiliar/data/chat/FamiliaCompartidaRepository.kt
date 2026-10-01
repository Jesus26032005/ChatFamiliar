package com.example.chatfamiliar.data.chat

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class FamiliaCompartidaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val familias = firestore.collection(COLECCION_FAMILIAS)
    fun buscarFamiliaCompartida(otroUsuarioId: String,
        familiaPreferidaId: String? = null,
        alCompletar: (Result<String?>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return }
        if (!esIdValido(otroUsuarioId) || otroUsuarioId == uid) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)))
            return
        }

        firestore
            .collectionGroup(SUBCOLECCION_MIEMBROS)
            .whereEqualTo(CAMPO_UID, uid)
            .get(Source.SERVER)
            .addOnSuccessListener { resultado ->
                if (auth.currentUser?.uid != uid) {
                    alCompletar(Result.failure(
                            ExcepcionChat(
                                CodigoErrorChat.SESION_REQUERIDA)))
                    return@addOnSuccessListener
                }
                val familiasDelUsuario = resultado.documents
                    .mapNotNull { documento ->
                        documento.reference.parent.parent }
                    .filter { familiaRef ->
                        familiaRef.parent.path == COLECCION_FAMILIAS }
                    .map { familiaRef ->
                        familiaRef.id }
                    .distinct()
                    .sortedWith(
                        compareByDescending<String> {
                            it == familiaPreferidaId
                        }.thenBy { it })
                comprobarFamilias(uid = uid,
                    otroUsuarioId = otroUsuarioId,
                    familiasIds = familiasDelUsuario,
                    indice = 0, primerError = null,
                    alCompletar = alCompletar)
            }.addOnFailureListener { error ->
                alCompletar(
                    Result.failure(ExcepcionChat.desde(error)))
            }
    }

    private fun comprobarFamilias(uid: String, otroUsuarioId: String,
        familiasIds: List<String>, indice: Int,
        primerError: Throwable?,
        alCompletar: (Result<String?>) -> Unit) {
        if (auth.currentUser?.uid != uid) {
            alCompletar(Result.failure(
                    ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return
        }
        if (indice >= familiasIds.size) {
            if (primerError != null) {
                alCompletar(Result.failure(
                        ExcepcionChat.desde(primerError)))
            } else {
                alCompletar(Result.success(null))
            }
            return
        }
        val familiaId = familiasIds[indice]
        val familiaRef = familias.document(familiaId)

        val otroMiembroRef = familiaRef
            .collection(SUBCOLECCION_MIEMBROS)
            .document(otroUsuarioId)

        val tareas = listOf(familiaRef.get(Source.SERVER),
            otroMiembroRef.get(Source.SERVER))

        Tasks.whenAllSuccess<DocumentSnapshot>(tareas)
            .addOnSuccessListener { documentos ->
                if (auth.currentUser?.uid != uid) {
                    alCompletar(
                        Result.failure(
                            ExcepcionChat(
                                CodigoErrorChat.SESION_REQUERIDA))
                    )
                    return@addOnSuccessListener
                }
                val familia = documentos[0]
                val otroMiembro = documentos[1]
                if (familia.exists() && otroMiembro.exists()) {
                    alCompletar(Result.success(familiaId))
                } else {
                    comprobarFamilias(uid = uid,
                        otroUsuarioId = otroUsuarioId,
                        familiasIds = familiasIds,
                        indice = indice + 1,
                        primerError = primerError,
                        alCompletar = alCompletar) }
            }.addOnFailureListener { error ->
                comprobarFamilias(
                    uid = uid, otroUsuarioId = otroUsuarioId,
                    familiasIds = familiasIds, indice = indice + 1,
                    primerError = primerError ?: error,
                    alCompletar = alCompletar)
            }
    }
    private fun esIdValido(id: String): Boolean {
        return id.isNotBlank() &&
                !id.contains("/") && id != "." && id != ".." }

    companion object {
        private const val COLECCION_FAMILIAS = "familias"
        private const val SUBCOLECCION_MIEMBROS = "miembros"
        private const val CAMPO_UID = "uid"
    }
}