package com.example.chatfamiliar.ui.home

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.chatfamiliar.R
import com.example.chatfamiliar.data.auth.AuthRepository
import com.example.chatfamiliar.data.family.FamiliaRepository
import com.example.chatfamiliar.data.user.UsuarioRepository
import com.example.chatfamiliar.model.Familia
import com.example.chatfamiliar.model.MiembroFamilia
import com.example.chatfamiliar.model.Usuario
import com.example.chatfamiliar.data.family.MiembroFamiliaRepository
import com.example.chatfamiliar.model.MiembroFamiliaDetalle

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val familiaRepository = FamiliaRepository()
    private val miembroFamiliaRepository = MiembroFamiliaRepository()
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var familias by mutableStateOf<List<Familia>>(emptyList())
        private set
    var familiaActiva by mutableStateOf<Familia?>(null)
        private set
    var membresiaActiva by
    mutableStateOf<MiembroFamilia?>(null)
        private set
    var nombreNuevo by mutableStateOf("")
        private set
    var nombreFamiliaNueva by mutableStateOf("")
        private set
    var codigoInvitacion by mutableStateOf("")
        private set
    var cargando by mutableStateOf(false)
        private set
    var cargandoFamilia by mutableStateOf(false)
        private set
    var guardandoNombre by mutableStateOf(false)
        private set
    var creandoFamilia by mutableStateOf(false)
        private set
    var uniendoFamilia by mutableStateOf(false)
        private set
    var nombreFamiliaEdicion by mutableStateOf("")
        private set
    var editandoFamilia by mutableStateOf(false)
        private set
    var abandonandoFamilia by mutableStateOf(false)
        private set
    var miembrosFamilia by
    mutableStateOf<List<MiembroFamiliaDetalle>>(emptyList())
        private set
    var cargandoMiembros by mutableStateOf(false)
        private set
    var actualizandoRolUid by mutableStateOf<String?>(null)
        private set
    var eliminandoFamilia by mutableStateOf(false)
        private set
    var expulsandoMiembroUid by mutableStateOf<String?>(null)
        private set
    @get:StringRes
    var errorMiembrosRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorRecurso by mutableStateOf<Int?>(null)
        private set
    @get:StringRes
    var errorFamiliaRecurso by mutableStateOf<Int?>(null)
        private set
    val necesitaNombre: Boolean
        get() = usuario != null && usuario?.nombre.isNullOrBlank()
    val tieneFamilias: Boolean
        get() = familias.isNotEmpty()
    val esAdministrador: Boolean
        get() = membresiaActiva?.rol == MiembroFamilia.ROL_ADMINISTRADOR

    fun cargarUsuario() {
        if (cargando) { return }
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorRecurso = R.string.home_error_no_session
            return
        }
        cargando = true
        errorRecurso = null

        usuarioRepository
            .obtenerUsuario(usuarioFirebase.uid
            ) { resultado ->
                resultado
                    .onSuccess { perfil ->
                        if (perfil != null) {
                            usuario = perfil
                            cargarFamilias(
                                uidUsuario = usuarioFirebase.uid,
                                finalizarCargaInicial = true)
                        } else {
                            crearPerfilFaltante(
                                uid = usuarioFirebase.uid,
                                correo = usuarioFirebase.email
                                        .orEmpty())
                        }
                    }
                    .onFailure {
                        cargando = false
                        errorRecurso = R.string.home_error_load_profile
                    }
            }
    }

    private fun cargarFamilias(
        uidUsuario: String,
        finalizarCargaInicial: Boolean,
        familiaPreferidaId: String? = null
    ) {
        cargandoFamilia = true

        familiaRepository
            .obtenerFamiliasDelUsuario(uidUsuario) { resultado ->
                resultado
                    .onSuccess { familiasUsuario ->
                        val familiasOrdenadas =
                            familiasUsuario.sortedBy { it.nombre.lowercase() }
                        familias = familiasOrdenadas
                        if (familiasOrdenadas.isEmpty()) {
                            familiaActiva = null
                            membresiaActiva = null
                            cargandoFamilia = false
                            if (finalizarCargaInicial
                            ) { cargando = false }
                            return@onSuccess
                        }

                        val seleccionada =
                            familiaPreferidaId?.let { id ->
                                    familiasOrdenadas.firstOrNull { it.id == id }
                                }
                                ?: familiaActiva?.let { anterior ->
                                        familiasOrdenadas.firstOrNull { it.id ==
                                                        anterior.id
                                            }
                                    }
                                ?: familiasOrdenadas.first()
                        familiaActiva = seleccionada

                        cargarMembresia(
                            familia = seleccionada,
                            uidUsuario = uidUsuario,
                            finalizarCargaInicial = finalizarCargaInicial)
                    }
                    .onFailure {
                        cargandoFamilia = false
                        if (finalizarCargaInicial) {
                            cargando = false }
                        errorRecurso = R.string.home_error_load_families
                    }
            }
    }

    private fun cargarMiembrosFamilia(
        familiaId: String
    ) {
        cargandoMiembros = true
        errorMiembrosRecurso = null

        miembroFamiliaRepository
            .obtenerMiembrosFamilia(familiaId = familiaId) {
                resultado ->
                cargandoMiembros = false
                resultado
                    .onSuccess { miembros ->
                        miembrosFamilia = miembros }
                    .onFailure {
                        miembrosFamilia = emptyList()
                        errorMiembrosRecurso =
                            R.string.home_error_load_members
                    }
            }
    }


    private fun cargarMembresia(
        familia: Familia,
        uidUsuario: String,
        finalizarCargaInicial: Boolean
    ) {
        cargandoFamilia = true
        familiaRepository.obtenerMembresia(
                familiaId = familia.id,
                uidUsuario = uidUsuario
            ) { resultado ->
                resultado
                    .onSuccess { membresia ->
                        membresiaActiva = membresia
                        if (membresia != null) {
                            cargarMiembrosFamilia(
                                familiaId = familia.id)
                        } else {
                            miembrosFamilia = emptyList()
                        }
                        cargandoFamilia = false

                        if (finalizarCargaInicial) {
                            cargando = false }
                        if (membresia == null) {
                            errorRecurso = R.string.home_error_load_membership
                        }
                    }
                    .onFailure {
                        membresiaActiva = null
                        cargandoFamilia = false

                        if (finalizarCargaInicial) {
                            cargando = false
                        }
                        errorRecurso = R.string.home_error_load_membership
                    }
            }
    }

    fun hacerAdministrador(miembro: MiembroFamiliaDetalle) {
        if (actualizandoRolUid != null) { return }
        val familia = familiaActiva ?: return

        if (!esAdministrador) {
            errorMiembrosRecurso = R.string.home_error_admin_required
            return
        }

        if (miembro.esAdministrador) { return }
        actualizandoRolUid = miembro.uid
        errorMiembrosRecurso = null

        miembroFamiliaRepository.hacerAdministrador(
                familiaId = familia.id,
                uidMiembro = miembro.uid
            ) { resultado ->
                actualizandoRolUid = null
                resultado
                    .onSuccess {
                        miembrosFamilia = miembrosFamilia
                                .map { actual ->
                                    if (actual.uid == miembro.uid) {
                                        actual.copy(
                                            rol = MiembroFamilia.ROL_ADMINISTRADOR)
                                    } else {
                                        actual
                                    }
                                }
                                .sortedWith(compareByDescending
                                <MiembroFamiliaDetalle> {
                                    it.esAdministrador
                                }.thenBy {
                                    it.nombre.lowercase()
                                }) }
                    .onFailure {
                        errorMiembrosRecurso = R.string
                            .home_error_update_member_role
                    }
            }
    }

    fun quitarAdministrador(miembro: MiembroFamiliaDetalle) {
        if (actualizandoRolUid != null) { return
        }
        val familia = familiaActiva ?: return
        val usuarioActual = usuario ?: return
        if (!esAdministrador) {
            errorMiembrosRecurso = R.string.home_error_admin_required
            return
        }
        if (miembro.uid == usuarioActual.uid) {
            errorMiembrosRecurso = R.string.home_error_modify_own_role
            return
        }
        if (miembro.uid == familia.creadoPor) {
            errorMiembrosRecurso = R.string.home_error_owner_protected
            return
        }
        if (!miembro.esAdministrador) { return }

        actualizandoRolUid = miembro.uid

        errorMiembrosRecurso = null

        miembroFamiliaRepository.quitarAdministrador(
                familiaId = familia.id,
                uidMiembro = miembro.uid
            ) { resultado ->
                actualizandoRolUid = null
                resultado
                    .onSuccess { miembrosFamilia =
                            miembrosFamilia
                                .map { actual ->
                                    if (actual.uid == miembro.uid) {
                                        actual.copy(
                                            rol = MiembroFamilia.ROL_MIEMBRO)
                                    } else { actual }
                                }
                                .sortedWith(
                                    compareByDescending
                                    <MiembroFamiliaDetalle> {
                                        it.esAdministrador
                                    }.thenBy {
                                        it.nombre.lowercase()
                                    }) }
                    .onFailure {
                        errorMiembrosRecurso = R.string
                                .home_error_update_member_role
                    }
            }
    }

    fun expulsarMiembro(miembro: MiembroFamiliaDetalle,
                        alTenerExito: () -> Unit) {
        if (expulsandoMiembroUid != null) { return }
        val familia = familiaActiva ?: return
        val usuarioActual = usuario ?: return
        if (!esAdministrador) {
            errorMiembrosRecurso = R.string.home_error_admin_required
            return
        }
        if (miembro.uid == usuarioActual.uid) {
            errorMiembrosRecurso =
                R.string.home_error_remove_self
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
                uidMiembro = miembro.uid) { resultado ->
                expulsandoMiembroUid = null
                resultado
                    .onSuccess {
                        miembrosFamilia =
                            miembrosFamilia.filterNot {
                                it.uid == miembro.uid
                            }
                        alTenerExito() }
                    .onFailure {
                        errorMiembrosRecurso = R.string.home_error_remove_member
                    }
            }
    }


    fun seleccionarFamilia(familia: Familia) {
        if (familiaActiva?.id == familia.id && membresiaActiva != null) {
            return }

        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorRecurso = R.string.home_error_no_session
            return }

        familiaActiva = familia
        membresiaActiva = null
        miembrosFamilia = emptyList()
        errorMiembrosRecurso = null
        errorRecurso = null

        cargarMembresia(
            familia = familia,
            uidUsuario = usuarioFirebase.uid,
            finalizarCargaInicial = false)
    }


    fun actualizarNombreFamilia(nombre: String) {
        nombreFamiliaNueva = nombre
        errorFamiliaRecurso = null
    }

    fun crearFamilia(alTenerExito: () -> Unit) {
        if (creandoFamilia) { return }
        val nombreLimpio = nombreFamiliaNueva.trim()

        if (nombreLimpio.isBlank()) {
            errorFamiliaRecurso = R.string.home_error_empty_family_name
            return }

        val usuarioFirebase = authRepository.obtenerUsuarioActual()

        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }

        creandoFamilia = true
        errorFamiliaRecurso = null

        familiaRepository
            .crearFamilia(nombre = nombreLimpio,
                uidCreador = usuarioFirebase.uid
            ) { resultado ->
                creandoFamilia = false
                resultado
                    .onSuccess { familia ->
                        nombreFamiliaNueva = ""
                        alTenerExito()
                        cargarFamilias(
                            uidUsuario = usuarioFirebase.uid,
                            finalizarCargaInicial = false,
                            familiaPreferidaId = familia.id) }
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
        if (uniendoFamilia) { return }
        val codigoLimpio = codigoInvitacion.trim().uppercase()

        if (codigoLimpio.isBlank()) {
            errorFamiliaRecurso = R.string.home_error_empty_invitation_code
            return
        }

        val usuarioFirebase = authRepository.obtenerUsuarioActual()

        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return }

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
                        alTenerExito()
                        cargarFamilias(
                            uidUsuario = usuarioFirebase.uid,
                            finalizarCargaInicial = false,
                            familiaPreferidaId = familia.id) }
                    .onFailure {
                        errorFamiliaRecurso = R.string.home_error_join_family
                    }
            }
    }


    fun limpiarErrorFamilia() { errorFamiliaRecurso = null }

    fun actualizarNombre(nuevoNombre: String
    ) { nombreNuevo = nuevoNombre
        errorRecurso = null }


    fun guardarNombre() {
        if (guardandoNombre) { return }
        val perfilActual = usuario ?: return
        val nombreLimpio = nombreNuevo.trim()
        if (nombreLimpio.isBlank()) {
            errorRecurso = R.string.home_error_empty_name
            return
        }
        guardandoNombre = true
        errorRecurso = null
        usuarioRepository.actualizarNombre(
                uid = perfilActual.uid,
                nombre = nombreLimpio
            ) { resultado ->
                guardandoNombre = false
                resultado
                    .onSuccess {
                        usuario = perfilActual.copy(
                                nombre = nombreLimpio)
                        nombreNuevo = "" }
                    .onFailure {
                        errorRecurso = R.string.home_error_save_name }
            }
    }


    private fun crearPerfilFaltante(uid: String, correo: String) {
        usuarioRepository
            .crearUsuario(uid = uid, correo = correo) { resultado ->
                resultado
                    .onSuccess {
                        usuario =
                            Usuario(uid = uid,
                                nombre = "", correo = correo)

                        cargarFamilias(uidUsuario = uid,
                            finalizarCargaInicial = true) }
                    .onFailure {
                        cargando = false
                        errorRecurso = R.string.home_error_load_profile
                    }
            }
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
        if (editandoFamilia) { return }
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
        familiaRepository.editarFamilia(
                familiaId = familia.id,
                nuevoNombre = nombreLimpio
            ) { resultado ->
                editandoFamilia = false
                resultado
                    .onSuccess {
                        val familiaActualizada =
                            familia.copy(nombre = nombreLimpio)
                        familiaActiva = familiaActualizada
                        familias = familias
                            .map { if (it.id == familia.id) { familiaActualizada
                            } else { it } }
                            .sortedBy {
                                it.nombre.lowercase()
                            }
                        nombreFamiliaEdicion = ""
                        alTenerExito() }
                    .onFailure { errorFamiliaRecurso =
                        R.string.home_error_edit_family
                    }
            }
    }


    fun eliminarFamilia(alTenerExito: () -> Unit) {
        if (eliminandoFamilia) { return }
        val familia = familiaActiva ?: return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }
        val uidActual = usuarioFirebase.uid
        if (familia.creadoPor != uidActual) {
            errorFamiliaRecurso = R.string
                .home_error_delete_family_owner_only
            return
        }
        if (!esAdministrador) {
            errorFamiliaRecurso = R.string.home_error_admin_required
            return
        }
        eliminandoFamilia = true
        errorFamiliaRecurso = null

        familiaRepository
            .eliminarFamiliaCompleta(
                familiaId = familia.id,
                uidAdministradorPrincipal = uidActual
            ) { resultado ->
                eliminandoFamilia = false
                resultado
                    .onSuccess {
                        limpiarFamiliaActiva()
                        alTenerExito()
                        cargarFamilias(uidUsuario = uidActual,
                            finalizarCargaInicial = false
                        )
                    }
                    .onFailure {
                        errorFamiliaRecurso = R.string.home_error_delete_family }
            }
    }

    fun abandonarFamilia(alTenerExito: () -> Unit) {
        if (abandonandoFamilia) { return }
        val familia = familiaActiva ?: return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorFamiliaRecurso = R.string.home_error_no_session
            return
        }
        val uidActual = usuarioFirebase.uid
        if (familia.creadoPor == uidActual) {
            val candidatos = miembrosFamilia.filter { it.uid != uidActual }
            if (candidatos.isEmpty()) { errorFamiliaRecurso =
                    R.string.home_error_owner_last_member
                return
            }
            val nuevoAdministrador = candidatos.random()
            abandonandoFamilia = true
            errorFamiliaRecurso = null
            familiaRepository
                .transferirAdministracionYAbandonar(
                    familiaId = familia.id,
                    uidAdministradorActual = uidActual,
                    uidNuevoAdministrador = nuevoAdministrador.uid
                ) { resultado ->
                    abandonandoFamilia = false
                    resultado
                        .onSuccess {
                            limpiarFamiliaActiva()
                            alTenerExito()
                            cargarFamilias(
                                uidUsuario = uidActual,
                                finalizarCargaInicial = false
                            )
                        }
                        .onFailure {
                            errorFamiliaRecurso = R.string
                                .home_error_leave_family }
                }
            return
        }
        abandonandoFamilia = true
        errorFamiliaRecurso = null
        familiaRepository
            .abandonarFamilia(
                familiaId = familia.id,
                uidUsuario = uidActual
            ) { resultado ->
                abandonandoFamilia = false
                resultado
                    .onSuccess {
                        limpiarFamiliaActiva()
                        alTenerExito()
                        cargarFamilias(
                            uidUsuario = uidActual,
                            finalizarCargaInicial = false) }
                    .onFailure { errorFamiliaRecurso =
                        R.string.home_error_leave_family }
            }
    }

    private fun limpiarFamiliaActiva() {
        familiaActiva = null
        membresiaActiva = null
        miembrosFamilia = emptyList()
        cargandoMiembros = false
        actualizandoRolUid = null
        expulsandoMiembroUid = null
        errorMiembrosRecurso = null
    }

    fun cerrarSesion(alCerrarSesion: () -> Unit) {
        authRepository.cerrarSesion()
        usuario = null
        familias = emptyList()
        familiaActiva = null
        membresiaActiva = null
        nombreNuevo = ""
        nombreFamiliaNueva = ""
        codigoInvitacion = ""
        cargando = false
        cargandoFamilia = false
        guardandoNombre = false
        creandoFamilia = false
        uniendoFamilia = false
        errorRecurso = null
        errorFamiliaRecurso = null
        miembrosFamilia = emptyList()
        cargandoMiembros = false
        actualizandoRolUid = null
        errorMiembrosRecurso = null
        nombreFamiliaEdicion = ""
        expulsandoMiembroUid = null
        miembrosFamilia = emptyList()
        cargandoMiembros = false
        actualizandoRolUid = null
        expulsandoMiembroUid = null
        errorMiembrosRecurso = null
        editandoFamilia = false
        eliminandoFamilia = false
        abandonandoFamilia = false
        alCerrarSesion()
    }
    fun limpiarErrorMiembros() { errorMiembrosRecurso = null }
}