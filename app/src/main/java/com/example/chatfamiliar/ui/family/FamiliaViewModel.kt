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
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.util.TimeoutSolicitud

/**
 * Estado y acciones de la gestión de una familia: crear, unirse, editar, roles, expulsar, abandonar y eliminar.
 */
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
    private val timeoutFamilia = TimeoutSolicitud()
    private val timeoutMembresia = TimeoutSolicitud()
    private val timeoutMiembros = TimeoutSolicitud()
    val esAdministrador: Boolean
        get() = membresiaActiva?.rol == MiembroFamilia.ROL_ADMINISTRADOR
    /** Carga la familia indicada, la membresía del usuario y la lista de integrantes. */
    fun cargarPantalla(familiaIdInicial: String?) { if (cargando) return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) { errorFamiliaRecurso = R.string.home_error_no_session
            return }
        uidUsuarioActual = usuarioFirebase.uid
        errorFamiliaRecurso = null
        errorMiembrosRecurso = null
        if (familiaIdInicial.isNullOrBlank()) { limpiarFamiliaActiva()
            return }
        cargando = true
        val solicitud = timeoutFamilia.iniciar(scope = viewModelScope) { cargando = false
            familiaActiva = null
            membresiaActiva = null
            miembrosFamilia = emptyList()
            errorFamiliaRecurso = R.string.error_network }
        familiaRepository.obtenerFamilia(familiaIdInicial) { resultado ->
            if (!timeoutFamilia.completar(solicitud)) { return@obtenerFamilia }
            resultado.onSuccess { familia ->
                if (familia == null) { cargando = false
                    limpiarFamiliaActiva()
                    errorFamiliaRecurso = R.string.home_error_load_families
                    return@onSuccess }
                familiaActiva = familia
                cargarMembresia(familia = familia,
                    uidUsuario = usuarioFirebase.uid) }
                .onFailure { cargando = false
                    limpiarFamiliaActiva()
                    errorFamiliaRecurso = R.string.home_error_load_families } } }

    /** Lee el rol del usuario en la familia y después carga los integrantes. */
    private fun cargarMembresia(familia: Familia, uidUsuario: String) {
        val solicitud = timeoutMembresia.iniciar(scope = viewModelScope) { cargando = false
            membresiaActiva = null
            miembrosFamilia = emptyList()
            errorFamiliaRecurso = R.string.error_network }
        familiaRepository.obtenerMembresia(familiaId = familia.id, uidUsuario = uidUsuario) { resultado ->
            if (!timeoutMembresia.completar(solicitud)) { return@obtenerMembresia }
            resultado.onSuccess { membresia ->
                    membresiaActiva = membresia
                    cargando = false
                    if (membresia == null) { miembrosFamilia = emptyList()
                        errorFamiliaRecurso = R.string.home_error_load_membership
                        return@onSuccess }
                    cargarMiembrosFamilia(familia.id) }
                .onFailure { cargando = false
                    membresiaActiva = null
                    miembrosFamilia = emptyList()
                    errorFamiliaRecurso = R.string.home_error_load_membership } } }
    /** Carga los integrantes de la familia. */
    private fun cargarMiembrosFamilia(familiaId: String) { cargandoMiembros = true
        errorMiembrosRecurso = null
        val solicitud = timeoutMiembros.iniciar(scope = viewModelScope) { cargandoMiembros = false
                errorMiembrosRecurso = R.string.error_network }
        miembroFamiliaRepository.obtenerMiembrosFamilia(familiaId = familiaId) { resultado ->
            if (!timeoutMiembros.completar(solicitud)) { return@obtenerMiembrosFamilia }
            cargandoMiembros = false
            resultado.onSuccess { miembros ->
                    miembrosFamilia = miembros }
                .onFailure { miembrosFamilia = emptyList()
                    errorMiembrosRecurso = R.string.home_error_load_members } } }
    /** Actualiza el nombre escrito para la familia nueva. */
    fun actualizarNombreFamilia(nombre: String) { nombreFamiliaNueva = nombre
        errorFamiliaRecurso = null }

    /** Crea una familia nueva con el usuario como administrador principal. */
    fun crearFamilia(alTenerExito: () -> Unit) { if (creandoFamilia) return
        val nombreLimpio = nombreFamiliaNueva.trim()
        if (nombreLimpio.isBlank()) { errorFamiliaRecurso = R.string.home_error_empty_family_name
            return }
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) { errorFamiliaRecurso = R.string.home_error_no_session
            return }
        uidUsuarioActual = usuarioFirebase.uid
        creandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.crearFamilia(nombre = nombreLimpio,
            uidCreador = usuarioFirebase.uid) { resultado ->
            creandoFamilia = false
            resultado
                .onSuccess { familia ->
                    nombreFamiliaNueva = ""
                    activarFamilia(familia = familia,
                        uidUsuario = usuarioFirebase.uid)
                    alTenerExito() }
                .onFailure { errorFamiliaRecurso = R.string.home_error_create_family } } }

    /** Actualiza el código de invitación escrito (en mayúsculas). */
    fun actualizarCodigoInvitacion(codigo: String) { codigoInvitacion = codigo.uppercase()
        errorFamiliaRecurso = null }

    /** Une al usuario a la familia del código de invitación. */
    fun unirseAFamilia(alTenerExito: () -> Unit) { if (uniendoFamilia) return
        val codigoLimpio = codigoInvitacion.trim().uppercase()
        if (codigoLimpio.isBlank()) { errorFamiliaRecurso = R.string.home_error_empty_invitation_code
            return }
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) { errorFamiliaRecurso = R.string.home_error_no_session
            return }
        uidUsuarioActual = usuarioFirebase.uid
        uniendoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.unirseAFamilia(codigoInvitacion = codigoLimpio,
            uidUsuario = usuarioFirebase.uid) { resultado ->
            uniendoFamilia = false
            resultado.onSuccess { familia -> codigoInvitacion = ""
                    activarFamilia(familia = familia, uidUsuario = usuarioFirebase.uid)
                    alTenerExito() }
                .onFailure { errorFamiliaRecurso = R.string.home_error_join_family } } }
    /** Deja una familia como la activa en pantalla y carga sus datos. */
    private fun activarFamilia(familia: Familia, uidUsuario: String) { familiaActiva = familia
        membresiaActiva = null
        miembrosFamilia = emptyList()
        errorFamiliaRecurso = null
        errorMiembrosRecurso = null
        cargarMembresia(familia = familia, uidUsuario = uidUsuario) }
    /** Prepara el diálogo de edición con el nombre actual (solo administradores). */
    fun prepararEdicionFamilia() { val familia = familiaActiva ?: return
        if (!esAdministrador) { errorFamiliaRecurso = R.string.home_error_admin_required
            return }
        nombreFamiliaEdicion = familia.nombre
        errorFamiliaRecurso = null }
    /** Actualiza el nombre escrito en el diálogo de edición. */
    fun actualizarNombreFamiliaEdicion(nombre: String) { nombreFamiliaEdicion = nombre
        errorFamiliaRecurso = null }

    /** Guarda el nuevo nombre de la familia (solo administradores). */
    fun editarFamilia(alTenerExito: () -> Unit) { if (editandoFamilia) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) { errorFamiliaRecurso = R.string.home_error_admin_required
            return }
        val nombreLimpio = nombreFamiliaEdicion.trim()
        if (nombreLimpio.isBlank()) { errorFamiliaRecurso = R.string.home_error_empty_family_name
            return }
        editandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.editarFamilia(familiaId = familia.id,
            nuevoNombre = nombreLimpio) { resultado ->
            editandoFamilia = false
            resultado.onSuccess { familiaActiva = familia.copy(nombre = nombreLimpio)
                    nombreFamiliaEdicion = ""
                    alTenerExito() }
                .onFailure { errorFamiliaRecurso = R.string.home_error_edit_family } } }
    /** Da el rol de administrador a un integrante. */
    fun hacerAdministrador(miembro: MiembroFamiliaDetalle) { if (actualizandoRolUid != null) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) { errorMiembrosRecurso = R.string.home_error_admin_required
            return }
        if (miembro.esAdministrador) return
        actualizandoRolUid = miembro.uid
        errorMiembrosRecurso = null
        miembroFamiliaRepository.hacerAdministrador(familiaId = familia.id,
            uidMiembro = miembro.uid) { resultado ->
            actualizandoRolUid = null
            resultado.onSuccess { miembrosFamilia = ordenarMiembros(miembrosFamilia.map { actual ->
                            if (actual.uid == miembro.uid) { actual.copy(rol = MiembroFamilia.ROL_ADMINISTRADOR)
                            } else { actual } }) }
                .onFailure { errorMiembrosRecurso =
                        R.string.home_error_update_member_role } } }

    /**
     * Quita el rol de administrador; no se permite con uno mismo ni con el administrador principal.
     */
    fun quitarAdministrador(miembro: MiembroFamiliaDetalle) { if (actualizandoRolUid != null) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) { errorMiembrosRecurso = R.string.home_error_admin_required
            return }
        if (miembro.uid == uidUsuarioActual) { errorMiembrosRecurso = R.string.home_error_modify_own_role
            return }
        if (miembro.uid == familia.creadoPor) { errorMiembrosRecurso = R.string.home_error_owner_protected
            return }
        if (!miembro.esAdministrador) return
        actualizandoRolUid = miembro.uid
        errorMiembrosRecurso = null
        miembroFamiliaRepository.quitarAdministrador(familiaId = familia.id,
            uidMiembro = miembro.uid) { resultado ->
            actualizandoRolUid = null
            resultado.onSuccess { miembrosFamilia = ordenarMiembros(miembrosFamilia.map { actual ->
                            if (actual.uid == miembro.uid) { actual.copy(rol = MiembroFamilia.ROL_MIEMBRO)
                            } else { actual } }) }
                .onFailure { errorMiembrosRecurso =
                        R.string.home_error_update_member_role } } }

    /** Saca a un integrante; no se permite con uno mismo ni con el administrador principal. */
    fun expulsarMiembro(miembro: MiembroFamiliaDetalle, alTenerExito: () -> Unit) {
        if (expulsandoMiembroUid != null) return
        val familia = familiaActiva ?: return
        if (!esAdministrador) { errorMiembrosRecurso = R.string.home_error_admin_required
            return }
        if (miembro.uid == uidUsuarioActual) { errorMiembrosRecurso = R.string.home_error_remove_self
            return }
        if (miembro.uid == familia.creadoPor) { errorMiembrosRecurso = R.string.home_error_owner_protected
            return }
        expulsandoMiembroUid = miembro.uid
        errorMiembrosRecurso = null
        miembroFamiliaRepository.expulsarMiembro(familiaId = familia.id,
            uidMiembro = miembro.uid) { resultado ->
            expulsandoMiembroUid = null
            resultado.onSuccess { miembrosFamilia = miembrosFamilia.filterNot { it.uid == miembro.uid }
                    alTenerExito() }
                .onFailure { errorMiembrosRecurso = R.string.home_error_remove_member } } }

    /** Elimina la familia completa; solo el administrador principal puede hacerlo. */
    fun eliminarFamilia(alTenerExito: () -> Unit) { if (eliminandoFamilia) return
        val familia = familiaActiva ?: return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) { errorFamiliaRecurso = R.string.home_error_no_session
            return }
        val uidActual = usuarioFirebase.uid
        if (familia.creadoPor != uidActual) { errorFamiliaRecurso =
                R.string.home_error_delete_family_owner_only
            return }
        if (!esAdministrador) { errorFamiliaRecurso = R.string.home_error_admin_required
            return }
        eliminandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.eliminarFamiliaCompleta(familiaId = familia.id,
            uidAdministradorPrincipal = uidActual) { resultado ->
            eliminandoFamilia = false
            resultado.onSuccess { limpiarFamiliaActiva()
                    alTenerExito() }
                .onFailure { errorFamiliaRecurso = R.string.home_error_delete_family } } }
    /**
     * Sale de la familia. Si quien sale es el administrador principal, antes
     * transfiere la familia a otro integrante.
     */
    fun abandonarFamilia(alTenerExito: () -> Unit) { if (abandonandoFamilia) return
        val familia = familiaActiva ?: return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) { errorFamiliaRecurso = R.string.home_error_no_session
            return }
        val uidActual = usuarioFirebase.uid
        if (familia.creadoPor == uidActual) { val candidatos = miembrosFamilia.filter { it.uid != uidActual }
            if (candidatos.isEmpty()) { errorFamiliaRecurso = R.string.home_error_owner_last_member
                return }
            // Se prefiere a quien ya es administrador; si no hay, uno al azar.
            val nuevoAdministrador = candidatos.firstOrNull { it.esAdministrador }
                ?: candidatos.random()
            abandonandoFamilia = true
            errorFamiliaRecurso = null
            familiaRepository.transferirAdministracionYAbandonar(
                familiaId = familia.id, uidAdministradorActual = uidActual,
                uidNuevoAdministrador = nuevoAdministrador.uid) { resultado ->
                abandonandoFamilia = false
                resultado.onSuccess { limpiarFamiliaActiva()
                        alTenerExito() }
                    .onFailure { errorFamiliaRecurso = R.string.home_error_leave_family } }
            return }
        abandonandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository.abandonarFamilia(familiaId = familia.id,
            uidUsuario = uidActual) { resultado ->
            abandonandoFamilia = false
            resultado.onSuccess { limpiarFamiliaActiva()
                    alTenerExito() }
                .onFailure { errorFamiliaRecurso = R.string.home_error_leave_family } } }
    /** Oculta el error de la sección de familia. */
    fun limpiarErrorFamilia() { errorFamiliaRecurso = null }
    /** Oculta el error de la sección de integrantes. */
    fun limpiarErrorMiembros() { errorMiembrosRecurso = null }
    /** Cancela las esperas y limpia los datos de la familia en pantalla. */
    private fun limpiarFamiliaActiva() { timeoutFamilia.cancelar()
        timeoutMembresia.cancelar()
        timeoutMiembros.cancelar()
        familiaActiva = null
        membresiaActiva = null
        miembrosFamilia = emptyList()
        nombreFamiliaEdicion = ""
        cargando = false
        cargandoMiembros = false
        actualizandoRolUid = null
        expulsandoMiembroUid = null
        errorFamiliaRecurso = null
        errorMiembrosRecurso = null }
    /** Administradores primero y después por nombre. */
    private fun ordenarMiembros(miembros: List<MiembroFamiliaDetalle>): List<MiembroFamiliaDetalle> {
        return miembros.sortedWith(compareByDescending<MiembroFamiliaDetalle> { it.esAdministrador
            }.thenBy { it.nombre.lowercase() }) } }
