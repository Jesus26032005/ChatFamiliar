package com.example.chatfamiliar.data.family

import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.MiembroFamilia
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class FamiliaRepository {
    private val firestore =
        FirebaseFirestore.getInstance()
    private val familias =
        firestore.collection(
            COLECCION_FAMILIAS
        )

    private val codigosFamilia = firestore.collection(COLECCION_CODIGOS)

    // Crear familia
    fun crearFamilia(
        nombre: String,
        uidCreador: String,
        alCompletar: (Result<Familia>) -> Unit
    ) {
        val nombreLimpio = nombre.trim()
        if (nombreLimpio.isBlank()) {
            alCompletar(
                Result.failure(IllegalArgumentException(
                        "El nombre de la familia no puede estar vacío.")
                ))
            return
        }

        if (uidCreador.isBlank()) {
            alCompletar(
                Result.failure(IllegalArgumentException(
                        "No se encontró el usuario creador.")
                ))
            return
        }

        val familiaRef = familias.document()
        val familiaId = familiaRef.id
        val codigoInvitacion = generarCodigoInvitacion(familiaId)
        val familia = Familia(id = familiaId,
                nombre = nombreLimpio,
                codigoInvitacion = codigoInvitacion,
                creadoPor = uidCreador)
        val miembroAdministrador =
            MiembroFamilia(
                uid = uidCreador,
                rol = MiembroFamilia.ROL_ADMINISTRADOR,
                codigoUnion = codigoInvitacion)
        val miembroRef =
            familiaRef
                .collection(SUBCOLECCION_MIEMBROS)
                .document(uidCreador)
        val codigoRef = codigosFamilia
            .document(codigoInvitacion)

        val batch = firestore.batch()
        batch.set(familiaRef, familia)
        batch.set(miembroRef, miembroAdministrador)
        batch.set(codigoRef,
            mapOf(CAMPO_FAMILIA_ID to familiaId))

        batch.commit()
            .addOnSuccessListener {
                Log.d(
                    "FamiliaRepository",
                    "Familia creada correctamente: ${familia.id}"
                )
                alCompletar(Result.success(familia))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))

                Log.e(
                    "FamiliaRepository",
                    "ERROR AL CREAR FAMILIA",
                    excepcion
                )
            }
    }


    // Unirse a familia
    fun unirseAFamilia(
        codigoInvitacion: String,
        uidUsuario: String,
        alCompletar: (Result<Familia>) -> Unit
    ) {
        val codigoLimpio = codigoInvitacion.trim().uppercase()
        if (codigoLimpio.isBlank()) {
            alCompletar(
                Result.failure(
                    IllegalArgumentException(
                        "Ingresa un código de invitación.")))
            return
        }

        if (uidUsuario.isBlank()) {
            alCompletar(
                Result.failure(
                    IllegalArgumentException(
                        "No se encontró el usuario actual.")))
            return
        }
        codigosFamilia
            .document(codigoLimpio)
            .get()
            .addOnSuccessListener { documentoCodigo ->
                if (!documentoCodigo.exists()) {
                    alCompletar(
                        Result.failure(
                            IllegalArgumentException(
                                "El código de invitación no existe.")))
                    return@addOnSuccessListener
                }


                val familiaId =
                    documentoCodigo.getString(CAMPO_FAMILIA_ID)

                if (familiaId.isNullOrBlank()) {
                    alCompletar(
                        Result.failure(
                            IllegalStateException(
                                "El código no está asociado a una familia.")))
                    return@addOnSuccessListener
                }

                val familiaRef = familias.document(familiaId)
                val miembroRef = familiaRef
                        .collection(SUBCOLECCION_MIEMBROS)
                        .document(uidUsuario)
                miembroRef
                    .get()
                    .addOnSuccessListener { documentoMiembro ->
                        if (documentoMiembro.exists()) {
                            familiaRef
                                .get()
                                .addOnSuccessListener { documentoFamilia ->
                                    val familia =
                                        documentoFamilia.toObject(Familia::class.java)
                                    if (familia != null) {
                                        alCompletar(Result.success(familia))
                                    } else {
                                        alCompletar(
                                            Result.failure(
                                                IllegalStateException(
                                                    "La familia ya no existe.")
                                            )
                                        )
                                    }
                                }
                                .addOnFailureListener { excepcion ->
                                    alCompletar(Result.failure(excepcion))
                                }
                            return@addOnSuccessListener
                        }
                        val nuevoMiembro =
                            MiembroFamilia(
                                uid = uidUsuario,
                                rol = MiembroFamilia.ROL_MIEMBRO,
                                codigoUnion = codigoLimpio
                            )
                        miembroRef
                            .set(nuevoMiembro)
                            .addOnSuccessListener {
                                familiaRef
                                    .get()
                                    .addOnSuccessListener { documentoFamilia ->
                                        val familia = documentoFamilia.toObject(
                                                Familia::class.java)

                                        if (familia != null) {
                                            alCompletar(Result.success(familia))
                                        } else {
                                            alCompletar(
                                                Result.failure(
                                                    IllegalStateException(
                                                        "La familia ya no existe.")))
                                        }
                                    }
                                    .addOnFailureListener { excepcion ->
                                        alCompletar(Result.failure(
                                            excepcion))
                                    }
                            }
                            .addOnFailureListener { excepcion ->
                                alCompletar(
                                    Result.failure(excepcion))
                            }
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


    // Obtener famila
    fun obtenerFamilia(familiaId: String,
                       alCompletar: (Result<Familia?>) -> Unit
    ) {
        familias
            .document(familiaId)
            .get()
            .addOnSuccessListener { documento ->
                val familia = documento.toObject(Familia::class.java)
                alCompletar(
                    Result.success(familia))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion)
                )
            }
    }


    // Obtener membresia
    fun obtenerMembresia(
        familiaId: String,
        uidUsuario: String,
        alCompletar: (Result<MiembroFamilia?>) -> Unit
    ) {
        familias
            .document(familiaId)
            .collection(SUBCOLECCION_MIEMBROS)
            .document(uidUsuario)
            .get()
            .addOnSuccessListener { documento ->
                val miembro =
                    documento.toObject(MiembroFamilia::class.java)
                alCompletar(Result.success(miembro)
                )
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }


    // Obtener todas las familias donde esta el usuari0
    fun obtenerFamiliasDelUsuario(
        uidUsuario: String,
        alCompletar: (Result<List<Familia>>) -> Unit
    ) {
        firestore.collectionGroup(SUBCOLECCION_MIEMBROS)
            .whereEqualTo(CAMPO_UID, uidUsuario)
            .get()
            .addOnSuccessListener { resultadoMiembros ->
                val tareasFamilias =
                    resultadoMiembros
                        .documents
                        .mapNotNull { documentoMiembro ->
                            documentoMiembro.reference
                                .parent.parent?.get()
                        }
                if (tareasFamilias.isEmpty()) {
                    alCompletar(Result.success(emptyList()))
                    return@addOnSuccessListener
                }

                Tasks
                    .whenAllSuccess<DocumentSnapshot>(tareasFamilias)
                    .addOnSuccessListener { documentosFamilia ->
                        val familiasUsuario =
                            documentosFamilia.mapNotNull {
                                documento ->
                                    documento.toObject(
                                        Familia::class.java)
                                }
                        alCompletar(Result.success(
                            familiasUsuario))
                    }
                    .addOnFailureListener { excepcion ->
                        alCompletar(Result
                            .failure(excepcion)
                        )
                    }
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }


    // Editar familia
    fun editarFamilia(
        familiaId: String,
        nuevoNombre: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        val nombreLimpio = nuevoNombre.trim()
        if (nombreLimpio.isBlank()) {
            alCompletar(
                Result.failure(
                    IllegalArgumentException(
                        "El nombre no puede estar vacío.")))
            return
        }

        familias
            .document(familiaId)
            .update(CAMPO_NOMBRE, nombreLimpio)
            .addOnSuccessListener {
                alCompletar(
                    Result.success(Unit))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(
                    Result.failure(excepcion))
            }
    }


    // Un usuario puede abandonar, pero un ultimo administrador no
    fun abandonarFamilia(
        familiaId: String,
        uidUsuario: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        val miembros =
            familias
                .document(familiaId)
                .collection(SUBCOLECCION_MIEMBROS)

        val miembroRef = miembros.document(uidUsuario)

        miembroRef
            .get()
            .addOnSuccessListener { documentoMiembro ->
                val miembro = documentoMiembro
                        .toObject(MiembroFamilia::class.java)

                if (miembro == null) {
                    alCompletar(
                        Result.failure(
                            IllegalStateException(
                                "El usuario no pertenece a esta familia.")))
                    return@addOnSuccessListener
                }


                // Sino es admin se sale facil
                if (
                    miembro.rol != MiembroFamilia.ROL_ADMINISTRADOR
                ) {
                    eliminarMembresia(
                        miembroRef = miembroRef,
                        alCompletar = alCompletar)
                    return@addOnSuccessListener
                }
                // Si es admin se checa numero de admins
                miembros
                    .whereEqualTo(CAMPO_ROL,
                        MiembroFamilia.ROL_ADMINISTRADOR)
                    .get()
                    .addOnSuccessListener { administradores ->
                        if (administradores.size() <= 1) {
                            alCompletar(
                                Result.failure(
                                    IllegalStateException(
                                        "Debes asignar otro administrador antes de abandonar la familia.")))
                            return@addOnSuccessListener
                        }

                        eliminarMembresia(miembroRef = miembroRef,
                            alCompletar = alCompletar)
                    }

                    .addOnFailureListener { excepcion ->
                        alCompletar(Result.failure(excepcion))
                    }
            }

            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }

    private fun eliminarMembresia(
        miembroRef: DocumentReference,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        miembroRef.delete()
            .addOnSuccessListener {
                alCompletar(Result.success(Unit))
            }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }


    private fun generarCodigoInvitacion(familiaId: String): String {
        return "FAM-${familiaId.take(6).uppercase()}"
    }

    fun transferirAdministracionYAbandonar(familiaId: String,
        uidAdministradorActual: String,
        uidNuevoAdministrador: String,
        alCompletar: (Result<Unit>) -> Unit
    ) {
        val familiaRef = familias.document(familiaId)
        val miembroActualRef =
            familiaRef.collection(SUBCOLECCION_MIEMBROS)
                .document(uidAdministradorActual)
        val nuevoAdministradorRef = familiaRef
            .collection(SUBCOLECCION_MIEMBROS)
            .document(uidNuevoAdministrador)
        firestore.runTransaction { transaction ->
                val documentoFamilia = transaction.get(familiaRef)
                val documentoActual = transaction.get(miembroActualRef)
                val documentoNuevo = transaction.get(nuevoAdministradorRef)
                val familia = documentoFamilia.toObject(
                    Familia::class.java)
                        ?: throw IllegalStateException("La familia no existe.")
                val miembroActual =
                    documentoActual.toObject(MiembroFamilia::class.java
                    ) ?: throw IllegalStateException(
                        "El administrador actual no pertenece a la familia.")
                val nuevoAdministrador =
                    documentoNuevo.toObject(MiembroFamilia::class.java)
                        ?: throw IllegalStateException(
                            "El nuevo administrador no pertenece a la familia.")
                if (familia.creadoPor != uidAdministradorActual) {
                    throw IllegalStateException(
                        "El usuario no es el administrador principal.")
                }
                if (miembroActual.rol != MiembroFamilia.ROL_ADMINISTRADOR) {
                    throw IllegalStateException(
                        "El administrador principal no tiene un rol válido.")
                }
                if (uidAdministradorActual == uidNuevoAdministrador) {
                    throw IllegalStateException(
                        "El nuevo administrador debe ser otro integrante."
                    )
                }
                if (nuevoAdministrador.rol != MiembroFamilia.ROL_ADMINISTRADOR
                ) { transaction.update(nuevoAdministradorRef,
                        CAMPO_ROL, MiembroFamilia.ROL_ADMINISTRADOR)
                }

                transaction.update(familiaRef, CAMPO_CREADO_POR, uidNuevoAdministrador)
                transaction.delete(miembroActualRef)
            Unit
            }
            .addOnSuccessListener {
                alCompletar(Result.success(Unit)) }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }


    fun eliminarFamiliaCompleta(
    familiaId: String,
    uidAdministradorPrincipal: String,
    alCompletar: (Result<Unit>) -> Unit
    ) {
        val familiaRef =
            familias.document(familiaId)
        familiaRef.get()
            .addOnSuccessListener { documentoFamilia ->
                val familia =
                    documentoFamilia.toObject(Familia::class.java)
                if (familia == null) {
                    alCompletar(Result.failure(
                            IllegalStateException("La familia no existe.")
                        )
                    )
                    return@addOnSuccessListener
                }


                if (familia.creadoPor != uidAdministradorPrincipal) {
                    alCompletar(
                        Result.failure(IllegalStateException(
                            "Solo el administrador principal puede eliminar" +
                                    " la familia."
                            )))
                    return@addOnSuccessListener
                }

                familiaRef
                    .collection(SUBCOLECCION_MIEMBROS)
                    .get()
                    .addOnSuccessListener { resultadoMiembros ->
                        if (resultadoMiembros.size() > 498) {
                            alCompletar(
                                Result.failure(
                                    IllegalStateException(
                                        "La familia contiene demasiados miembros" +
                                                " para eliminarse desde esta operación.")
                                )
                            )
                            return@addOnSuccessListener
                        }
                        val batch = firestore.batch()
                        resultadoMiembros.documents.forEach { documento ->
                                batch.delete(documento.reference) }
                        val codigoRef = codigosFamilia.document(
                            familia.codigoInvitacion)
                        batch.delete(codigoRef)
                        batch.delete(familiaRef)
                        batch.commit()
                            .addOnSuccessListener {
                                alCompletar(Result.success(Unit))
                            }
                            .addOnFailureListener { excepcion ->
                                alCompletar(Result.failure(excepcion)) }
                    }
                    .addOnFailureListener { excepcion ->
                        alCompletar(Result.failure(excepcion))
                    } }
            .addOnFailureListener { excepcion ->
                alCompletar(Result.failure(excepcion))
            }
    }


companion object {
    private const val COLECCION_FAMILIAS = "familias"
    private const val COLECCION_CODIGOS = "codigosFamilia"
    private const val SUBCOLECCION_MIEMBROS = "miembros"
    private const val CAMPO_FAMILIA_ID = "familiaId"
    private const val CAMPO_UID = "uid"
    private const val CAMPO_NOMBRE = "nombre"
    private const val CAMPO_ROL = "rol"
    private const val CAMPO_CREADO_POR = "creadoPor"
}
}