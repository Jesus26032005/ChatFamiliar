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
import androidx.lifecycle.viewModelScope
import com.example.chatfamiliar.util.TimeoutSolicitud

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val familiaRepository = FamiliaRepository()

    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var familias by mutableStateOf<List<Familia>>(emptyList())
        private set
    var familiaActiva by mutableStateOf<Familia?>(null)
        private set
    var membresiaActiva by mutableStateOf<MiembroFamilia?>(null)
        private set
    var nombreNuevo by mutableStateOf("")
        private set
    var cargando by mutableStateOf(false)
        private set
    private val timeoutUsuario = TimeoutSolicitud()
    private val timeoutFamilias = TimeoutSolicitud()
    private val timeoutMembresia = TimeoutSolicitud()
    var cargandoFamilia by mutableStateOf(false)
        private set
    var guardandoNombre by mutableStateOf(false)
        private set
    @get:StringRes
    var errorRecurso by mutableStateOf<Int?>(null)
        private set
    val necesitaNombre: Boolean
        get() = usuario != null && usuario?.nombre.isNullOrBlank()
    fun cargarUsuario() {
        if (cargando) return
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorRecurso = R.string.home_error_no_session
            return
        }
        cargando = true
        errorRecurso = null

        val solicitud = timeoutUsuario.iniciar(scope = viewModelScope) {
                cargando = false
                errorRecurso = R.string.error_network }
        usuarioRepository.obtenerUsuario(usuarioFirebase.uid) { resultado ->
            if (!timeoutUsuario.completar(solicitud)) {
                return@obtenerUsuario }
            resultado
                .onSuccess { perfil ->
                    if (perfil != null) {
                        usuario = perfil
                        cargarFamilias(
                            uidUsuario = usuarioFirebase.uid,
                            finalizarCargaInicial = true,
                            familiaPreferidaId = familiaActiva?.id)
                    } else {
                        crearPerfilFaltante(
                            uid = usuarioFirebase.uid,
                            correo = usuarioFirebase.email.orEmpty())
                    } }
                .onFailure {
                    cargando = false
                    errorRecurso = R.string.home_error_load_profile
                }
        }
    }
    fun recargarHome() {
        if (cargando || cargandoFamilia) return

        errorRecurso = null
        cargarUsuario()
    }

    fun sincronizarFamiliasDesdeGestion(familiaPreferidaId: String?) {
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorRecurso = R.string.home_error_no_session
            return
        }
        errorRecurso = null
        if (familiaPreferidaId == null) {
            familiaActiva = null
            membresiaActiva = null
        }
        cargarFamilias(
            uidUsuario = usuarioFirebase.uid,
            finalizarCargaInicial = false,
            familiaPreferidaId = familiaPreferidaId
        )
    }

    private fun cargarFamilias(
        uidUsuario: String,
        finalizarCargaInicial: Boolean,
        familiaPreferidaId: String? = null
    ) {
        cargandoFamilia = true
        val solicitud = timeoutFamilias.iniciar(scope = viewModelScope) {
            cargandoFamilia = false
            if (finalizarCargaInicial) { cargando = false }
            errorRecurso = R.string.error_network }
        familiaRepository.obtenerFamiliasDelUsuario(uidUsuario) { resultado ->
            if (!timeoutFamilias.completar(solicitud)) {
                return@obtenerFamiliasDelUsuario }
            resultado
                .onSuccess { familiasUsuario ->
                    val familiasOrdenadas =
                        familiasUsuario.sortedBy { it.nombre.lowercase() }
                    familias = familiasOrdenadas
                    if (familiasOrdenadas.isEmpty()) {
                        familiaActiva = null
                        membresiaActiva = null
                        cargandoFamilia = false
                        if (finalizarCargaInicial) { cargando = false }
                        return@onSuccess
                    }
                    val seleccionada =
                        familiaPreferidaId?.let { id ->
                            familiasOrdenadas.firstOrNull { it.id == id } }
                            ?: familiaActiva?.let { anterior ->
                                familiasOrdenadas.firstOrNull {
                                    it.id == anterior.id } }
                            ?: familiasOrdenadas.first()
                    familiaActiva = seleccionada
                    cargarMembresia(
                        familia = seleccionada,
                        uidUsuario = uidUsuario,
                        finalizarCargaInicial = finalizarCargaInicial) }
                .onFailure { cargandoFamilia = false
                    if (finalizarCargaInicial) { cargando = false }
                    errorRecurso = R.string.home_error_load_families
                }
        }
    }

    private fun cargarMembresia(
        familia: Familia,
        uidUsuario: String,
        finalizarCargaInicial: Boolean
    ) {
        cargandoFamilia = true
        val solicitud = timeoutMembresia.iniciar(scope = viewModelScope) {
            membresiaActiva = null
            cargandoFamilia = false
            if (finalizarCargaInicial) { cargando = false }
            errorRecurso = R.string.error_network }
        familiaRepository.obtenerMembresia(
            familiaId = familia.id,
            uidUsuario = uidUsuario
        ) { resultado ->
            if (!timeoutMembresia.completar(solicitud)) {
                return@obtenerMembresia }
            resultado
                .onSuccess { membresia ->
                    membresiaActiva = membresia
                    cargandoFamilia = false
                    if (finalizarCargaInicial) { cargando = false }
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

    fun seleccionarFamilia(familia: Familia) {
        if (familiaActiva?.id == familia.id && membresiaActiva != null) {
            return
        }
        val usuarioFirebase = authRepository.obtenerUsuarioActual()
        if (usuarioFirebase == null) {
            errorRecurso = R.string.home_error_no_session
            return
        }
        familiaActiva = familia
        membresiaActiva = null
        errorRecurso = null
        cargarMembresia(
            familia = familia,
            uidUsuario = usuarioFirebase.uid,
            finalizarCargaInicial = false)
    }

    fun actualizarNombre(nuevoNombre: String) {
        nombreNuevo = nuevoNombre
        errorRecurso = null
    }

    fun guardarNombre() {
        if (guardandoNombre) return
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
                    usuario = perfilActual.copy(nombre = nombreLimpio)
                    nombreNuevo = "" }
                .onFailure {
                    errorRecurso = R.string.home_error_save_name }
        }
    }

    private fun crearPerfilFaltante(
        uid: String,
        correo: String
    ) {
        usuarioRepository.crearUsuario(uid = uid,
            correo = correo) { resultado ->
            resultado
                .onSuccess {
                    usuario = Usuario(
                        uid = uid, nombre = "",
                        correo = correo)
                    cargarFamilias(
                        uidUsuario = uid,
                        finalizarCargaInicial = true) }
                .onFailure {
                    cargando = false
                    errorRecurso = R.string.home_error_load_profile }
        }
    }

    fun cerrarSesion(alCerrarSesion: () -> Unit) {
        timeoutUsuario.cancelar()
        timeoutFamilias.cancelar()
        timeoutMembresia.cancelar()
        authRepository.cerrarSesion()
        usuario = null
        familias = emptyList()
        familiaActiva = null
        membresiaActiva = null
        nombreNuevo = ""
        cargando = false
        cargandoFamilia = false
        guardandoNombre = false
        errorRecurso = null
        alCerrarSesion()
    }
}
