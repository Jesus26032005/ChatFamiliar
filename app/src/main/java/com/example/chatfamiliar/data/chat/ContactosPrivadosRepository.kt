package com.example.chatfamiliar.data.chat

import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.family.MiembroFamiliaRepository
import com.example.chatfamiliar.model.ContactoPrivado
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.MiembroFamiliaDetalle
import java.util.Locale

/** Contactos encontrados y, si alguna familia no se pudo consultar, el error de cada una. */
data class ResultadoContactosPrivados(
    val contactos: List<ContactoPrivado>,
    val erroresPorFamilia: Map<String, ExcepcionChat>)

/** Obtiene las personas con las que el usuario puede iniciar un chat privado. */
class ContactosPrivadosRepository {
    private val authRepository = AuthRepository()
    private val miembrosRepository = MiembroFamiliaRepository()
    /**
     * Consulta los miembros de todas las familias del usuario y los combina
     * en una sola lista de contactos. Una familia con error no impide mostrar
     * los contactos de las demás.
     */
    fun obtenerContactos(familias: List<Familia>,
        alCompletar: (Result<ResultadoContactosPrivados>) -> Unit) {
        val uidActual = authRepository.obtenerUsuarioActual()?.uid
        if (uidActual.isNullOrBlank()) { alCompletar(Result.failure(ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA)))
            return }
        if (familias.any { !esIdValido(it.id) }) { alCompletar(Result.failure(
                ExcepcionChat(CodigoErrorChat.IDENTIFICADOR_INVALIDO)))
            return }
        val familiasUnicas = familias.distinctBy { it.id }.sortedBy { it.id }
        if (familiasUnicas.isEmpty()) { alCompletar(Result.success(ResultadoContactosPrivados(contactos = emptyList(),
                        erroresPorFamilia = emptyMap())))
            return }
        val miembrosPorFamilia = mutableMapOf<String, List<MiembroFamiliaDetalle>>()
        val erroresPorFamilia = mutableMapOf<String, ExcepcionChat>()
        var consultasPendientes = familiasUnicas.size
        var finalizado = false

        familiasUnicas.forEach { familia ->
            miembrosRepository.obtenerMiembrosFamilia(familiaId = familia.id) { resultado ->
                if (!finalizado) { val uidSesion = authRepository.obtenerUsuarioActual()?.uid
                    if (uidSesion != uidActual) { finalizado = true
                        alCompletar(Result.failure(ExcepcionChat(CodigoErrorChat.SESION_REQUERIDA))) } else { resultado
                            .onSuccess { miembros ->
                                if (miembros.any { !esIdValido(it.uid) }) { erroresPorFamilia[familia.id] =
                                        ExcepcionChat(CodigoErrorChat.DATOS_INVALIDOS)
                                } else { miembrosPorFamilia[familia.id] = miembros } }
                            .onFailure { error ->
                                erroresPorFamilia[familia.id] = ExcepcionChat.desde(error) }
                        consultasPendientes--
                        if (consultasPendientes == 0) { finalizado = true
                            val contactos = combinarContactos(uidActual = uidActual,
                                familias = familiasUnicas,
                                miembrosPorFamilia = miembrosPorFamilia)
                            alCompletar(Result.success(ResultadoContactosPrivados(contactos = contactos,
                                        erroresPorFamilia =
                                            erroresPorFamilia.toMap()))) } } } } } }

    /** Une a la misma persona aunque aparezca en varias familias y ordena por nombre. */
    private fun combinarContactos(uidActual: String, familias: List<Familia>,
        miembrosPorFamilia: Map<String, List<MiembroFamiliaDetalle>>): List<ContactoPrivado> {
        val contactosPorUid = linkedMapOf<String, ContactoPrivado>()
        familias.forEach { familia ->
            miembrosPorFamilia[familia.id].orEmpty().filter { it.uid != uidActual }
                .forEach { miembro ->
                    val anterior = contactosPorUid[miembro.uid]
                    val nombreConsultado = miembro.nombre.trim()
                    if (anterior == null) { contactosPorUid[miembro.uid] = ContactoPrivado(uid = miembro.uid,
                            nombre = nombreConsultado,
                            familiasCompartidas = listOf(familia))
                    } else { contactosPorUid[miembro.uid] = anterior.copy(
                            nombre = anterior.nombre.ifBlank { nombreConsultado },
                            familiasCompartidas = (anterior.familiasCompartidas +
                                                familia).distinctBy { it.id }) } } }
        return contactosPorUid.values.map { contacto ->
                contacto.copy(familiasCompartidas =
                        contacto.familiasCompartidas.sortedWith(compareBy<Familia> { it.nombre.lowercase(Locale.ROOT)
                            }.thenBy { it.id })) }
            .sortedWith(compareBy<ContactoPrivado> { it.nombre.isBlank() }.thenBy { it.nombre.lowercase(Locale.ROOT)
                }.thenBy { it.uid }) }
    /** Un id válido de Firestore no está vacío ni contiene '/'. */
    private fun esIdValido(id: String): Boolean { return id.isNotBlank() &&
                "/" !in id &&
                id != "." &&
                id != ".."
    } }