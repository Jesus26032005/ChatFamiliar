package com.example.chatfamiliar.ui.family

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.family.FamiliaRepository
import com.example.chatfamiliar.data.family.MiembroFamiliaRepository
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.MiembroFamilia
import com.example.chatfamiliar.model.MiembroFamiliaDetalle

class FamiliaViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val familiaRepository = FamiliaRepository()
    private val miembroFamiliaRepository = MiembroFamiliaRepository()

    var uidUsuarioActual by mutableStateOf("")
        private set
    var familiaActiva by mutableStateOf<Familia?>(null)
        private set
    var membresiaActiva by mutableStateOf<MiembroFamilia?>(null)
        private set
    var miembrosFamilia by
        mutableStateOf<List<MiembroFamiliaDetalle>>(emptyList())
        private set
    var nombreFamiliaNueva by mutableStateOf("")
        private set
    var codigoInvitacion by mutableStateOf("")
        private set
    var nombreFamiliaEdicion by mutableStateOf("")
        private set
    var cargando by mutableStateOf(false)
        private set
    var cargandoMiembros by mutableStateOf(false)
        private set
    var creandoFamilia by mutableStateOf(false)
        private set
    var uniendoFamilia by mutableStateOf(false)
        private set
    var editandoFamilia by mutableStateOf(false)
        private set
    var abandonandoFamilia by mutableStateOf(false)
        private set
    var eliminandoFamilia by mutableStateOf(false)
        private set
    var actualizandoRolUid by mutableStateOf<String?>(null)
        private set
    var expulsandoMiembroUid by mutableStateOf<String?>(null)
        private set
    @get:StringRes
    var errorFamiliaRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorMiembrosRecurso by mutableStateOf<Int?>(null)
        private set
    val esAdministrador: Boolean
        get() = membresiaActiva?.rol == MiembroFamilia.ROL_ADMINISTRADOR
    fun cargarPantalla(familiaIdInicial: String?) {
        if (cargando) return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }
        uidUsuarioActual = usuarioFirebase.uid
        errorFamiliaRecurso = null
        errorMiembrosRecurso = null

        if (familiaIdInicial.isNullOrBlank()) {
            limpiarFamiliaActiva()
            return
        }
        cargando = true
        familiaRepository.obtenerFamilia(familiaIdInicial) { resultado ->
            resultado
                .onSuccess { familia ->
                    if (familia == null) {
                        cargando = false
                        limpiarFamiliaActiva()
                        errorFamiliaRecurso = R.string.home_error_load_families
                        return@onSuccess
                    }
                    familiaActiva = familia
                    cargarMembresia(
                        familia = familia,
                        uidUsuario = usuarioFirebase.uid) }
                .onFailure {
                    cargando = false
                    limpiarFamiliaActiva()
                    errorFamiliaRecurso = R.string.home_error_load_families
                }
        }
    }
    private fun cargarMembresia(familia: Familia, uidUsuario: String) {
        familiaRepository.obtenerMembresia(
            familiaId = familia.id, uidUsuario = uidUsuario
        ) { resultado ->
            resultado
                .onSuccess { membresia ->
                    membresiaActiva = membresia
                    cargando = false
                    if (membresia == null) {
                        miembrosFamilia = emptyList()
                        errorFamiliaRecurso = R.string.home_error_load_membership
                        return@onSuccess
                    }
                    cargarMiembrosFamilia(familia.id) }
                .onFailure {
                    cargando = false
                    membresiaActiva = null
                    miembrosFamilia = emptyList()
                    errorFamiliaRecurso = R.string.home_error_load_membership
                }
        }
    }

    private fun cargarMiembrosFamilia(familiaId: String) {
        cargandoMiembros = true
        errorMiembrosRecurso = null

        miembroFamiliaRepository.obtenerMiembrosFamilia(
            familiaId = familiaId
        ) { resultado ->
            cargandoMiembros = false
            resultado
                .onSuccess { miembros ->
                    miembrosFamilia = miembros }
                .onFailure {
                    miembrosFamilia = emptyList()
                    errorMiembrosRecurso = R.string.home_error_load_members }
        }
    }

    fun actualizarNombreFamilia(nombre: String) {
        nombreFamiliaNueva = nombre
        errorFamiliaRecurso = null
    }

    fun crearFamilia(alTenerExito: () -> Unit) {
        if (creandoFamilia) return
        val nombreLimpio = nombreFamiliaNueva.trim()
        if (nombreLimpio.isBlank()) {
            errorFamiliaRecurso = R.string.home_error_empty_family_name
            return
        }
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }
        uidUsuarioActual = usuarioFirebase.uid
        creandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.crearFamilia(
            nombre = nombreLimpio,
            uidCreador = usuarioFirebase.uid) { resultado ->
            creandoFamilia = false
            resultado
                .onSuccess { familia ->
                    nombreFamiliaNueva = ""
                    activarFamilia(
                        familia = familia,
                        uidUsuario = usuarioFirebase.uid)
                    alTenerExito() }
                .onFailure {
                    errorFamiliaRecurso = R.string.home_error_create_family
                }
        }
    }

    fun actualizarCodigoInvitacion(codigo: String) {
        codigoInvitacion = codigo.uppercase()
        errorFamiliaRecurso = null
    }

    fun unirseAFamilia(alTenerExito: () -> Unit) {
        if (uniendoFamilia) return
        val codigoLimpio = codigoInvitacion.trim().uppercase()
        if (codigoLimpio.isBlank()) {
            errorFamiliaRecurso = R.string.home_error_empty_invitation_code
            return }
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }
        uidUsuarioActual = usuarioFirebase.uid
        uniendoFamilia = true
        errorFamiliaRecurso = null

        familiaRepository.unirseAFamilia(
            codigoInvitacion = codigoLimpio,
            uidUsuario = usuarioFirebase.uid
        ) { resultado ->
            uniendoFamilia = false
            resultado
                .onSuccess { familia ->
                    codigoInvitacion = ""
                    activarFamilia(
                        familia = familia,
                        uidUsuario = usuarioFirebase.uid)
                    alTenerExito() }
                .onFailure {
                    errorFamiliaRecurso = R.string.home_error_join_family }
        }
    }

    private fun activarFamilia(
        familia: Familia,
        uidUsuario: String
    ) {
        familiaActiva = familia
        membresiaActiva = null
        miembrosFamilia = emptyList()
        errorFamiliaRecurso = null
        errorMiembrosRecurso = null
        cargarMembresia(familia = familia, uidUsuario = uidUsuario)
    }

    fun prepararEdicionFamilia() {
        val familia = familiaActiva ?: return
        if (!esAdministrador) {
            errorFamiliaRecurso = R.string.home_error_admin_required
            return
        }
        nombreFamiliaEdicion = familia.nombre
        errorFamiliaRecurso = null
    }

    fun actualizarNombreFamiliaEdicion(nombre: String) {
        nombreFamiliaEdicion = nombre
        errorFamiliaRecurso = null
    }

    fun editarFamilia(alTenerExito: () -> Unit) {
        if (editandoFamilia) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) {
            errorFamiliaRecurso = R.string.home_error_admin_required
            return
        }
        val nombreLimpio = nombreFamiliaEdicion.trim()
        if (nombreLimpio.isBlank()) {
            errorFamiliaRecurso = R.string.home_error_empty_family_name
            return
        }
        editandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.editarFamilia(familiaId = familia.id,
            nuevoNombre = nombreLimpio
        ) { resultado ->
            editandoFamilia = false
            resultado
                .onSuccess {
                    familiaActiva = familia.copy(nombre = nombreLimpio)
                    nombreFamiliaEdicion = ""
                    alTenerExito() }
                .onFailure {
                    errorFamiliaRecurso = R.string.home_error_edit_family }
        }
    }

    fun hacerAdministrador(
        miembro: MiembroFamiliaDetalle
    ) {
        if (actualizandoRolUid != null) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) {
            errorMiembrosRecurso = R.string.home_error_admin_required
            return
        }
        if (miembro.esAdministrador) return
        actualizandoRolUid = miembro.uid
        errorMiembrosRecurso = null
        miembroFamiliaRepository.hacerAdministrador(
            familiaId = familia.id,
            uidMiembro = miembro.uid
        ) { resultado ->
            actualizandoRolUid = null
            resultado
                .onSuccess {
                    miembrosFamilia = ordenarMiembros(
                        miembrosFamilia.map { actual ->
                            if (actual.uid == miembro.uid) {
                                actual.copy(rol = MiembroFamilia.ROL_ADMINISTRADOR)
                            } else { actual }
                        }
                    ) }
                .onFailure {
                    errorMiembrosRecurso =
                        R.string.home_error_update_member_role
                }
        }
    }

    fun quitarAdministrador(miembro: MiembroFamiliaDetalle) {
        if (actualizandoRolUid != null) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) {
            errorMiembrosRecurso = R.string.home_error_admin_required
            return
        }
        if (miembro.uid == uidUsuarioActual) {
            errorMiembrosRecurso = R.string.home_error_modify_own_role
            return
        }
        if (miembro.uid == familia.creadoPor) {
            errorMiembrosRecurso = R.string.home_error_owner_protected
            return
        }
        if (!miembro.esAdministrador) return
        actualizandoRolUid = miembro.uid
        errorMiembrosRecurso = null
        miembroFamiliaRepository.quitarAdministrador(
            familiaId = familia.id,
            uidMiembro = miembro.uid
        ) { resultado ->
            actualizandoRolUid = null
            resultado
                .onSuccess {
                    miembrosFamilia = ordenarMiembros(
                        miembrosFamilia.map { actual ->
                            if (actual.uid == miembro.uid) {
                                actual.copy(rol = MiembroFamilia.ROL_MIEMBRO)
                            } else { actual } }) }
                .onFailure {
                    errorMiembrosRecurso =
                        R.string.home_error_update_member_role }
        }
    }

    fun expulsarMiembro(
        miembro: MiembroFamiliaDetalle,
        alTenerExito: () -> Unit
    ) {
        if (expulsandoMiembroUid != null) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) {
            errorMiembrosRecurso = R.string.home_error_admin_required
            return
        }
        if (miembro.uid == uidUsuarioActual) {
            errorMiembrosRecurso = R.string.home_error_remove_self
            return
        }
        if (miembro.uid == familia.creadoPor) {
            errorMiembrosRecurso = R.string.home_error_owner_protected
            return
        }
        expulsandoMiembroUid = miembro.uid
        errorMiembrosRecurso = null
        miembroFamiliaRepository.expulsarMiembro(
            familiaId = familia.id,
            uidMiembro = miembro.uid
        ) { resultado ->
            expulsandoMiembroUid = null
            resultado
                .onSuccess {
                    miembrosFamilia = miembrosFamilia.filterNot {
                        it.uid == miembro.uid
                    }
                    alTenerExito() }
                .onFailure {
                    errorMiembrosRecurso = R.string.home_error_remove_member }
        }
    }

    fun eliminarFamilia(alTenerExito: () -> Unit) {
        if (eliminandoFamilia) return
        val familia = familiaActiva ?: return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }
        val uidActual = usuarioFirebase.uid
        if (familia.creadoPor != uidActual) {
            errorFamiliaRecurso =
                R.string.home_error_delete_family_owner_only
            return
        }
        if (!esAdministrador) {
            errorFamiliaRecurso = R.string.home_error_admin_required
            return
        }
        eliminandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.eliminarFamiliaCompleta(
            familiaId = familia.id,
            uidAdministradorPrincipal = uidActual
        ) { resultado ->
            eliminandoFamilia = false
            resultado
                .onSuccess {
                    limpiarFamiliaActiva()
                    alTenerExito() }
                .onFailure {
                    errorFamiliaRecurso = R.string.home_error_delete_family }
        }
    }

    fun abandonarFamilia(alTenerExito: () -> Unit) {
        if (abandonandoFamilia) return
        val familia = familiaActiva ?: return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }
        val uidActual = usuarioFirebase.uid
        if (familia.creadoPor == uidActual) {
            val candidatos = miembrosFamilia.filter {
                it.uid != uidActual
            }
            if (candidatos.isEmpty()) {
                errorFamiliaRecurso = R.string.home_error_owner_last_member
                return
            }
            val nuevoAdministrador = candidatos.random()
            abandonandoFamilia = true
            errorFamiliaRecurso = null
            familiaRepository.transferirAdministracionYAbandonar(
                familiaId = familia.id,
                uidAdministradorActual = uidActual,
                uidNuevoAdministrador = nuevoAdministrador.uid
            ) { resultado ->
                abandonandoFamilia = false
                resultado
                    .onSuccess {
                        limpiarFamiliaActiva()
                        alTenerExito() }
                    .onFailure {
                        errorFamiliaRecurso = R.string.home_error_leave_family }
            }
            return
        }
        abandonandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.abandonarFamilia(
            familiaId = familia.id,
            uidUsuario = uidActual
        ) { resultado ->
            abandonandoFamilia = false
            resultado
                .onSuccess {
                    limpiarFamiliaActiva()
                    alTenerExito() }
                .onFailure {
                    errorFamiliaRecurso = R.string.home_error_leave_family }
        }
    }

    fun limpiarErrorFamilia() { errorFamiliaRecurso = null }
    fun limpiarErrorMiembros() { errorMiembrosRecurso = null }
    private fun limpiarFamiliaActiva() {
        familiaActiva = null
        membresiaActiva = null
        miembrosFamilia = emptyList()
        nombreFamiliaEdicion = ""
        cargando = false
        cargandoMiembros = false
        actualizandoRolUid = null
        expulsandoMiembroUid = null
        errorMiembrosRecurso = null
    }
    private fun ordenarMiembros(
        miembros: List<MiembroFamiliaDetalle>
    ): List<MiembroFamiliaDetalle> {
        return miembros.sortedWith(
            compareByDescending<MiembroFamiliaDetalle> {
                it.esAdministrador
            }.thenBy {
                it.nombre.lowercase()
            }
        )
    }
}
