package com.example.chatfamiliar.data.family

import com.example.chatfamiliar.model.MiembroFamilia
import com.example.chatfamiliar.model.MiembroFamiliaDetalle
import com.example.chatfamiliar.model.Usuario
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MiembroFamiliaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val familias = firestore.collection(COLECCION_FAMILIAS)
    private val usuarios =
        firestore.collection(COLECCION_USUARIOS)

    fun obtenerMiembrosFamilia(
        familiaId: String,
        alCompletar:
            (Result<List<MiembroFamiliaDetalle>>) -> Unit
    ) {
        familias
            .document(familiaId)
            .collection(SUBCOLECCION_MIEMBROS)
            .get()
            .addOnSuccessListener { resultadoMiembros ->
                val membresias =
                    resultadoMiembros
                        .documents
                        .mapNotNull { documento ->
                            documento.toObject(
                                MiembroFamilia::class.java)
                        }


                if (membresias.isEmpty()) {
                    alCompletar(Result.success(emptyList()))
                    return@addOnSuccessListener
                }


                val tareasUsuarios =
                    membresias.map { membresia ->
                        usuarios.document(membresia.uid).get()
                    }

                Tasks
                    .whenAllSuccess<DocumentSnapshot>(tareasUsuarios)
                    .addOnSuccessListener { documentos ->
                        val usuariosPorUid =
                            documentos
                                .mapNotNull { documento ->
                                    documento.toObject(
                                        Usuario::class.java
                                    )
                                }
                                .associateBy { it.uid
                                }

                        val miembros = membresias
                                .map { membresia ->
                                    val usuario = usuariosPorUid[membresia.uid]
                                    MiembroFamiliaDetalle(
                                        uid = membresia.uid,
                                        nombre = usuario?.nombre.orEmpty(),
                                        correo = usuario?.correo.orEmpty(),
                                        rol = membresia.rol)
                                }
                                .sortedWith(
                                    compareByDescending
                                    <MiembroFamiliaDetalle> {
                                        it.esAdministrador
                                    }.thenBy {
                                        it.nombre.lowercase()
                                    }
                                )

                        alCompletar(Result.success(miembros))
                    }

                    .addOnFailureListener { excepcion ->
                        alCompletar(Result.failure(excepcion))
                    }
            }

            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)
                )
            }
    }


    fun hacerAdministrador(
        familiaId: String,
        uidMiembro: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        actualizarRol(familiaId = familiaId,
            uidMiembro = uidMiembro,
            nuevoRol = MiembroFamilia.ROL_ADMINISTRADOR,
            alCompletar = alCompletar)
    }

    fun quitarAdministrador(
        familiaId: String,
        uidMiembro: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        actualizarRol(
            familiaId = familiaId,
            uidMiembro = uidMiembro,
            nuevoRol = MiembroFamilia.ROL_MIEMBRO,
            alCompletar = alCompletar)
    }

    private fun actualizarRol(
        familiaId: String,
        uidMiembro: String,
        nuevoRol: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {

        familias
            .document(familiaId)
            .collection(SUBCOLECCION_MIEMBROS)
            .document(uidMiembro)

            .update(CAMPO_ROL, nuevoRol)
            .addOnSuccessListener {
                alCompletar(Result.success(Unit))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)
                )
            }
    }

    fun expulsarMiembro(
        familiaId: String,
        uidMiembro: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {

        familias
            .document(familiaId)
            .collection(SUBCOLECCION_MIEMBROS)
            .document(uidMiembro)
            .delete()
            .addOnSuccessListener {
                alCompletar(Result.success(Unit))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }


    companion object {
        private const val COLECCION_FAMILIAS =
            "familias"
        private const val COLECCION_USUARIOS =
            "usuarios"
        private const val SUBCOLECCION_MIEMBROS =
            "miembros"
        private const val CAMPO_ROL =
            "rol"
    }
}